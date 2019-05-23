import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KUBE extends Bandit {
    List<Integer> actions=new ArrayList<Integer>();
    int t=0;
    double estimates[]={1,1,1,1,1,1};
    int counts[]={0,0,0,0,0,0};
    int cost[]={10,8,6,5,4,3};
    int Bt=100;
    double[] prices=new double[K];//性价比
    int[] tags = new int[K];
    double[] p=new double[K];
    int mincost= Arrays.stream(cost).min().getAsInt();

    public int mab(int num_steps){
        int i=0;
        for (int j=0;j<num_steps;j++){
            i=run_one_step();
            if (i!=-1) {
                counts[i] = counts[i] + 1;
                System.out.println("count:" + Arrays.toString(counts));
                actions.add(i);
                System.out.println("actions:" + actions);
                update_regret(i);
            }
            else{
                j=num_steps;
                System.out.println("资源已用完");
            }
        }
        return i;
    }
    int run_one_step() {
        t=t+1;
        int i=0;

        if (Bt < mincost) {
            return -1;
        }
        if(t<=K){
            i=t-1;
        }
        double x[] = new double[K];
        for (int j = 0; j < K; j++) {
            double log = 2 * Math.log(t);
            double count = Math.sqrt(log / (1 + counts[j]));
//            x[j] = estimates[j] + count;
            x[j] = count - estimates[j];
            prices[j] = x[j] / cost[j];
            tags[j] = j;
        }
        System.out.println("xi:"+ Arrays.toString(x));
        for (int t = 0; t < K; t++) {
            for (int j = t + 1; j < K; j++) {
                if (prices[t] < prices[j]) {                    // 交换
                    double temp = prices[t];
                    prices[t] = prices[j];
                    prices[j] = temp;
                    int tag = tags[t];
                    tags[t] = tags[j];
                    tags[j] = tag;
                }
            }
        }
        System.out.println("prices:" + Arrays.toString(prices));
        System.out.println("tags:" + Arrays.toString(tags));
        int bt=Bt;
        int j = 0;
        int[] m = new int[K];
        while (true) {
                if (cost[tags[j]] <= bt) {
                    m[tags[j]]++;
                    bt = bt - cost[tags[j]];
                } else j++;
                if (j >= K) {
                    System.out.println("m:" + Arrays.toString(m));
                    int sum = 0;
                    for (int s = 0; s < K; s++) {
                        sum = sum + m[s];
                    }
                    System.out.println("sum:"+sum);
                    p[0]=(double)m[0]/ sum;
                    for (int s = 1; s < K; s++) {
                        p[s] =p[s-1]+ (double)m[s] / sum;
                    }
                    System.out.println("p:" + Arrays.toString(p));
                    break ;
                }
            }
            Random rd = new Random();
            double randomNumber = rd.nextInt(1);
            for(int s = 0; s < K; s++){
                if(randomNumber<p[s]){
                    i=s;
                    break;
                }
            }

        Bt=Bt-cost[i];
        double r = ucb_generate_reward(i);
        System.out.println("regret:" + regret);
//        double y = (r - estimates[i]) / (counts[i] + 1);
        estimates[i] =(r + estimates[i]*counts[i]) / (counts[i] + 1);
        System.out.println("estimates:" + Arrays.toString(estimates));
        System.out.println("prices:" + Arrays.toString(prices));
        System.out.println("tags:" + Arrays.toString(tags));
        System.out.println("r:" + r);
        System.out.println("t:" + t);
        System.out.println("i:"+i);
        System.out.println("Bt:"+Bt);
        return i;
    }

}
