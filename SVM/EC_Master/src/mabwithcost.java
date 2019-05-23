import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mabwithcost extends Bandit {
    List<Integer> actions=new ArrayList<Integer>();
    int t=0;
    double estimates[]={1,1,1,1,1,1};
    int counts[]={0,0,0,0,0,1};
    int oldArm = 5;
    double regret = 0;
    List<Double> regrets = new ArrayList<>();

    private double cost[]=new double[K];
    public double io[]=new double[K];
    public double cpu[]=new double[K];

    double avgcost[]=new double[K];

    public int allCPU = 10000;
    public int newcpu;
    public int newio;
    double mincost=0;
    double minio=0;
    double mincpu=0;
    public double[] density = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};



    public boolean isResourceEnough(){
        if( allCPU> (minio+mincpu)){
            return true;
        }
        return false;
    }


    public int mab(int num_steps){
        int i =0;
        for (int j=0; j<num_steps; j++){
            i = run_one_step();
            counts[i] = counts[i] + 1;
            System.out.println("Counts: " + Arrays.toString(counts));
            actions.add(i);
            System.out.println("Actions: " + actions);
            regret = regret + probas[i] - best_probas();
            System.out.println("Regret: " + regret);
            regrets.add(regret);
            System.out.println("Regrets: " + regrets);
            //update_regret(i);

//            try {
//                // 准备文件666.txt其中的内容是空的
//                File f1 = new File("C:\\Users\\HanQing\\IdeaProjects\\EC_Master\\src\\666.txt");
//                if (f1.exists()==false){
//                    f1.getParentFile().mkdirs();
//                }
//                // 准备长度是2的字节数组，用88,89初始化，其对应的字符分别是X,Y
//                //byte data[] = {88,89};
//                // 创建基于文件的输出流
//                FileOutputStream fos = new FileOutputStream(f1);
//                // 把数据写入到输出流
//                fos.write();
//                // 关闭输出流
//                fos.close();
//                System.out.println("输入完成");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
        return i;
    }


    int run_one_step() {
        t=t+1;
        int i = 0;
//        float newcost = (float) Math.random() * 5 + 8;
        System.out.println("newio:" + newio);
        System.out.println("newcpu:" + newcpu);
        io[oldArm]=(double)newio;
        cpu[oldArm]=(double)newcpu;
        cost[oldArm] = cost[oldArm] + io[oldArm]/cpu[oldArm];
        System.out.println("cost[oldArm]:" +cost[oldArm]);
        allCPU=allCPU-newcpu-newio;
//        allIO=allIO-newio;
        System.out.println("allCPU:" + allCPU);
//        System.out.println("allIO:" + allIO);

        if(t<K){
            i = t-1;
            System.out.println("cost:" + Arrays.toString(cost));

        }
        else {
            double x[] = new double[K];

            minio=Arrays.stream(io).min().getAsDouble();
            mincpu=Arrays.stream(cpu).min().getAsDouble();
            System.out.println("minio:" + minio);
            System.out.println("mincpu:" + mincpu);
            for (int j = 0; j < K; j++) {
                avgcost[j] = cost[j] / (counts[j] + 1);
                density[j] = estimates[j] / avgcost[j];
            }
            for (int j = 0; j < K; j++) {
                mincost=Arrays.stream(avgcost).min().getAsDouble();
                double log = 2 * Math.log(t);
                double count = Math.sqrt(log / (1 + counts[j]));
                double count1=(1 + 1 / mincost) * count;
//                x[j] =  (count1 / (mincost - count))-density[j] ;
                x[j] = density[j] + (1/mincost)*(1+1/(mincost-count))*count;
            }
            System.out.println("cost:" + Arrays.toString(cost));
            System.out.println("avgcost:" + Arrays.toString(avgcost));
            System.out.println("prices:" + Arrays.toString(density));
            System.out.println("xi:" + Arrays.toString(x));

            for (int j = 0; j < x.length; j++) {
                if (x[j] > x[i]) {
                    i = j;
                }
            }
        }

        oldArm = i;

        System.out.println("i:"+i);


        return i;
    }


    public void updateEstimate(){
        double r = ucb_generate_reward(oldArm);
        System.out.println("Regret: " + regret);
        System.out.println("r: " + r);
        //double y = (r - estimates[i]) / (counts[i] + 1);
        estimates[oldArm] = (r + estimates[oldArm] * counts[oldArm]) / (counts[oldArm] + 1);

        System.out.println("Estimates: " + Arrays.toString(estimates));
        System.out.println("r: " + r);
        System.out.println("t: " + t);
    }

}
