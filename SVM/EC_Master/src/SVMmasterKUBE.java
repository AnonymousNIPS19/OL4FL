import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import utils.WriteToFile;

public class SVMmasterKUBE{

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

        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 51);
        armMap.put(1, 40);
        armMap.put(2, 27);
        armMap.put(3, 18);
        armMap.put(4, 6);
        armMap.put(5, 3);
        KUBE kube = new KUBE();

        //同步情况下
        boolean isFirst = true;
        int recycleCount = 0;
        int clientNum = 3;
        int N = 100;
        int sum = 0;

        List<MParaChannel> paraList = new ArrayList<>();

        double[] slaveacc=new double[clientNum];//slave传来的局部acc
        long[] sendtime=new long[clientNum];//slave传来的上传时间
        long[] uploadtime=new long[clientNum];//上传用时，IO时间
        long[] runtime=new long[clientNum];//局部迭代时间，CPU时间

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

            paraList.clear();

            globalw = svm.getW_list();//获得全局簇中心

            //计算评价指标acc
            ///kkk
            Accuracy test = new Accuracy(globalw,miniBatchNum);
            Accuracy.add(test.getAcc());


            if( isFirst ){
                isFirst = false;
            } else {
                kube.updateEstimate();
            }

            int arm = -1;
            if( kube.isResourceEnough() ){
                arm = kube.mab(1);
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
            System.out.println("正常退出");

        }else{
            System.out.println("异常退出：预计" + N + "次，但只执行了" + (recycleCount+1) + "次");
        }
        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        mapRegret.put("sync", kube.regrets);
//        System.out.println("\n\nDBI: " + DBI);
        System.out.println("accuracy"+Accuracy);


    }
}