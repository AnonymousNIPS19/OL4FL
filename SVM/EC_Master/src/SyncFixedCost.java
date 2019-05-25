import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import utils.WriteToFile;

public class SyncFixedCost {

    public static void main(String[] args) throws IOException{

        // Batch size
        int miniBatchNum =500;

        Accuracy readdata=new Accuracy();
        readdata.loadData();


        MServer server = new MServer(8802);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                server.begin();
            }
        }).start();

        ArrayList<float[]> globalw = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            float[] w = new float[Accuracy.alltestX.get(0).size()];
            for (int j=0;j<Accuracy.alltestX.get(0).size();j++){
                w[j]=0;
            }
            globalw.add(w);
        }
        ArrayList<Double> Accuracy = new ArrayList<Double>();

        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 51);
        armMap.put(1, 40);
        armMap.put(2, 27);
        armMap.put(3, 18);
        armMap.put(4, 6);
        armMap.put(5, 3);
        FixedCost fixedCost = new FixedCost();

        boolean isFirst = true;
        int recycleCount = 0;
        int clientNum = 3;
        int N = 100;
        int sum = 0;

        List<MParaChannel> paraList = new ArrayList<>();

        double[] slaveacc=new double[clientNum];
        long[] sendtime=new long[clientNum];
        long[] uploadtime=new long[clientNum];
        long[] runtime=new long[clientNum];

        // Connect 3 slaves, and then initialize global parameter
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

        Para paraMtoS = new Para();
        paraMtoS.time = 1;
        paraMtoS.w = globalw;
        MServer.serverHandler.broadcast(paraMtoS);

        for( int i = 0; i < N; i++ ) {
            while (clientNum != MServer.paraQueue.size()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            recycleCount = i;

            for (int j = 0; j < clientNum; j++) {
                MParaChannel paraChannel = MServer.paraQueue.poll();
                if (null != paraChannel) {
                    paraList.add(paraChannel);
                    //....
                }
            }

            long receiveTime = System.currentTimeMillis();

            // Receive local parameter and update global parameter
            SVM svm = new SVM(
                    paraList.get(0).paraStoM.w, paraList.get(1).paraStoM.w, paraList.get(2).paraStoM.w,
                    paraList.get(0).paraStoM.num, paraList.get(1).paraStoM.num, paraList.get(2).paraStoM.num);

            paraList.clear();

            globalw = svm.getW_list();

            // Get Accuracy
            Accuracy test = new Accuracy(globalw,miniBatchNum);
            Accuracy.add(test.getAcc());


            if( isFirst ){
                isFirst = false;
            } else {
                fixedCost.updateEstimate();
            }

            // The distribution of each arm of MAB is modified according to the value of Accuracy,
            // so as to select the arm I of the next iteration
            int arm = -1;
            if( fixedCost.isResourceEnough() ){
                arm = fixedCost.mab(1);
            }
            else {
                System.out.println("run out of resource");
                break;
            }

            int t = armMap.get(arm);

            paraMtoS.time = t;
            paraMtoS.w = globalw;
            MServer.serverHandler.broadcast(paraMtoS);
        }
        System.out.println("ready to stop!!!");
        paraMtoS = new Para();
        paraMtoS.state = -1;
        MServer.serverHandler.broadcast(paraMtoS);
        MServer.closeGracefully();

        System.out.println("END");
        if( N ==  (recycleCount+1) ){
            System.out.println("Normal Exit !");

        }else{
            System.out.println("Abnormal Exit：expect" + N + "times，but only executed" + (recycleCount+1) + "times");
        }
        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        mapRegret.put("sync", fixedCost.regrets);
//        System.out.println("\n\nDBI: " + DBI);
        System.out.println("accuracy"+Accuracy);


    }
}