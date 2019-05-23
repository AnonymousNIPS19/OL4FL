import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import utils.WriteToFile;

public class KMeansMastercost {

    public static void main(String[] args) throws IOException{
        int k =6;
        int miniBatchNum =500;

        KMeans readDataKMeans = new KMeans();
        readDataKMeans.readData();


        MServer server = new MServer(8803);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                server.begin();
            }
        }).start();

        List<ArrayList<Double>> globalcenter = new ArrayList<>();
        ArrayList<Double> DBI = new ArrayList<Double>();
        //        f1f1f1
        ArrayList<Double> f1=new ArrayList<Double>();
        List<ArrayList<Double>> oldcenter = new ArrayList<>();


        //time
        int syncio=0;
        ArrayList<Integer> IO=new ArrayList<Integer>();
        int sumio=0;
        ArrayList<Integer> sumIO=new ArrayList<Integer>();

        int synccpu=0;
        ArrayList<Integer> CPU=new ArrayList<Integer>();
        int sumcpu=0;
        ArrayList<Integer> sumCPU=new ArrayList<Integer>();

        int syncsum=0;
        ArrayList<Integer> sumlist=new ArrayList<Integer>();
        int sumtime=0;
        ArrayList<Integer> sumTime=new ArrayList<Integer>();


        double[] distance = new double[k];
        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 50);
        armMap.put(1, 40);
        armMap.put(2, 30);
        armMap.put(3, 20);
        armMap.put(4, 10);
        armMap.put(5, 1);
        mabwithcost mabwithcost=new mabwithcost();

        //同步情况下
        boolean isFirst = true;
        int recycleCount = 0;
        int clientNum = 3;
        int N = 300;
        int sum = 0;

        List<MParaChannel> paraList = new ArrayList<>();

        //
        double[] slavedbi=new double[clientNum];//slave传来的局部DBI
        long[] sendtime=new long[clientNum];//slave传来的上传时间
        long[] uploadtime=new long[clientNum];//上传用时，IO时间
        long[] runtime=new long[clientNum];//局部迭代时间，CPU时间
        int[] io=new int[clientNum];
        int[] cpu=new int[clientNum];

        while (clientNum != MServer.paraQueue.size()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        for (int j = 0; j < clientNum; j++) {
            MParaChannel paraChannel = MServer.paraQueue.poll();
            if (null != paraChannel) {
                paraList.add(paraChannel);
                //....
            }
        }
        System.out.println("wait for 3 slave in paraQueue");
        paraList.clear();

        KMeans center = new KMeans(k,miniBatchNum);
        globalcenter = center.getCenter();

        Para paraMtoS = new Para();
        paraMtoS.time = 1;
        paraMtoS.centerList = globalcenter;
        MServer.serverHandler.broadcast(paraMtoS);

        for( int i = 0; i < N; i++ ) {
            while (clientNum != MServer.paraQueue.size()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("第" + i + "次接受到数据的客户端个数：" + clientNum);
            recycleCount = i;

            for (int j = 0; j < clientNum; j++) {
                MParaChannel paraChannel = MServer.paraQueue.poll();
                if (null != paraChannel) {
                    paraList.add(paraChannel);
                    //....
                }
            }

            long receiveTime = System.currentTimeMillis();//master接收到的时间

            KMeans kmeans = new KMeans(
                    paraList.get(0).paraStoM.centerList, paraList.get(1).paraStoM.centerList, paraList.get(2).paraStoM.centerList,
                    paraList.get(0).paraStoM.num, paraList.get(1).paraStoM.num, paraList.get(2).paraStoM.num, k);

                for (int j = 0; j < clientNum; j++) {
                    runtime[j] = paraList.get(j).paraStoM.runtime;
                    sendtime[j] = paraList.get(j).paraStoM.sendtime;
                    uploadtime[j] = receiveTime - sendtime[j];
                    io[j] = (int)uploadtime[j];
                    cpu[j] = (int) runtime[j];
                }

             //time
            syncio=io[0]+io[1]+io[2];
            IO.add(syncio);
            sumio=sumio+syncio;
            sumIO.add(sumio);

            synccpu=cpu[0]+cpu[1]+cpu[2];
            CPU.add(synccpu);
            sumcpu=sumcpu+synccpu;
            sumCPU.add(sumcpu);
            syncsum=synccpu+syncio;
            sumlist.add(syncsum);
            sumtime=sumtime+syncsum;
            sumTime.add(sumtime);

            System.out.println("io:" + Arrays.toString(io) + "ns" );
            System.out.println("cpu:" + Arrays.toString(cpu) + "ns" );

            if (i==0) {
                mabwithcost.newio = Arrays.stream(io).min().getAsInt();
                mabwithcost.newcpu = Arrays.stream(cpu).min().getAsInt();
            }
            else  {
                mabwithcost.newio = Arrays.stream(io).max().getAsInt();
                mabwithcost.newcpu = Arrays.stream(cpu).max().getAsInt();
            }
            paraList.clear();

            long start = System.currentTimeMillis();
            globalcenter = kmeans.getCenter();//获得全局簇中心

            //计算两次全局簇中心之间的距离
            if(!oldcenter.isEmpty()) {
                for (int t = 0; t < k; t++) {
                    distance[t]=0;
                    for (int j = 0; j < globalcenter.get(0).size(); j++) {//计算两点之间的欧式距离
                        distance[t] += (globalcenter.get(t).get(j) - oldcenter.get(t).get(j)) * (globalcenter.get(t).get(j) - oldcenter.get(t).get(j));
                    }
                    distance[t]= (double) Math.sqrt(distance[t]);
                    System.out.println("distance"+t+":" + distance[t] );
                }

            }
            for(int j = 0; j < globalcenter.size(); j++){
                oldcenter.add( (ArrayList<Double>)globalcenter.get(j).clone());
            }

            //计算评价指标DBI
            ///kkk
            KMeans kmean=new KMeans(k, globalcenter,miniBatchNum);
            List<ArrayList<Double>> test_center = kmean.getNewCenter();
            DBI test=new DBI(test_center, kmean.getHelpCenterList());
            DBI.add(test.dbi);
            //f1f1f1
            F1measure f1measure=new F1measure(kmean.train_target,kmean.predict_target);
            f1.add(f1measure.f1);

            if( isFirst ){
                isFirst = false;
            } else {
                mabwithcost.updateEstimate();
            }


            int arm = -1;
            if( mabwithcost.isResourceEnough() ){
                arm = mabwithcost.mab(1);
            }
            else {
                System.out.println("run out of resource");
                break;
            }

            int t = armMap.get(arm);
            System.out.println("\n\nt"+t);
            long end = System.currentTimeMillis();


            paraMtoS.time = t;
            paraMtoS.centerList = globalcenter;
            MServer.serverHandler.broadcast(paraMtoS);

        }
        System.out.println("ready to stop!!!");
        paraMtoS = new Para();
        paraMtoS.state = -1;
        MServer.serverHandler.broadcast(paraMtoS);
        MServer.closeGracefully();

        System.out.println("END");
        if( N ==  (recycleCount+1) ){
            System.out.println("正常退出");

        }else{
            System.out.println("异常退出：预计" + N + "次，但只执行了" + (recycleCount+1) + "次");
        }
        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        mapRegret.put("sync", mabwithcost.regrets);
        System.out.println("dbi"+DBI);
        System.out.println("f1"+f1);

    }
}
