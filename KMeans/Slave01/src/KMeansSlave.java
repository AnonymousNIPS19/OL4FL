import communication.mclient.ClientHandler;
import communication.mclient.MClient;
import communication.utils.Para;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class KMeansSlave {

    public static void main(String[] args) throws IOException, ClassNotFoundException{

        String trainfile="/home/zjq/KMeans/Slave01/src/kdd1.txt";
        String testfile="/home/zjq/KMeans/Slave01/src/kdd1_test.txt";

        KMeans readDataKMeans = new KMeans();
        readDataKMeans.readData(trainfile);
        DBI readTestData=new DBI();
        readTestData.readData(testfile);

        //聚类数k
        int k = 6;

        ArrayList<Double> DBI=new ArrayList<Double>();//DBI(存入列表，便于输出观察)
        double dbi; //DBI，传输给master
        long runtime; //局部聚类运行时间，传输给master
        List<ArrayList<Double>> oldcenter = new ArrayList<>();//保存前一次的簇中心

        int miniBatchNum =500;

        //master的ip和端口
        MClient client = new MClient("192.168.0.183",8803);
        client.init(client);

        while (null == ClientHandler.ctx) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Para paraMtoS = null;
        Para paraStoM = new Para();
        paraStoM.slaveName = "slave1";

        MClient.clientHandler.sendMsg(paraStoM);


        while( true ) {

            paraMtoS = client.waitForReceive();

            if (null == paraMtoS) {
                System.out.println("stop");
                break;
            }
            if (-1 == paraMtoS.state) {
                System.out.println("stop");
                break;
            }

            List<ArrayList<Double>> globalcenter=new ArrayList<>();
            for ( int ia = 0; ia < paraMtoS.centerList.size(); ia++ ){
                ArrayList<Double> des = new ArrayList<>();
                des.addAll(paraMtoS.centerList.get(ia));
                globalcenter.add(des);
            }
            int time = paraMtoS.time;

            System.out.println("\n\n接收到的全局簇中心： ");
            for (int i=0; i<k; i++){
                System.out.println(globalcenter.get(i));
            }
            System.out.println("接收到的迭代数： " + time);

            if (time==0){
                try {
                    Thread.sleep(100);
                    System.out.println("\n\n\n\nttttttttttttttt:\n\n\n\n");
                    time=1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /////////////////////////接收到master发来的参数，进行下一次迭代时，就要新的数据/////////////////////////

            long startTime = System.currentTimeMillis();
            KMeans kmeans = new KMeans(k, time, globalcenter,miniBatchNum);//聚类
            List<ArrayList<Double>> localcenter = kmeans.getNewCenter();
            int[] num = new int[k];
            for (int i=0; i<k; i++) {
                num[i] = kmeans.getHelpCenterList().get(i).size();
            }


            //计算两次上传的簇中心之间的距离
            double[] distance=new double[k];
            if(!oldcenter.isEmpty()) {
                for (int i = 0; i < k; i++) {
                    distance[i]=0;
                    for (int j = 0; j < localcenter.get(0).size(); j++) {//计算两点之间的欧式距离
                        distance[i] += (localcenter.get(i).get(j) - oldcenter.get(i).get(j)) * (localcenter.get(i).get(j) - oldcenter.get(i).get(j));
                    }
                    distance[i]=Math.sqrt(distance[i]);
                    System.out.println("distance"+i+":" + distance[i] );
                }

            }
            for (ArrayList<Double> aLocalcenter : localcenter) {
                ArrayList<Double> des = new ArrayList<>();
                des.addAll(aLocalcenter);
                oldcenter.add(des);
            }

            //计算局部DBI
            DBI test=new DBI(k,localcenter,miniBatchNum);
            dbi=test.Dbi;
            DBI.add(dbi);


            long endTime = System.currentTimeMillis();//局部聚类结束时间,即为上传时间，单位ns
            runtime=endTime - startTime;

            //计算
            paraStoM.centerList= localcenter;
            paraStoM.num = num;
            paraStoM.DBI=dbi;
            paraStoM.sendtime=endTime;
            paraStoM.runtime=runtime;
            MClient.clientHandler.sendMsg(paraStoM);

        }

        System.out.println("\n\nDBI:"+DBI);

    }
}
