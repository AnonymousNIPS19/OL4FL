import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

class KMeans {
    public static List<ArrayList<ArrayList<Double>>> initHelpCenterList(List<ArrayList<ArrayList<Double>>> helpCenterList,int k){
        for(int i=0;i<k;i++){
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        return helpCenterList;
    }

    private List<ArrayList<Double>> center = new ArrayList<>();
    public static List<ArrayList<Double>> newCenters = new ArrayList<>();
    public static List<ArrayList<ArrayList<Double>>> helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
    public static List<ArrayList<Double>> dataList = new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;
    public int[] arrNum;

    List<ArrayList<Double>> getCenter() {
        return center;
    }

    //获得全局质心，用于传给slave
    List<ArrayList<Double>> getNewCenter() { return newCenters; }

    //获得各簇的点集，用于计算DBI
    List<ArrayList<ArrayList<Double>>> getHelpCenterList(){
        return helpCenterList;
    }

    //f1f1f1f1
    List<ArrayList<ArrayList<Double>>> helpCenterList1 = new ArrayList<ArrayList<ArrayList<Double>>>();
    List<ArrayList<ArrayList<Double>>> getHelpCenterList1(){
        return helpCenterList1;
    }
    public static List<ArrayList<Double>> dataList1 = new ArrayList<>();
    Map<Integer, Integer> train_target = new HashMap<Integer, Integer>();
    Map<Integer, Integer> gettrain_target() throws IOException {
        return train_target;
    }
    Map<Integer, Integer> predict_target = new HashMap<Integer, Integer>();
    Map<Integer, Integer> getpredict_target(){
        return predict_target;
    }



    //读取数据
    private List<ArrayList<Double>> Data() throws IOException {
        //读入原始数据
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

    public static int updateDataListRandom(int miniBatchNum){
        if( miniBatchNum > allDataList.size() ){
            return 0;
        }
        Set<Integer> set = new HashSet<Integer>();

        Random rand = new Random();
        dataList.clear();
        dataList1.clear();
//        ArrayList<Integer> index=new ArrayList<>();
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


        System.out.println("set: " + set);

        return miniBatchNum;
    }

    //同步时计算全局簇中心
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
        System.out.println("\n计算得到的全局簇中心： ");
        for(int i=0; i<k; i++) {
            System.out.println(center.get(i));
        }
    }

    //异步时计算全局簇中心
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
        System.out.println("\n计算得到的全局簇中心： ");
        ///kkk
        for(int i=0; i<k; i++) {
            System.out.println(center.get(i));
        }
    }

    //初始簇中心
    public KMeans(int k,int miniBatchNum) throws IOException {

        KMeans.updateDataListRandom(miniBatchNum);
        //随机确定k个初始聚类中心
        Random rd = new Random();
        System.out.println("Random Center' index");
        for (int i=0; i<k; i++){
            int index = rd.nextInt(dataList.size());
            System.out.println("index: " + index);
            center.add(dataList.get(index));
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }

        //输出k个初始中心
        System.out.println("初始簇中心： ");
        for (int i=0; i<k; i++){
            System.out.println(center.get(i));
        }
    }

    KMeans(int k, List<ArrayList<Double>> centers,int miniBatchNum) throws IOException {
        KMeans.updateDataListRandom(miniBatchNum);

        helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
        helpCenterList = initHelpCenterList(helpCenterList, k);
        newCenters = new ArrayList<ArrayList<Double>>();

        //f1f1f1f
        helpCenterList1 = new ArrayList<ArrayList<ArrayList<Double>>>();
        helpCenterList1 = initHelpCenterList(helpCenterList1, k);
        int num=dataList1.get(0).size()-1;
        for (int i=0;i<dataList1.size();i++){
            double target=dataList1.get(i).get(num);
            train_target.put(i,(int) target) ;
        }

        //标注每一条记录所属的中心
        for(int i=0; i<dataList.size(); i++){
            double minDistance = 999999999;
            int centerIndex = -1;
            //离0-k之间哪个中心最近
            for(int j=0; j<k; j++){
                double currentDistance = 0;
                //计算两点之间的欧式距离
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
            //f1f1f1
            helpCenterList1.get(centerIndex).add(dataList1.get(i));
            predict_target.put(i,centerIndex+1);
        }

        //计算k个新的聚类中心
        for (int i=0; i<k; i++) {
            ArrayList<Double> tmp = new ArrayList<>();
            for (int j=0; j<centers.get(0).size(); j++) { //j=0 用到了第一维的数据
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


