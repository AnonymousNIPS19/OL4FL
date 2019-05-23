import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class random extends Bandit {

    List<Integer> actions = new ArrayList<>();
    int counts[] = {0,0,0,0,0,0};
    double regret = 0;
    int oldArm = 0;
    List<Double> regrets = new ArrayList<>();
    int i=0;

    public int allCPU = 10000;
    public int newcpu;
    public int newio;

    public boolean isResourceEnough(){
        if( allCPU> (newcpu+newio)){
            return true;
        }
        return false;
    }

    public int mab(int num_steps) {
        Random r=new Random();
        for (int j=0; j<num_steps; j++) {
            i = r.nextInt(K-1);
            allCPU=allCPU-newcpu-newio;
            counts[i] = counts[i] + 1;
            System.out.println("Counts: " + Arrays.toString(counts));
            actions.add(i);
            System.out.println("Actions: " + actions);
            regret = regret + probas[i] - best_probas();
            System.out.println("Regret: " + regret);
            regrets.add(regret);
            System.out.println("Regrets: " + regrets);
        }
        return i;
    }

}