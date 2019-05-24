import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KMeansMasterKUBE {

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
        ArrayList<Double> f1=new ArrayList<Double>();
        List<ArrayList<Double>> oldcenter = new ArrayList<>();
        double[] distance = new double[k];
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

        double[] slavedbi=new double[clientNum];//slave传来的局部DBI
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

            long receiveTime = System.nanoTime();//master接收到的时间

            KMeans kmeans = new KMeans(
                    paraList.get(0).paraStoM.centerList, paraList.get(1).paraStoM.centerList, paraList.get(2).paraStoM.centerList,
                    paraList.get(0).paraStoM.num, paraList.get(1).paraStoM.num, paraList.get(2).paraStoM.num, k);

            for (int j=0;j<clientNum;j++) {
                slavedbi[j] = paraList.get(j).paraStoM.DBI;
            }
            paraList.clear();
            globalcenter = kmeans.getCenter();//获得全局簇中心

            //计算两次全局簇中心之间的距离
            if(!oldcenter.isEmpty()) {
                for (int t = 0; t < k; t++) {
                    distance[t]=0;
                    for (int j = 0; j < globalcenter.get(0).size(); j++) {//计算两点之间的欧式距离
                        distance[t] += (globalcenter.get(t).get(j) - oldcenter.get(t).get(j)) * (globalcenter.get(t).get(j) - oldcenter.get(t).get(j));
                    }
                    distance[t]=Math.sqrt(distance[t]);
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
                kube.updateEstimate();
            }
            //根据DBI的值来修改MAB每条臂的分布，从而选出下一次迭代的臂i

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
        mapRegret.put("sync", kube.regrets);
//        System.out.println("\n\nDBI: " + DBI);
        System.out.println("\n\nF1: " + f1);


    }
}
