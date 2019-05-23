import java.io.FileNotFoundException;
import java.util.*;

class F1measure {
    double f1;
    double getf1(){return f1;}

    double[] LineMax(double data[][], int line, int column){
        int i = 0;
        int j = 0;
        System.out.println("\ncolumn:"+ column);
        double[] result=new double[line];
        double max = 0;
        for (i = 0; i < line; i++)    {
            for ( j = 0; j < column; j++)
            {
                if (data[i][j] > max)
                {
                    max = data[i][j];
                }
            }
            result[i] = max;
            max = 0;  // 最大值复位，以免上一次比较照成的错误
        }
        return result;
    }


    F1measure(Map<Integer, Integer> train_target, Map<Integer, Integer> predict_target) throws FileNotFoundException {

        Set<Integer> train_set = new HashSet<Integer>(train_target.values());
        ArrayList<Integer> target = new ArrayList<>(train_set);
        Set<Integer> predict_set = new HashSet<Integer>(predict_target.values());
        ArrayList<Integer> p_target = new ArrayList<>(predict_set);
//        System.out.println("\npredict_count:"+ train_set);
//        System.out.println("\npredict_count:"+ predict_set);


        int[][] TP = new int[train_set.size()][predict_set.size()];

        int index=0;
        Map<Integer, Integer> train_Key = new HashMap<Integer, Integer>();
        Map<Integer, Integer> train_Value = new HashMap<Integer, Integer>();
        for(Map.Entry<Integer, Integer> entry : train_target.entrySet()) {
            //将原来MAP的VALUE放入新的MAP的VALUE里面
            train_Value.put(index, entry.getValue());
            //将原来MAP的KEY放入新的MAP的VALUE 里面
            train_Key.put(index, entry.getKey());
            index++;

        }

        int p_index=0;
        Map<Integer, Integer> predict_Key = new HashMap<Integer, Integer>();
        Map<Integer, Integer> predict_Value = new HashMap<Integer, Integer>();
        for(Map.Entry<Integer, Integer> p_entry : predict_target.entrySet()) {
            //将原来MAP的VALUE放入新的MAP的VALUE里面
            predict_Value.put(p_index, p_entry.getValue());
            //将原来MAP的KEY放入新的MAP的VALUE 里面
            predict_Key.put(p_index, p_entry.getKey());
            p_index++;

        }

        for (int i = 0; i < train_set.size(); i++) {
            for (int j=0;j<predict_set.size();j++) {
                TP[i][j] = 0;
                for(int k=0;k<predict_target.size();k++){
                    if(train_Key.get(k).equals(predict_Key.get(k)) && predict_Value.get(k).equals(p_target.get(j)) && train_Value.get(k).equals(target.get(i))){
                        TP[i][j]++;
                    }
                }
            }

        }

        int[] train_count = new int[train_set.size()];
        int[] predict_count = new int[predict_set.size()];
        for (int i = 0; i < train_set.size(); i++) {
            for (int j = 0; j < train_target.size(); j++) {
                if (train_target.get(j).equals(target.get(i))){
                    train_count[i]++;
                }
            }
        }
        for (int i = 0; i < predict_set.size(); i++) {
            for (int j = 0; j < predict_target.size(); j++) {
                if (predict_target.get(j).equals(p_target.get(i))){
                    predict_count[i]++;
                }
            }
        }
        System.out.println(Arrays.toString(train_count));
        System.out.println(Arrays.toString(predict_count));


//
//        System.out.println("\npredict_count:"+ Arrays.toString(predict_count));
//        System.out.println("\ntrain_count:"+ Arrays.toString(train_count));

        double[][] recall=new double[train_set.size()][predict_set.size()];
        double[][] precision=new double[train_set.size()][predict_set.size()];
        double[][] f1measure=new double[train_set.size()][predict_set.size()];

        for (int i = 0; i < train_set.size(); i++) {
            for (int j = 0; j < predict_set.size(); j++) {
                recall[i][j] = (double) TP[i][j] / train_count[i];
                precision[i][j] = (double) TP[i][j] / predict_count[j];
                if(recall[i][j]+precision[i][j]==0){
                    f1measure[i][j]=0;
                }else {
                    f1measure[i][j] = (2 * recall[i][j] * precision[i][j]) / (recall[i][j] + precision[i][j]);
                }
            }

        }

        double[] f1max;
        f1max=LineMax(f1measure,train_set.size(),predict_set.size());
        f1=0;

        for(int i=0;i<train_set.size();i++){
            f1=f1+f1max[i]*train_count[i];

//            f2=f2+f1max[i];
        }
        f1=f1/train_target.size();

        for (int i = 0; i < train_set.size(); i++) {
            System.out.println("\nf1max:" + f1max[i]);
            for (int j = 0; j < predict_set.size(); j++) {
//                System.out.println("\nTP:" + TP[i][j]);
//                System.out.println("\nrecall:"+ recall[i][j]);
//                System.out.println("\nprecision:"+ precision[i][j]);
//                System.out.println("\nf1measure:"+ f1measure[i][j]);
            }
        }
//        System.out.println("\nf1:"+f1);
//        System.out.println("\nf2:"+f2);



    }


}

