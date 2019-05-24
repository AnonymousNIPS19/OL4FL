import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

//计算局部DBI
class DBI{
    public static double dbi = 0; //聚类后计算DBI时引用
    public static double Dbi = 0; //main函数中引用
    public static List<ArrayList<Double>> dataList = new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;

    public static List<ArrayList<ArrayList<Double>>> initHelpCenterList(List<ArrayList<ArrayList<Double>>> helpCenterList,int k){
        for(int i=0;i<k;i++){
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        return helpCenterList;
    }

    //读取数据
    public List<ArrayList<Double>> Data(String filepath) throws IOException {
        //读入原始数据
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filepath)));
        String data = null;
        List<ArrayList<Double>> dataList = new ArrayList<>();
        while ((data = br.readLine()) != null){
            String []fileds = data.split(",");
            List<Double> tmpList = new ArrayList<>();
            for (int i=0; i<fileds.length-1; i++){
                tmpList.add(Double.parseDouble(fileds[i]));
            }
            dataList.add((ArrayList<Double>) tmpList);
        }
        br.close();
        return dataList;
    }

    public DBI(){
    }

    public void  readData(String filepath){
        try {
            allDataList = Data(filepath);
//            dataList = Data();
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
//        ArrayList<Integer> index=new ArrayList<>();
        while(true){
            int k = rand.nextInt(allDataList.size());
            set.add(k);
            if(set.size()==miniBatchNum){
                break;
            }
        }
        for(int i : set){
            dataList.add(new ArrayList<>(allDataList.get(i)));
        }


        return miniBatchNum;
    }

    //利用测试集进行一次聚类，计算DBI
    DBI(int k, List<ArrayList<Double>> centers ,int miniBatchNum) throws IOException {

        DBI.updateDataListRandom(miniBatchNum);

        List<ArrayList<ArrayList<Double>>> helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
        helpCenterList = initHelpCenterList(helpCenterList, k);
        List<ArrayList<Double>> newCenters = new ArrayList<ArrayList<Double>>();

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
                currentDistance=Math.sqrt(currentDistance);
                if(minDistance>currentDistance){
                    minDistance=currentDistance;
                    centerIndex=j;
                }
            }
            helpCenterList.get(centerIndex).add(dataList.get(i));
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

        //计算DBI
        Dbi=computeDBI(newCenters,helpCenterList);

    }

    private double distance(ArrayList<Double> v1, ArrayList<Double> v2){
        double dis=0;
        for(int j=0; j<v1.size(); j++) {
            dis += (v1.get(j) - v2.get(j)) * (v1.get(j) - v2.get(j));
        }
        dis = Math.sqrt(dis);
        return dis;
    }

    private double Si(int i, List<ArrayList<ArrayList<Double>>> helpcenter, List<ArrayList<Double>> center){
        double s=0;
        for(int j=0; j<helpcenter.get(i).size(); j++) {
            s += distance(center.get(i), helpcenter.get(i).get(j));
        }
        s = s/helpcenter.get(i).size();
        return s;
    }

    private double Rij(int i, int j, List<ArrayList<Double>> center, List<ArrayList<ArrayList<Double>>> helpcenter){
        double Mij = distance(center.get(i), center.get(j));
        double Rij = (Si(i,helpcenter,center) + Si(j,helpcenter,center)) / Mij;
        return Rij;
    }

    private double Di(int i, List<ArrayList<Double>> center, List<ArrayList<ArrayList<Double>>> helpcenter){
        List list = new ArrayList();
        for(int j=0; j<center.size(); j++){
            if (i != j){
                double temp = Rij(i, j, center,helpcenter);
                list.add(temp);
            }
        }
        double max = (double) Collections.max(list);
        return max;
    }

    private double computeDBI(List<ArrayList<Double>> center, List<ArrayList<ArrayList<Double>>> helpcenter){
        double di=0;
        for (int i=0; i<center.size(); i++){
            di += Di(i, center, helpcenter);
        }
        dbi = di / center.size();
        if ( Double.isNaN(dbi) ){
            dbi = 50;
        }
        return dbi;
    }

}