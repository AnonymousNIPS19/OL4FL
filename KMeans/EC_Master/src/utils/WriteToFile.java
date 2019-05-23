//package utils;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.category.DefaultCategoryDataset;

//import javax.swing.*;
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class WriteToFile {
//    public static boolean write(String path, List<Double> list){
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String line = format.format(new Date()) + "  size:"+ list.size() + " data=" +  list.toString() + "\r\n";
//        try {
//            File file = new File(path);
//            if( !file.exists() ) {
//                file.createNewFile();
//            }
//            FileWriter fileWriter = new FileWriter(file, true);
//            fileWriter.write(line);
//            fileWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//    public static boolean write(String path, HashMap<String, List<Double>> map, String master){
//        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
//        String currentTime = format.format(new Date());
//        String dataPath = null;
//        String picturePath = null;
//        if( master.equals("sync") ){
//            dataPath = path + File.separator + "regretsSync.txt";
//            picturePath = path + File.separator + "regretsSync" + File.separator + currentTime +".png";
//        }else{//async
//            dataPath = path + File.separator + "regretsAsync.txt";
//            picturePath = path + File.separator + "regretsAsync" + File.separator + currentTime +".png";
//        }
//
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        Iterator<Map.Entry<String, List<Double>>> entries = map.entrySet().iterator();
//        while (entries.hasNext()) {
//            Map.Entry<String, List<Double>> entry = entries.next();
//            String slaveName = entry.getKey();
//            List<Double> list  = entry.getValue();
//            String line = currentTime + " " +  slaveName +"  size:"+ list.size() + " data=" +  list.toString() + "\r\n";
//            writeDataToFile(dataPath, line);
//
//            for( int i = 0; i < list.size(); i++ ) {
//                dataset.addValue(list.get(i), slaveName, String.valueOf(i));
//            }
//        }
//        drawLineChart(dataset,picturePath);
//        return true;
//    }
//
//    public static boolean writeDataToFile(String path, String line){
//        try {
//            File file = new File(path);
//            if( !file.exists() ) {
//                file.createNewFile();
//            }
//            FileWriter fileWriter = new FileWriter(file, true);
//            fileWriter.write(line);
//            fileWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//    public static void drawLineChart(DefaultCategoryDataset dataset, String pictruePath) {
//
//        JFreeChart chart = ChartFactory.createLineChart("title", "x", "regret", dataset);
//        JPanel jPanel = new ChartPanel(chart);
//        JFrame frame = new JFrame("Title");
//        frame.add(jPanel);
//        frame.setBounds(0, 0, 1000, 600);
//        frame.setVisible(true);
////        while(frame.isVisible()){
////            try{
////                Thread.sleep(1000);//死循环中降低CPU占用
////            } catch(Exception e){
////                e.printStackTrace();
////            }
////        }
//        saveAsFile(chart, pictruePath);
//    }
//
//    public static void saveAsFile(JFreeChart chart, String path) {
//        FileOutputStream out = null;
//        try {
//            File outFile = new File(path);
//            if (!outFile.getParentFile().exists()) {
//                outFile.getParentFile().mkdirs();
//            }
//            out = new FileOutputStream(path);
//            // 保存为PNG
//            ChartUtilities.writeChartAsPNG(out, chart, 1000, 1000);
//            // 保存为JPEG
////	      ChartUtilities.writeChartAsJPEG(out, chart, 600, 400);
//            out.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//}
