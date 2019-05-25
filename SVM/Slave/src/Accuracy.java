import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class Accuracy {
    public static double acc;
    public double getAcc(){return acc;}
    public static ArrayList<Double> testY=new ArrayList<>();
    public static ArrayList<ArrayList<Double>> testX=new ArrayList<>();
    public static ArrayList<Double> alltestY=new ArrayList<>();
    public static ArrayList<ArrayList<Double>> alltestX=new ArrayList<>();

    public void loadData(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader
                (new FileInputStream(filepath)));
        String data = null;
        while ((data = br.readLine()) != null) {
            String[] fields = data.split("\\|");
            ArrayList<Double> tmpList = new ArrayList<Double>();
            for (int i = 0; i < fields.length - 1; i++)
                tmpList.add(Double.parseDouble(fields[i]));

            alltestY.add(Double.valueOf(fields[fields.length - 1]));
            alltestX.add(tmpList);
        }
        br.close();
    }

    public static int updateDataListRandom(int miniBatchNum){
        if( miniBatchNum > alltestX.size() ){
            return 0;
        }
        Set<Integer> set = new HashSet<Integer>();

        Random rand = new Random();
        testX.clear();
        testY.clear();
        while(true){
            int k = rand.nextInt(alltestX.size());
            set.add(k);
            if(set.size()==miniBatchNum){
                break;
            }
        }
        for(int i : set){
            testX.add(new ArrayList<>(alltestX.get(i)));
            testY.add(alltestY.get(i));
        }

        System.out.println("set: " + set);

        return miniBatchNum;
    }


    private int predict(ArrayList<Double> x,float []w) {

        double pre=0;

        for(int j=0;j<x.size();j++) {

            pre+=x.get(j)*w[j];

        }

        if(pre >=0)

            return 1;

        else return -1;

    }

    public Accuracy(){}

    Accuracy(ArrayList<float[]> w,int miniBatchNum) throws IOException {

        Accuracy.updateDataListRandom(miniBatchNum);

        int right=0;
        for (int flag = 0; flag < 10; flag++) {

            ArrayList<Double> testy=new ArrayList<>();

            for(int j=0; j<testY.size();j++) {
                if (testY.get(j) == flag) {
                    testy.add((double) 1);
                }
                else testy.add((double) -1);
            }

            for (int i = 0; i < testX.size(); i++) {

                if (predict(testX.get(i), w.get(flag)) ==1 && testy.get(i)==1) {

                    right++;

                }

            }
        }

        System.out.println("total:"+testX.size());

        System.out.println("right:"+right);

        acc=(double)right/testX.size();

        System.out.println("acc rate:"+((double)(right)/testX.size()));
    }
}
