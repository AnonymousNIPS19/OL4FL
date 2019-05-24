import communication.mserver.MParaChannel;
import communication.mserver.MServer;
import communication.utils.Para;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import utils.WriteToFile;

public class SVMmasterAsyncKUBE {

    public static void main(String[] args) throws IOException {

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


        HashMap<Integer, Integer> armMap = new HashMap<>();
        armMap.put(0, 51);
        armMap.put(1, 40);
        armMap.put(2, 27);
        armMap.put(3, 18);
        armMap.put(4, 6);
        armMap.put(5, 3);
        HashMap<String, KUBE> mapKUBE = new HashMap<>();
        KUBE kube1 = new KUBE();
        kube1.allCPU = 1000;
        kube1.allIO = 50;
        mapKUBE.put("slave1", kube1);
        KUBE kube2 = new KUBE();
        kube2.allCPU = 1000;
        kube2.allIO = 150;
        mapKUBE.put("slave2", kube2);
        KUBE kube3 = new KUBE();
        kube3.allCPU = 1000;
        kube3.allIO = 200;
        mapKUBE.put("slave3", kube3);

        HashMap<String, Boolean> mapIsFirst = new HashMap<>();
        mapIsFirst.put("slave1", true);
        mapIsFirst.put("slave2", true);
        mapIsFirst.put("slave3", true);

        HashMap<String, ArrayList<Double>> mapAcc = new HashMap<>();
        ArrayList<Double> mapAcc1=new ArrayList<Double>();
        mapAcc.put("slave1",mapAcc1);
        ArrayList<Double> mapAcc2=new ArrayList<Double>();
        mapAcc.put("slave2",mapAcc2);
        ArrayList<Double> mapAcc3=new ArrayList<Double>();
        mapAcc.put("slave3",mapAcc3);
        HashMap<String, Integer> mapNum = new HashMap<>();
        int mapNum1 = 0,mapNum2=0,mapNum3=0;
        mapNum.put("slave1",mapNum1);
        mapNum.put("slave2",mapNum2);
        mapNum.put("slave3",mapNum3);

        ArrayList<float[]> globalw = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            float[] w = new float[Accuracy.alltestX.get(0).size()];
            for (int j=0;j<Accuracy.alltestX.get(0).size();j++){
                w[j]=0;
            }
            globalw.add(w);
        }
        ArrayList<float[]> oldw=new ArrayList<>();
        ArrayList<Double> Accuracy = new ArrayList<Double>();
        int oldNum = 0;

        double slaveacc; //slave传来的局部acc

        //异步情况下
        boolean isFirst = true;
        int aN = 1888;
        int ai = 0;
        int clientNum = 3;
        int num_stop = 0;

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


        Para paraMtoS = new Para();
        paraMtoS.time = 1;
        paraMtoS.w = globalw;
        MServer.serverHandler.broadcast(paraMtoS);



        while( ai < aN || num_stop < 3) {
            ai++;
            MParaChannel paraChannel = null;
            try {
                paraChannel = MServer.paraQueue.take();//阻塞直到有值
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Para paraStoM = paraChannel.paraStoM;
            Channel socketChannel = paraChannel.socketChannel;

            long receivetime = System.nanoTime();

            if( isFirst ){
                isFirst = false;
                globalw = paraStoM.w;
                oldNum = paraStoM.num;
            }else{
                SVM svm = new SVM(paraStoM.w,oldw,paraStoM.num,oldNum);
                globalw=svm.getW_list();//获得全局簇中心
                oldNum = svm.arrNum;
                slaveacc=paraStoM.acc;
            }


            ///kkk
            Accuracy test = new Accuracy(globalw,miniBatchNum);
            Accuracy.add(test.getAcc());

            if( mapIsFirst.get(paraStoM.slaveName) ){
                mapIsFirst.put(paraStoM.slaveName, false);
            }else {
                mapKUBE.get(paraStoM.slaveName).updateEstimate();
            }


            int arm = -1;
            KUBE kube = mapKUBE.get(paraStoM.slaveName);

            ArrayList<Double> mapacc=mapAcc.get(paraStoM.slaveName);
            int num=mapNum.get(paraStoM.slaveName);
            mapacc.add(paraStoM.acc);
            if(mapacc.size()>1){
                double d=mapacc.get(mapacc.size()-1)-mapacc.get(mapacc.size()-2);
                if(d*d<0.0001){
                    num=num+1;
                    mapNum.put(paraStoM.slaveName, num);
                }
                else num=0;
            }

            if(num==5){
                aparaMtoS.time = 0;
                aparaMtoS.w = globalw;
                MServer.serverHandler.sendOneChannel(socketChannel, aparaMtoS);
                mapNum.put(paraStoM.slaveName, 0);
            }
            else {
                if (kube.isResourceEnough()) {
                    arm = kube.mab(1);
                    int t = armMap.get(arm);
                    aparaMtoS.time = t;
                    aparaMtoS.w = globalw;
                    MServer.serverHandler.sendOneChannel(socketChannel, aparaMtoS);
                } else {
                    num_stop++;
                    Para endParaMtoS = new Para();
                    endParaMtoS.state = -1;
                    MServer.serverHandler.sendOneChannel(socketChannel, endParaMtoS);
                }
            }

        }
        System.out.println("ready to stop!!!");
        Para endParaMtoS = new Para();
        endParaMtoS.state = -1;
        MServer.serverHandler.broadcast(endParaMtoS);
        MServer.closeGracefully();

        System.out.println("END");
        if( aN ==  ai){
            System.out.println("正常退出");
        }else{
            System.out.println("异常退出：预计" + aN + "次，但只执行了" + ai + "次");
        }

        HashMap<String, List<Double>> mapRegret = new HashMap<>();
        Iterator<Map.Entry<String, KUBE>> entries = mapKUBE.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, KUBE> entry = entries.next();
            mapRegret.put(entry.getKey(), entry.getValue().regrets);
        }


    }
}