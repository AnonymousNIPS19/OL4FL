import java.util.ArrayList;
import java.util.List;

//计算簇内误差平方和(SSE)，在最分散的一簇中随机选择一点作为空簇的簇中心继续迭代，减少空簇的影响
public class SSE {

    int clusterIdWithMaxSSE;//SSE值最大的簇号
    int getClusterIdWithMaxSSE(){
        return clusterIdWithMaxSSE;
    }

    //计算两点之间的欧式距离
    private double distance(ArrayList<Double> v1, ArrayList<Double> v2){
        double dis=0;
        for(int j=0;j<v1.size();j++) {
            dis += (v1.get(j) - v2.get(j)) * (v1.get(j) - v2.get(j));
        }
        dis=Math.sqrt(dis);
        return dis;
    }

    //找出SSE值最大的簇
    SSE(List<ArrayList<Double>> centers, List<ArrayList<ArrayList<Double>>> helpCenterList) {
        double maxSSE = 0.0;
        clusterIdWithMaxSSE = -1;
        for(int i=0;i<centers.size();i++) {
            double sse = computeSSE(centers.get(i), helpCenterList.get(i)); // 计算一个簇的SSE值
            if(sse > maxSSE) {
                maxSSE = sse;
                clusterIdWithMaxSSE = i;
            }

        }
        System.out.println("\nclusterIdWithMaxSSE:"+clusterIdWithMaxSSE);
    }

    // 计算一个簇的SSE值
    private double computeSSE(ArrayList<Double> center, List<ArrayList<Double>> helpCenterList) { // 计算某个簇的SSE
        double sse = 0.0;
        for(int i=0;i<helpCenterList.size();i++) {
            double distance = distance(center,helpCenterList.get(i));
            sse += distance * distance;
        }
//        System.out.println("\nsse:"+sse);
        return sse;
    }
}
