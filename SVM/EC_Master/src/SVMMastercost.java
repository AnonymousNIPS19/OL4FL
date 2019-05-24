import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class SVMMastercost {

    public static void main(String[] args) throws IOException{
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
        mabwithcost mabwithcost=new mabwithcost();

        //同步情况下
        boolean isFirst = true;
        int recycleCount = 0;
        int clientNum = 3;
        int N = 300;
        int sum = 0;

        List<MParaChannel> paraList = new ArrayList<>();

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
                mabwithcost.newio = Arrays.stream(io).min().getAsInt();
                mabwithcost.newcpu = Arrays.stream(cpu).min().getAsInt();
            }
            else  {
                mabwithcost.newio = Arrays.stream(io).max().getAsInt();
                mabwithcost.newcpu = Arrays.stream(cpu).max().getAsInt();
            }
            paraList.clear();

            long start = System.currentTimeMillis();
            globalw = svm.getW_list();//获得全局簇中心


            //计算评价指标
            Accuracy test = new Accuracy(globalw,miniBatchNum);
            Accuracy.add(test.getAcc());

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
            System.out.println("正常退出");

        }else{
            System.out.println("异常退出：预计" + N + "次，但只执行了" + (recycleCount+1) + "次");
        }
        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        mapRegret.put("sync", mabwithcost.regrets);
        System.out.println("Accuracy"+Accuracy);


    }
}
