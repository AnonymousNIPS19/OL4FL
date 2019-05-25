import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class KMeans {

    private List<ArrayList<Double>> center = new ArrayList<>();
    List<ArrayList<Double>> getCenter() {
        return center;
    }

    // Local parameter
    public static List<ArrayList<Double>> newCenters = new ArrayList<>();
    List<ArrayList<Double>> getNewCenter() { return newCenters; }

    // Raw data for each cluster
    public static List<ArrayList<ArrayList<Double>>> helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
    List<ArrayList<ArrayList<Double>>> getHelpCenterList(){
        return helpCenterList;
    }
    public static List<ArrayList<ArrayList<Double>>> initHelpCenterList(List<ArrayList<ArrayList<Double>>> helpCenterList,int k){
        for(int i=0;i<k;i++){
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        return helpCenterList;
    }
    public static List<ArrayList<Double>> dataList = new ArrayList<>();
    public static List<ArrayList<Double>> dataList1 = new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;
    public int[] arrNum;

    // F1-score
    List<ArrayList<ArrayList<Double>>> helpCenterList1 = new ArrayList<ArrayList<ArrayList<Double>>>();
    List<ArrayList<ArrayList<Double>>> getHelpCenterList1(){
        return helpCenterList1;
    }
    Map<Integer, Integer> train_target = new HashMap<Integer, Integer>();
    Map<Integer, Integer> gettrain_target() throws IOException {
        return train_target;
    }
    Map<Integer, Integer> predict_target = new HashMap<Integer, Integer>();
    Map<Integer, Integer> getpredict_target(){
        return predict_target;
    }


    // Read data
    private List<ArrayList<Double>> Data() throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader
                (new FileInputStream("kdd_test.txt")));
        String data = null;
        List<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();
        while((data=br.readLine())!=null){
            String []fields = data.split(",");
            List<Double> tmpList = new ArrayList<Double>();
            for(int i=0; i<fields.length;i++)
                tmpList.add(Double.parseDouble(fields[i]));
            dataList.add((ArrayList<Double>) tmpList);
        }
        br.close();
        return dataList;
    }


    public KMeans(){
    }

    public void  readData(){
        try {
            allDataList = Data();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get minibatch randomly
    public static int updateDataListRandom(int miniBatchNum){
        if( miniBatchNum > allDataList.size() ){
            return 0;
        }
        Set<Integer> set = new HashSet<Integer>();

        Random rand = new Random();
        dataList.clear();
        dataList1.clear();
        while(true){
            int k = rand.nextInt(allDataList.size());
            set.add(k);
            if(set.size()==miniBatchNum){
                break;
            }
        }
        for(int i : set){
            dataList1.add(new ArrayList<>(allDataList.get(i)));
            ArrayList<Double> tmpList = new ArrayList<Double>();
            for(int j=0; j<allDataList.get(i).size()-1; j++)
                tmpList.add(allDataList.get(i).get(j));
            dataList.add(tmpList);
        }
        return miniBatchNum;
    }

    // Global update synchronously
    KMeans(List<ArrayList<Double>> center1, List<ArrayList<Double>> center2, List<ArrayList<Double>> center3,
           int[] num1, int[] num2, int[] num3,int k){

        for(int i=0; i<k; i++) {
            double sum = 0;
            ArrayList<Double> tmp = new ArrayList<Double>();
            double num = num1[i] + num2[i] + num3[i];
            for (int j = 0; j < center1.get(0).size(); j++) {
                sum = center1.get(i).get(j) * num1[i] + center2.get(i).get(j) * num2[i] + center3.get(i).get(j) * num3[i];
                tmp.add(sum/num);
            }
            center.add(tmp);
        }
        for(int i=0; i<k; i++) {
            System.out.println(center.get(i));
        }
    }

    // Global update asynchronously
    public void kmeans(List<ArrayList<Double>> center1, List<ArrayList<Double>> center2,
           int[] num1, int[] num2, int k){
        center.clear();
        arrNum = new int[k];
        for(int i=0; i<k; i++) {
            double sum = 0;
            ArrayList<Double> tmp = new ArrayList<Double>();
            double num = num1[i] + num2[i];
            arrNum[i] = (num1[i] + num2[i])/2;
            for (int j = 0; j < center1.get(0).size(); j++) {
                sum = center1.get(i).get(j) * num1[i] + center2.get(i).get(j) * num2[i];
                tmp.add(sum/num);
            }
            center.add(tmp);
        }

        for(int i=0; i<k; i++) {
            System.out.println(center.get(i));
        }
    }

    public KMeans(int k,int miniBatchNum) throws IOException {

        KMeans.updateDataListRandom(miniBatchNum);
        Random rd = new Random();
        System.out.println("Random Center' index");
        for (int i=0; i<k; i++){
            int index = rd.nextInt(dataList.size());
            System.out.println("index: " + index);
            center.add(dataList.get(index));
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        for (int i=0; i<k; i++){
            System.out.println(center.get(i));
        }
    }

    KMeans(int k, List<ArrayList<Double>> centers,int miniBatchNum) throws IOException {

        KMeans.updateDataListRandom(miniBatchNum);

        helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
        helpCenterList = initHelpCenterList(helpCenterList, k);
        newCenters = new ArrayList<ArrayList<Double>>();

        // F1-score
        helpCenterList1 = new ArrayList<ArrayList<ArrayList<Double>>>();
        helpCenterList1 = initHelpCenterList(helpCenterList1, k);
        int num=dataList1.get(0).size()-1;
        for (int i=0;i<dataList1.size();i++){
            double target=dataList1.get(i).get(num);
            train_target.put(i,(int) target) ;
        }

        for(int i=0; i<dataList.size(); i++){
            double minDistance = 999999999;
            int centerIndex = -1;
            for(int j=0; j<k; j++){
                double currentDistance = 0;
                for (int t=0; t<centers.get(0).size(); t++){
                    currentDistance += (centers.get(j).get(t)-dataList.get(i).get(t)) * (centers.get(j).get(t)-dataList.get(i).get(t));
                }
                currentDistance= (double) Math.sqrt(currentDistance);
                if(minDistance>currentDistance){
                    minDistance=currentDistance;
                    centerIndex=j;
                }
            }
            helpCenterList.get(centerIndex).add(dataList.get(i));
            helpCenterList1.get(centerIndex).add(dataList1.get(i));
            predict_target.put(i,centerIndex+1);
        }

        for (int i=0; i<k; i++) {
            ArrayList<Double> tmp = new ArrayList<>();
            for (int j=0; j<centers.get(0).size(); j++) {
                double sum = 0;
                for (int t=0; t<helpCenterList.get(i).size(); t++){
                    sum += helpCenterList.get(i).get(t).get(j);
                }
                tmp.add( sum / helpCenterList.get(i).size());
            }
            newCenters.add(tmp);
        }
    }
}


