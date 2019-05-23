import communication.mclient.ClientHandler;
import communication.mclient.MClient;
import communication.utils.Para;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class SVMSlave {

    public static void main(String[] args) throws IOException, ClassNotFoundException{

        int miniBatchNum =500;
        String trainfile="/home/zjq/SVM/Slave01/src/mnist_train1.txt";
        String testfile="/home/zjq/SVM/Slave01/src/mnist_test1.txt";

        SVM readData=new SVM();
        readData.readData(trainfile);
        Accuracy readdata=new Accuracy();
        readdata.loadData(testfile);

        ArrayList<Double> Accuracy = new ArrayList<Double>();//acc(存入列表，便于输出观察)
        double acc; //accuracy，传输给master
        long runtime; //局部聚类运行时间，传输给master
        List<ArrayList<Double>> oldcenter = new ArrayList<>();
        ArrayList<float[]> localw =new ArrayList<>();

                //master的ip和端口
        MClient client = new MClient("192.168.0.183",8802);
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
        paraStoM.slaveName = "slave2";

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

            ArrayList<float[]> globalw = paraMtoS.w;
            int time = paraMtoS.time;

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
            SVM Svm = new SVM(time, globalw,miniBatchNum);
            localw = Svm.getW_list();

            //计算局部acc
            Accuracy test = new Accuracy(localw,miniBatchNum);
            acc = test.getAcc();
            Accuracy.add(acc);


            long endTime = System.currentTimeMillis();//局部聚类结束时间,即为上传时间，单位ns
            runtime=endTime - startTime;

            //计算
            paraStoM.w = localw;
            paraStoM.num = miniBatchNum;
            paraStoM.acc=acc;
            paraStoM.sendtime = endTime;
            paraStoM.runtime = runtime;
            MClient.clientHandler.sendMsg(paraStoM);

        }

        System.out.println("\n\nAccuracy:"+Accuracy);

    }
}
