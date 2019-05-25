import communication.mclient.ClientHandler;
import communication.mclient.MClient;
import communication.utils.Para;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class KMeansSlave {

    public static void main(String[] args) throws IOException, ClassNotFoundException{

        // Cluster k
        int k = 6;
        // Batch size
        int miniBatchNum =500;

        String trainfile="kdd1.txt";
        String testfile="kdd1_test.txt";
        KMeans readDataKMeans = new KMeans();
        readDataKMeans.readData(trainfile);
        DBI readTestData=new DBI();
        readTestData.readData(testfile);

        ArrayList<Double> DBI=new ArrayList<Double>();
        double dbi;

        // Local iteration time consumption
        long runtime;
        List<ArrayList<Double>> oldcenter = new ArrayList<>();

        // Socket communication
        MClient client = new MClient("localhost",8803);
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

            // Receive global parameter and new update interval
            List<ArrayList<Double>> globalcenter=new ArrayList<>();
            for ( int ia = 0; ia < paraMtoS.centerList.size(); ia++ ){
                ArrayList<Double> des = new ArrayList<>();
                des.addAll(paraMtoS.centerList.get(ia));
                globalcenter.add(des);
            }
            int time = paraMtoS.time;

            if (time==0){
                try {
                    Thread.sleep(100);
                    time=1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Update local parameter
            long startTime = System.currentTimeMillis();
            KMeans kmeans = new KMeans(k, time, globalcenter,miniBatchNum);
            List<ArrayList<Double>> localcenter = kmeans.getNewCenter();
            int[] num = new int[k];
            for (int i=0; i<k; i++) {
                num[i] = kmeans.getHelpCenterList().get(i).size();
            }

            double[] distance=new double[k];
            if(!oldcenter.isEmpty()) {
                for (int i = 0; i < k; i++) {
                    distance[i]=0;
                    for (int j = 0; j < localcenter.get(0).size(); j++) {
                        distance[i] += (localcenter.get(i).get(j) - oldcenter.get(i).get(j)) * (localcenter.get(i).get(j) - oldcenter.get(i).get(j));
                    }
                    distance[i]=Math.sqrt(distance[i]);
                }

            }
            oldcenter.clear();
            for(int i = 0; i < localcenter.size(); i++){
                oldcenter.add( (ArrayList<Double>)localcenter.get(i).clone());
            }

            // Compute DBI
            DBI test=new DBI(k,localcenter,miniBatchNum);
            dbi=test.Dbi;
            DBI.add(dbi);

            long endTime = System.currentTimeMillis();
            runtime=endTime - startTime;

            // Slave send parameter to master
            paraStoM.centerList= localcenter;
            paraStoM.num = num;
            paraStoM.DBI=dbi;
            paraStoM.sendtime=endTime;
            paraStoM.runtime=runtime;
            MClient.clientHandler.sendMsg(paraStoM);

        }
    }
}
