import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class KMeans {

    public static List<ArrayList<ArrayList<Double>>> initHelpCenterList(List<ArrayList<ArrayList<Double>>> helpCenterList, int k){
        for (int i=0; i<k; i++){
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        return helpCenterList;
    }

    public static List<ArrayList<Double>> centers = new ArrayList<>();
    public static List<ArrayList<Double>> newCenters = new ArrayList<>();
    public static List<ArrayList<ArrayList<Double>>> helpCenterList = new ArrayList<>();
    public static List<ArrayList<Double>> dataList = new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;

    public List<ArrayList<Double>> getCenter() { return centers; }

    //获得质心，用于传给云端
    public List<ArrayList<Double>> getNewCenter() { return newCenters; }

    //获得各簇的点集，用于计算个数，传给云端
    public List<ArrayList<ArrayList<Double>>> getHelpCenterList() { return helpCenterList; }

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

    public KMeans(){
    }

    public void  readData(String filepath){
        try {
            allDataList = Data(filepath);
//            dataList = Data();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //////////////////////////dataList实时更新////////////////////////////
    //返回本次datalist的大小，0代表没有数据
    // stepLength: 10
    // step:   0   1    2   3   4
    // begin:  0   10  20   30
    // end:    10  20  30   40
    // [begin, end)  [0,10)  [10,20) ....
    public static int updateDataList(int step, int stepLength){
        int begin = step * stepLength;
        int end = begin + stepLength;
        if( begin > allDataList.size()){
            return 0;
        }
        else{
            end = ( end <= allDataList.size()) ? end : allDataList.size();
            dataList.clear();
            for( int i = begin; i < end; i++){
                dataList.add(new ArrayList<>(allDataList.get(i)));//拷贝 [begin, end)
            }
            //System.out.println("dataList: " + dataList);
            return end - begin;
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

    //初始簇中心
    public KMeans(int k) throws IOException {
        //随机确定k个初始聚类中心
        Random rd = new Random();
        System.out.println("Random Center' index");
        for (int i=0; i<k; i++){
            int index = rd.nextInt(dataList.size());
            System.out.println("index: " + index);
            centers.add(dataList.get(index));
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }

        //输出k个初始中心
        System.out.println("Slave01初始簇中心： ");
        for (int i=0; i<k; i++){
            System.out.println(centers.get(i));
        }
    }

    public KMeans(int k, int times, List<ArrayList<Double>> centers ,int miniBatchNum) throws IOException {
        int time = 0;

        //进行多次迭代，直到聚类中心稳定
        while (true) {
            time ++;
            KMeans.updateDataListRandom(miniBatchNum);

            newCenters = new ArrayList<ArrayList<Double>>();
            helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
            helpCenterList = initHelpCenterList(helpCenterList, k);

            //标注每一条记录所属于的中心
            for (int i=0; i<dataList.size(); i++) {
                double minDistance = 999999999;
                int centerIndex = -1;
                //距离0-k哪个中心最近
                for (int j=0; j<k; j++) {
                    double currentDistance = 0;
                    //计算两点之间的欧式距离
                    for (int t=0; t<centers.get(0).size(); t++){
                        currentDistance += (centers.get(j).get(t)-dataList.get(i).get(t)) * (centers.get(j).get(t)-dataList.get(i).get(t));
                    }
                    currentDistance=Math.sqrt(currentDistance);
                    if (minDistance > currentDistance){
                        minDistance = currentDistance;
                        centerIndex = j;
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

            //判断每个簇都有样本，如果没有，则在SSE最大的簇中随机选一个点赋值，防止为NAN
            for( int i = 0; i < k; i++ ) {
                if (0 == helpCenterList.get(i).size()) {
                    SSE sse=new SSE(newCenters,helpCenterList);
                    int j = sse.getClusterIdWithMaxSSE();
                    Random r = new Random();
                    int n=r.nextInt(helpCenterList.get(j).size());
                    newCenters.set(i,helpCenterList.get(j).get(n));
                    System.out.println("检测到NAN值"+i+"，并替换！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
                }
            }


            //大于时间阈值时，结束循环
            if (time > times){
                break;
            }
            //否则，新的中心将代替旧的中心，进行下一次迭代
            else {
                centers = new ArrayList<ArrayList<Double>>(newCenters);
            }
        }

        System.out.println("\nSlave01新的簇中心： \n");
        for (int i=0; i<k; i++) {
            System.out.println(newCenters.get(i));
        }
    }
}

