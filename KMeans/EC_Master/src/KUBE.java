import utils.MultinomialDistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KUBE extends Bandit {

    List<Integer> actions = new ArrayList<>();
    int t = 0;
    double estimates[] = {1,1,1,1,1,1};
    int counts[] = {0,0,0,0,0,0};
    int oldArm = 0;
    double regret = 0;
    List<Double> regrets = new ArrayList<>();

    // “同步 同质”实验
    // 本地迭代一次与全局聚合一次所需的资源数（定值，三个边缘节点同质）
    public int[] localupdate = {5, 5, 5};
    public int[] globalaggre = {25, 25, 25};
//    ////////////
//    private double[] cost1 = {0.098, 0.125, 0.185, 0.278, 0.833, 1.667};
//    private double[] cost2 = {10.2, 8.0, 5.4, 3.55, 1.2, 0.6};
//    // 每一次拉臂所需的资源数
//    public int[] CPU1 = {255, 200, 135, 90, 30, 15};
//    public int[] IO1 = {25, 25, 25, 25, 25, 25};
//    public int[] CPU2 = {255, 200, 135, 90, 30, 15};
//    public int[] IO2 = {25, 25, 25, 25, 25, 25};
//    public int[] CPU3 = {255, 200, 135, 90, 30, 15};
//    public int[] IO3 = {25, 25, 25, 25, 25, 25};
//    // 三个边缘节点各自的资源约束
//    public int allCPU1 = 1000;
//    public int allIO1 = 300;
//    public int allCPU2 = 1000;
//    public int allIO2 = 300;
//    public int allCPU3 = 1000;
//    public int allIO3 = 300;

    //private double[] cost = {0.019, 0.024, 0.032, 0.047, 0.143, 0.333};
    private double[] cost = {0.098, 0.125, 0.185, 0.278, 0.833, 1.667};
    //private double[] cost = {10.2, 8.0, 5.4, 3.55, 1.2, 0.6};


    public int[] cpu = {51*5, 40*5, 27*5, 18*5, 6*5, 3*5};
    public int[] io = {1*25, 1*25, 1*25, 1*25, 1*25, 1*25};

    public int allCPU = 8500;
    public int allIO = 1500;

    public double[] density = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

    public void init(){

    }

    public boolean isResourceEnough(){
        for( int i = 0; i < K; i++ ){
            if( allIO > io[i] && allCPU > cpu[i] ){
                return true;
            }
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

    int run_one_step(){

        t = t + 1;
        double x[] = new double[K];
        for (int i=0; i<K; i++){
            double log = 2 * Math.log(t);
            double count = Math.sqrt(log / (1 + counts[i]));
            //x[i] = estimates[i] + count;
            x[i] = count - estimates[i];
        }
        System.out.println("x[i]: " + Arrays.toString(x));
        System.out.println("cost[i]: " + Arrays.toString(cost));
        HashMap<Double, Integer> densityIndexMap = new HashMap<>();
        for( int i = 0; i < K; i++ ){

            density[i] = x[i]/cost[i];
            densityIndexMap.put(density[i], i);
        }
        System.out.println("density[i]: " + Arrays.toString(density));
        Arrays.sort(density);//sort
        //reverse density
        for(int i = 0; i < density.length/2; i++){
            double temp = density[i];
            density[i] = density[density.length-1-i];
            density[density.length-1-i] = temp;
        }
        System.out.println("density[i]: " + Arrays.toString(density));
        int[] densityIndex = new int[K];
        for( int i = 0; i < K; i++ ){
            densityIndex[i] = densityIndexMap.get(density[i]);
        }
        /*
            den       den  index      m
        0   0.1       0.6   2
        1   0.3       0.3   1
        2   0.6       0.1   0         50(0.6)
         */
        int[] m = new int[K];
        int sum_m = 0;
        int tempCPU = allCPU;
        int tempIO = allIO;
        for( int i = 0; i < K; i++ ){
            int nio = tempIO/io[densityIndex[i]];
            int ncpu = tempCPU/cpu[densityIndex[i]];
            int n = nio < ncpu ? nio : ncpu;
            m[densityIndex[i]] = n;
            sum_m += n;
            tempCPU = tempCPU - cpu[densityIndex[i]] * n;
            tempIO = tempIO - io[densityIndex[i]] * n;
        }
        double[] prob = new double[K];
        for(int i = 0; i < K; i++){
            prob[i] = 1.0*m[i]/sum_m;
        }
        int arm = MultinomialDistribution.sampleFromMultinomialDistribution(prob);
        oldArm = arm;
        System.out.println("arm: " + arm);
        allCPU = allCPU - cpu[arm];
        allIO = allIO - io[arm];

//        double r = ucb_generate_reward(i);
//        System.out.println("Regret: " + regret);
//        System.out.println("r: " + r);
//        //double y = (r - estimates[i]) / (counts[i] + 1);
//        estimates[i] = (r - estimates[i]) / (counts[i] + 1);
//
//        System.out.println("Estimates: " + Arrays.toString(estimates));
//        System.out.println("r: " + r);
//        System.out.println("t: " + t);
        return arm;
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
