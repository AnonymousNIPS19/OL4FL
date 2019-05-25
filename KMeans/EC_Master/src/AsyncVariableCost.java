import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import utils.WriteToFile;

public class AsyncVariableCost {

    public static void main(String[] args) throws IOException {

        KMeans readDataKMeans = new KMeans();
        readDataKMeans.readData();

        // Cluster number
        int k =6;

        // Batch size
        int miniBatchNum =500;

        MServer server = new MServer(8803);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                server.begin();
            }
        }).start();

        List<ArrayList<Double>> globalcenter = new ArrayList<>();
        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 50);
        armMap.put(1, 40);
        armMap.put(2, 30);
        armMap.put(3, 20);
        armMap.put(4, 10);
        armMap.put(5, 1);

        HashMap<String, VariableCost> mapmabwithcost = new HashMap<>();
        VariableCost mab1 = new VariableCost();
        mab1.resource =10000;
        mapmabwithcost.put("slave1", mab1);
        VariableCost mab2 = new VariableCost();
        mab2.resource = 10000;
        mapmabwithcost.put("slave2", mab2);
        VariableCost mab3 = new VariableCost();
        mab3.resource = 10000;
        mapmabwithcost.put("slave3", mab3);

        HashMap<String, Boolean> mapIsFirst = new HashMap<>();
        mapIsFirst.put("slave1", true);
        mapIsFirst.put("slave2", true);
        mapIsFirst.put("slave3", true);


        HashMap<String, ArrayList<Double>> mapDBI = new HashMap<>();
        ArrayList<Double> mapDBI1=new ArrayList<Double>();
        mapDBI.put("slave1",mapDBI1);
        ArrayList<Double> mapDBI2=new ArrayList<Double>();
        mapDBI.put("slave2",mapDBI2);
        ArrayList<Double> mapDBI3=new ArrayList<Double>();
        mapDBI.put("slave3",mapDBI3);
        HashMap<String, Integer> mapNum = new HashMap<>();
        int mapNum1 = 0,mapNum2=0,mapNum3=0;
        mapNum.put("slave1",mapNum1);
        mapNum.put("slave2",mapNum2);
        mapNum.put("slave3",mapNum3);


        KMeans kmeans = new KMeans();

        List<ArrayList<Double>> oldCenterList = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> DBI = new ArrayList<Double>();
        ArrayList<Double> f1=new ArrayList<Double>();
        List<ArrayList<Double>> oldcenter = new ArrayList<>();
        double[] distance = new double[k];
        int[] oldNum = null;


        double slavedbi;
        // Upload time from slave
        long sendtime;
        // Upload time, IO time
        long uploadtime=0;
        // Local iteration time, CPU time
        long runtime=0;

        ArrayList<Integer> IO=new ArrayList<Integer>();
        int sumio=0;
        ArrayList<Integer> sumIO=new ArrayList<Integer>();
        ArrayList<Integer> CPU=new ArrayList<Integer>();
        int sumcpu=0;
        ArrayList<Integer> sumCPU=new ArrayList<Integer>();
        int asyncsum=0;
        ArrayList<Integer> sumlist=new ArrayList<Integer>();
        int sumtime=0;
        ArrayList<Integer> sumTime=new ArrayList<Integer>();


        boolean isFirst = true;
        int aN = 1888;
        int ai = 0;
        int clientNum = 3;
        int num_stop = 0;

        // Connect 3 slaves, and then initialize global parameter
        Para aparaMtoS = new Para();
        while (clientNum != MServer.paraQueue.size()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<MParaChannel> paraList = new ArrayList<>();
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


        while( ai < aN || num_stop < 3) {
            ai++;
            MParaChannel paraChannel = null;
            try {
                // Block until there is a value
                paraChannel = MServer.paraQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Para paraStoM = paraChannel.paraStoM;
            Channel socketChannel = paraChannel.socketChannel;


            long receivetime = System.currentTimeMillis();
            // Global parameter update
            if( isFirst ){
                isFirst = false;
                globalcenter = paraStoM.centerList;
                oldNum = paraStoM.num;
                runtime=paraStoM.runtime;
                sendtime=paraStoM.sendtime;
                uploadtime=(receivetime-sendtime);
            }else{
                kmeans.kmeans(paraStoM.centerList, oldCenterList, paraStoM.num, oldNum, k);
                globalcenter = kmeans.getCenter();
                oldNum = kmeans.arrNum;
                runtime=paraStoM.runtime;
                slavedbi=paraStoM.DBI;
                sendtime=paraStoM.sendtime;
                uploadtime = receivetime - sendtime;
            }

            int io= (int) uploadtime;
            int cpu = (int) (runtime);

            if(!oldCenterList.isEmpty()) {
                for (int i = 0; i < k; i++) {
                    distance[i]=0;
                    for (int j = 0; j < globalcenter.get(0).size(); j++) {
                        distance[i] += (globalcenter.get(i).get(j) - oldCenterList.get(i).get(j)) * (globalcenter.get(i).get(j) - oldCenterList.get(i).get(j));
                    }
                    distance[i]=Math.sqrt(distance[i]);
                    System.out.println("distance"+i+":" + distance[i] );
                }

            }

            oldCenterList.clear();
            for(int i = 0; i < globalcenter.size(); i++){
                oldCenterList.add( (ArrayList<Double>)globalcenter.get(i).clone());
            }

            // Get DBI
            KMeans kmean=new KMeans(k, globalcenter,miniBatchNum);
            List<ArrayList<Double>> test_center =kmean.getNewCenter();
            DBI test=new DBI(test_center, kmean.getHelpCenterList());
            DBI.add(test.dbi);
            // Get F1-score
            F1score f1Score =new F1score(kmean.train_target,kmean.predict_target);
            f1.add(f1Score.f1);


            if( mapIsFirst.get(paraStoM.slaveName) ){
                mapIsFirst.put(paraStoM.slaveName, false);
            }else {
                mapmabwithcost.get(paraStoM.slaveName).updateEstimate();
            }
            System.out.println("paraStoM.slaveName:"+paraStoM.slaveName);


            int arm = -1;
            VariableCost mab = mapmabwithcost.get(paraStoM.slaveName);

            ArrayList<Double> mapdbi=mapDBI.get(paraStoM.slaveName);
            int num=mapNum.get(paraStoM.slaveName);
            mapdbi.add(paraStoM.DBI);
            if(mapdbi.size()>1){
                double d=mapdbi.get(mapdbi.size()-1)-mapdbi.get(mapdbi.size()-2);
                if(d*d<0.0001){
                    num=num+1;
                    mapNum.put(paraStoM.slaveName, num);
                }
                else num=0;
            }


            if(num==5){
                aparaMtoS.time = 0;
                aparaMtoS.centerList = globalcenter;
                MServer.serverHandler.sendOneChannel(socketChannel, aparaMtoS);
                mapNum.put(paraStoM.slaveName, 0);
            }
            else {
                if (mab.isResourceEnough()) {
                    mab.newio = io;
                    mab.newcpu = cpu;

                    //time
                    IO.add(mab.newio);
                    sumio = sumio + mab.newio;
                    sumIO.add(sumio);

                    CPU.add(mab.newcpu);
                    sumcpu = sumcpu + mab.newcpu;
                    sumCPU.add(sumcpu);
                    asyncsum = mab.newcpu + mab.newio;
                    sumlist.add(asyncsum);
                    sumtime = sumtime + asyncsum;
                    sumTime.add(sumtime);

                    arm = mab.mab(1);
                    int t = armMap.get(arm);
                    aparaMtoS.time = t;
                    aparaMtoS.centerList = globalcenter;
                    MServer.serverHandler.sendOneChannel(socketChannel, aparaMtoS);
                } else {
                    num_stop++;
                    System.out.println("num_stop" + num_stop);
                    Para endParaMtoS = new Para();
                    endParaMtoS.state = -1;
                    MServer.serverHandler.sendOneChannel(socketChannel, endParaMtoS);

                }
            }

            if(num_stop==clientNum) break;

        }
        System.out.println("ready to stop!!!");
        Para endParaMtoS = new Para();
        endParaMtoS.state = -1;
        MServer.serverHandler.broadcast(endParaMtoS);
        MServer.closeGracefully();


        System.out.println("END");
        if( aN ==  ai){
            System.out.println("Normal Exit !");
        }else{
            System.out.println("Abnormal Exit：expect" + aN + "times，but only executed" + ai + "times.");
        }

        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        Iterator<Map.Entry<String, VariableCost>> entries = mapmabwithcost.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, VariableCost> entry = entries.next();
            mapRegret.put(entry.getKey(), entry.getValue().regrets);
        }


    }
}
