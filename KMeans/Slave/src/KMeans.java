import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class KMeans {

    public static List<ArrayList<Double>> centers = new ArrayList<>();
    public List<ArrayList<Double>> getCenter() { return centers; }

    // Local parameter
    public static List<ArrayList<Double>> newCenters = new ArrayList<>();
    public List<ArrayList<Double>> getNewCenter() { return newCenters; }

    // Raw data for each cluster
    public static List<ArrayList<ArrayList<Double>>> helpCenterList = new ArrayList<>();
    public List<ArrayList<ArrayList<Double>>> getHelpCenterList() { return helpCenterList; }
    public static List<ArrayList<ArrayList<Double>>> initHelpCenterList(List<ArrayList<ArrayList<Double>>> helpCenterList, int k){
        for (int i=0; i<k; i++){
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        return helpCenterList;
    }

    public static List<ArrayList<Double>> dataList = new ArrayList<>();
    public static List<ArrayList<Double>> allDataList;


    // Read data
    public List<ArrayList<Double>> Data(String filepath) throws IOException {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get minibatch sequentially
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
                dataList.add(new ArrayList<>(allDataList.get(i)));
            }
            return end - begin;
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


    public KMeans(int k) throws IOException {
        Random rd = new Random();
        for (int i=0; i<k; i++){
            int index = rd.nextInt(dataList.size());
            centers.add(dataList.get(index));
            helpCenterList.add(new ArrayList<ArrayList<Double>>());
        }
        for (int i=0; i<k; i++){
            System.out.println(centers.get(i));
        }
    }

    public KMeans(int k, int times, List<ArrayList<Double>> centers ,int miniBatchNum) throws IOException {
        int time = 0;

        while (true) {
            time ++;

            // Local iteration by different minibatch
            KMeans.updateDataListRandom(miniBatchNum);

            newCenters = new ArrayList<ArrayList<Double>>();
            helpCenterList = new ArrayList<ArrayList<ArrayList<Double>>>();
            helpCenterList = initHelpCenterList(helpCenterList, k);

            for (int i=0; i<dataList.size(); i++) {
                double minDistance = 999999999;
                int centerIndex = -1;
                for (int j=0; j<k; j++) {
                    double currentDistance = 0;
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

            // For SSE
            for( int i = 0; i < k; i++ ) {
                if (0 == helpCenterList.get(i).size()) {
                    SSE sse=new SSE(newCenters,helpCenterList);
                    int j = sse.getClusterIdWithMaxSSE();
                    Random r = new Random();
                    int n=r.nextInt(helpCenterList.get(j).size());
                    newCenters.set(i,helpCenterList.get(j).get(n));
                }
            }

            if (time > times){
                break;
            }
            else {
                centers = new ArrayList<ArrayList<Double>>(newCenters);
            }
        }

        // Print new parameter
        for (int i=0; i<k; i++) {
            System.out.println(newCenters.get(i));
        }
    }
}

