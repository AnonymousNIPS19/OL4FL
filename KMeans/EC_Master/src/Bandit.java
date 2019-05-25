import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class Bandit {

    // Number of arms
    int K = 6;
    int probas[] = {15,13,10,7,5,3};

    int best_probas(){
        //int best_probas = Arrays.stream(probas).max().getAsInt();
        int best_probas = Arrays.stream(probas).min().getAsInt();
        System.out.println("Best_probas: " + best_probas);
        return best_probas;
    }

    int generate_reward(int i){
        Random rd = new Random();
        double precision = 0.0;
//        if (i == 2){
//            precision = rd.nextInt(4)+0;
//        }
//        else if (i == 1){
//            precision = rd.nextInt(2)+3;
//        }
//        else if (i == 0){
//            precision = rd.nextInt(4)+3;
//        }
        precision = F1score.f1;
        System.out.println("Precision: " + precision);
        //if(precision<probas[i])
        if(precision < 1.5)
            return 1;
        else return 0;
    }

    double ucb_generate_reward(int i){
        //Random rd = new Random();
        double precision = 0.0;
        precision = F1score.f1;
        System.out.println("Precision: " + precision);
        return precision;
    }

    static double regret = 0;
    List<Double> regrets = new ArrayList<>();

    void update_regret(int i){
        //regret = regret + best_probas() - probas[i];
        regret = regret + probas[i] - best_probas();
        regrets.add(regret);
        System.out.println("Regrets: " + regrets);
    }
}
