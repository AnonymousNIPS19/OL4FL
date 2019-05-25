package communication.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Para {

    //master to slave
    public int state = 1;
    public String msg = "master to slave!";
    public int globalCenter = 0;
    public List<ArrayList<Double>> centerList;
    public int time;

    //slave to master
    public String slaveName = "slave";
    public int localCenter = 0;
    public int[] num;
    public double DBI;
    public int length;
    public long sendtime;
    public long runtime;
    public Para(String str) {
        this.slaveName = str;
    }
    public Para() {}


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getGlobalCenter() {
        return globalCenter;
    }

    public void setGlobalCenter(int globalCenter) {
        this.globalCenter = globalCenter;
    }

    public List<ArrayList<Double>> getCenterList() {
        return centerList;
    }

    public void setCenterList(List<ArrayList<Double>> centerList) {
        this.centerList = centerList;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getSlaveName() {
        return slaveName;
    }

    public void setSlaveName(String slaveName) {
        this.slaveName = slaveName;
    }

    public int getLocalCenter() {
        return localCenter;
    }

    public void setLocalCenter(int localCenter) {
        this.localCenter = localCenter;
    }

    public int[] getNum() {
        return num;
    }

    public void setNum(int[] num) {
        this.num = num;
    }

    public double getDBI() {
        return DBI;
    }

    public void setDBI(double DBI) {
        this.DBI = DBI;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getSendtime() {
        return sendtime;
    }

    public void setSendtime(long sendtime) {
        this.sendtime = sendtime;
    }

    public long getRuntime() {
        return runtime;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        return "Para{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                ", globalCenter=" + globalCenter +
                ", centerList=" + centerList +
                ", time=" + time +
                ", slaveName='" + slaveName + '\'' +
                ", localCenter=" + localCenter +
                ", num=" + Arrays.toString(num) +
                ", DBI=" + DBI +
                ", length=" + length +
                ", sendtime=" + sendtime +
                ", runtime=" + runtime +
                '}';
    }
}
