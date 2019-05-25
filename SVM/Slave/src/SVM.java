import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class SVM {

    private int exampleNum;

    private int exampleDim;

    private double lambda=0.001;

    private double eta = 0.001;

    private double threshold = 36000;

    private double cost;

    private double[] yp;

    public static float[] new_w;
    public float[] getNew_w() { return new_w; }

    public static double[] grad;
    public double[] getGrad(){return grad;}

    public static ArrayList<Double> y=new ArrayList<>();
    public static ArrayList<ArrayList<Double>> X=new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;

    public static ArrayList<float[]> w_list;
    public ArrayList<float[]> getW_list() { return w_list; }

    public SVM() {

    }


    // Gradient descent implements SVM
    private void CostAndGrad(ArrayList<ArrayList<Double>> X,ArrayList<Double> y,float[] w) {

        cost =0;

        for(int m=0;m<exampleNum;m++) {

            yp[m]=0;

            for(int d=0;d<exampleDim;d++) {

                yp[m]+=X.get(m).get(d)*w[d];

            }



            if(y.get(m)*yp[m]-1<0) {

                cost += (1-y.get(m)*yp[m]);

            }

        }

        cost=cost/exampleNum;

        for(int d=0;d<exampleDim;d++) {

            cost += 0.5*lambda*w[d]*w[d];

        }



        for(int d=0;d<exampleDim;d++) {

            grad[d] = Math.abs(lambda*w[d]);

            for(int m=0;m<exampleNum;m++) {

                if(y.get(m)*yp[m]-1<0) {

                    grad[d]-= y.get(m)*X.get(m).get(d);

                }

            }

        }

    }



    private void update(float[] w) {

        for(int d=0;d<exampleDim;d++) {

            w[d] -= eta*grad[d];

        }
        new_w=w;

    }



    public SVM(int maxIters,ArrayList<float[]> w, int miniBatchNum) {

        exampleNum = miniBatchNum;

        if(exampleNum <=0) {

            System.out.println("num of example <=0!");

            return;

        }

        exampleDim = allDataList.get(0).size()-1;
        System.out.println("exampleDim+"+exampleDim);

        grad = new double[exampleDim];

        yp = new double[exampleNum];

        w_list=new ArrayList<>();


        for (int flag = 0; flag < 10; flag++) {

            for(int iter=0;iter<maxIters;iter++) {

                SVM.updateDataListRandom(miniBatchNum);

                ArrayList<Double> Y=new ArrayList<>();

                for(int j=0; j<y.size();j++) {
                    if (y.get(j) == flag) {
                        Y.add((double) 1);
                    }
                    else Y.add((double) -1);
                }

                CostAndGrad(X, Y, w.get(flag));

                update(w.get(flag));

            }
            w_list.add(new_w);

        }

    }



    public List<ArrayList<Double>> loadData(String filepath) throws IOException

    {
        BufferedReader br=new BufferedReader(new InputStreamReader
                (new FileInputStream(filepath)));
        List<ArrayList<Double>> dataList = new ArrayList<>();
        String data = null;
        while ((data = br.readLine()) != null){
            String []fileds = data.split("\\|");
            List<Double> tmpList = new ArrayList<>();
            for (int i=0; i<fileds.length; i++){
                tmpList.add(Double.parseDouble(fileds[i]));
            }
            dataList.add((ArrayList<Double>) tmpList);
        }
        br.close();
        return dataList;

    }

    public void  readData(String filepath){
        try {
            allDataList = loadData(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int updateDataListRandom(int miniBatchNum){
        if( miniBatchNum > allDataList.size() ){
            return 0;
        }
        Set<Integer> set = new HashSet<Integer>();

        Random rand = new Random();
        X.clear();
        y.clear();
        while(true){
            int k = rand.nextInt(allDataList.size());
            set.add(k);
            if(set.size()==miniBatchNum){
                break;
            }
        }
        for(int i : set){
            ArrayList<Double> tmpList = new ArrayList<Double>();
            for(int j=0; j<allDataList.get(i).size()-1;j++)
                tmpList.add(allDataList.get(i).get(j));
            y.add(allDataList.get(i).get(allDataList.get(i).size()-1));
            X.add(tmpList);
        }

        System.out.println("set: " + set);
        return miniBatchNum;
    }

}