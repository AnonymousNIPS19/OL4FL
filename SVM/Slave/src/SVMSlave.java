import communication.mclient.ClientHandler;
import communication.mclient.MClient;
import communication.utils.Para;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class SVMSlave {

    public static void main(String[] args) throws IOException, ClassNotFoundException{

        // Batch size
        int miniBatchNum =500;

        String trainfile="mnist_train1.txt";
        String testfile="mnist_test1.txt";
        SVM readData=new SVM();
        readData.readData(trainfile);
        Accuracy readdata=new Accuracy();
        readdata.loadData(testfile);

        ArrayList<Double> Accuracy = new ArrayList<Double>();
        double acc;

        // Local iteration time consumption
        long runtime;
        List<ArrayList<Double>> oldcenter = new ArrayList<>();
        ArrayList<float[]> localw =new ArrayList<>();

        // Socket communication
        MClient client = new MClient("localhost",8802);
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

            // Receive global parameter and new update interval
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

            // Update local parameter
            long startTime = System.currentTimeMillis();
            SVM Svm = new SVM(time, globalw,miniBatchNum);
            localw = Svm.getW_list();

            // Compute Acc
            Accuracy test = new Accuracy(localw,miniBatchNum);
            acc = test.getAcc();
            Accuracy.add(acc);


            long endTime = System.currentTimeMillis();
            runtime=endTime - startTime;

            // Slave send parameter to master
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
