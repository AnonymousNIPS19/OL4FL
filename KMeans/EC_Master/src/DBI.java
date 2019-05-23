//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//
//class DBI {
//    public static float dbi = 0;
//
//    private float distance(ArrayList<Float> v1, ArrayList<Float> v2){
//        float dis=0;
//        for(int j=0; j<v1.size(); j++) {
//            dis += (v1.get(j) - v2.get(j)) * (v1.get(j) - v2.get(j));
//        }
//        dis = (float) Math.sqrt(dis);
//        return dis;
//    }
//
//    private float Si(int i, List<ArrayList<ArrayList<Float>>> helpcenter, List<ArrayList<Float>> center){
//        float s=0;
//        for(int j=0; j<helpcenter.get(i).size(); j++) {
//            s += distance(center.get(i), helpcenter.get(i).get(j));
//        }
//        s = s/helpcenter.get(i).size();
//        return s;
//    }
//
//    private float Rij(int i, int j, List<ArrayList<Float>> center, List<ArrayList<ArrayList<Float>>> helpcenter){
//        float Mij = distance(center.get(i), center.get(j));
//        float Rij = (Si(i,helpcenter,center) + Si(j,helpcenter,center)) / Mij;
//        return Rij;
//    }
//
//    private float Di(int i, List<ArrayList<Float>> center, List<ArrayList<ArrayList<Float>>> helpcenter){
//        List list = new ArrayList();
//        for(int j=0; j<center.size(); j++){
//            if (i != j){
//                float temp = Rij(i, j, center,helpcenter);
//                list.add(temp);
//            }
//        }
//        float max = (float) Collections.max(list);
//        return max;
//    }
//
//    DBI(List<ArrayList<Float>> center, List<ArrayList<ArrayList<Float>>> helpcenter){
//        float di=0;
//        for (int i=0; i<center.size(); i++){
//            di += Di(i, center, helpcenter);
//        }
//        dbi = di / center.size();
//        if ( Float.isNaN(dbi) ){
//            dbi = 50;
//        }
//        System.out.println("DBI: " + dbi);
//    }
//
//}

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class DBI {
    public static double dbi = 0;

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

    DBI(List<ArrayList<Double>> center, List<ArrayList<ArrayList<Double>>> helpcenter){
        double di=0;
        for (int i=0; i<center.size(); i++){
            di += Di(i, center, helpcenter);
        }
        dbi = di / center.size();
        if ( Double.isNaN(dbi) ){
            dbi = 50;
        }
        System.out.println("DBI: " + dbi);
    }

}