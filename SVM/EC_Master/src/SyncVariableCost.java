import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class SyncVariableCost {

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

        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 50);
        armMap.put(1, 40);
        armMap.put(2, 30);
        armMap.put(3, 20);
        armMap.put(4, 10);
        armMap.put(5, 1);
        VariableCost VariableCost =new VariableCost();


        boolean isFirst = true;
        int recycleCount = 0;
        int clientNum = 3;
        int N = 300;
        int sum = 0;

        List<MParaChannel> paraList = new ArrayList<>();

        double[] slavedbi=new double[clientNum];
        // Upload time from slave
        long[] sendtime=new long[clientNum];
        // Upload time, IO time
        long[] uploadtime=new long[clientNum];
        // Local iteration time, CPU time
        long[] runtime=new long[clientNum];
        int[] io=new int[clientNum];
        int[] cpu=new int[clientNum];

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
                VariableCost.newio = Arrays.stream(io).min().getAsInt();
                VariableCost.newcpu = Arrays.stream(cpu).min().getAsInt();
            }
            else  {
                VariableCost.newio = Arrays.stream(io).max().getAsInt();
                VariableCost.newcpu = Arrays.stream(cpu).max().getAsInt();
            }
            paraList.clear();

            long start = System.currentTimeMillis();
            globalw = svm.getW_list();


            // Get Accuracy
            Accuracy test = new Accuracy(globalw,miniBatchNum);
            Accuracy.add(test.getAcc());

            if( isFirst ){
                isFirst = false;
            } else {
                VariableCost.updateEstimate();
            }


            // The distribution of each arm of MAB is modified according to the value of Accuracy,
            // so as to select the arm I of the next iteration
            int arm = -1;
            if( VariableCost.isResourceEnough() ){
                arm = VariableCost.mab(1);
            }
            else {
                System.out.println("run out of resource");
                break;
            }

            int t = armMap.get(arm);
            System.out.println("\n\nt"+t);
            long end = System.currentTimeMillis();

            // Master send parameter to slave
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
        mapRegret.put("sync", VariableCost.regrets);
        System.out.println("Accuracy"+Accuracy);


    }
}
