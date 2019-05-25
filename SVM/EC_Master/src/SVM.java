import java.util.ArrayList;


public class SVM {

    public static float[] w;
    float[] getW(){return w;}
    public static ArrayList<float[]> w_list;
    public ArrayList<float[]> getW_list() { return w_list; }
    public int arrNum;

    // Global update synchronously
    SVM(ArrayList<float[]> w1, ArrayList<float[]> w2, ArrayList<float[]> w3, int num1, int num2, int num3){
        w_list=new ArrayList<>();

        for(int j=0;j<w1.size();j++) {
            w = new float[w1.get(j).length];
            int num = num1 + num2 + num3;

            for (int i = 0; i < w1.get(j).length; i++) {
                float sum = 0;
                sum = w1.get(j)[i] * num1 + w2.get(j)[i] * num2 + w3.get(j)[i] * num3;
                w[i] = sum / num;
            }
            w_list.add(w);
        }
    }

    // Global update asynchronously
    SVM(ArrayList<float[]> w1, ArrayList<float[]> w2, int num1, int num2) {
        w_list=new ArrayList<>();

        for(int j=0;j<w1.size();j++) {
            w = new float[w1.get(j).length];

            for (int i = 0; i < w1.get(j).length; i++) {
                float sum = 0;
                ArrayList<Double> tmp = new ArrayList<Double>();
                float num = num1 + num2;
                arrNum = (num1 + num2) / 2;

                sum = w1.get(j)[i] * num1 + w2.get(j)[i] * num2;
                w[i] = sum / num;

            }
            w_list.add(w);
        }


    }
}
