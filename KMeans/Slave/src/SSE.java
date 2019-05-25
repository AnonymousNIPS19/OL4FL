import java.util.ArrayList;
import java.util.List;

public class SSE {

    int clusterIdWithMaxSSE;
    int getClusterIdWithMaxSSE(){
        return clusterIdWithMaxSSE;
    }

    private double distance(ArrayList<Double> v1, ArrayList<Double> v2){
        double dis=0;
        for(int j=0;j<v1.size();j++) {
            dis += (v1.get(j) - v2.get(j)) * (v1.get(j) - v2.get(j));
        }
        dis=Math.sqrt(dis);
        return dis;
    }

    SSE(List<ArrayList<Double>> centers, List<ArrayList<ArrayList<Double>>> helpCenterList) {
        double maxSSE = 0.0;
        clusterIdWithMaxSSE = -1;
        for(int i=0;i<centers.size();i++) {
            double sse = computeSSE(centers.get(i), helpCenterList.get(i));
            if(sse > maxSSE) {
                maxSSE = sse;
                clusterIdWithMaxSSE = i;
            }
        }
    }

    private double computeSSE(ArrayList<Double> center, List<ArrayList<Double>> helpCenterList) {
        double sse = 0.0;
        for(int i=0;i<helpCenterList.size();i++) {
            double distance = distance(center,helpCenterList.get(i));
            sse += distance * distance;
        }
        return sse;
    }
}
