package communication.utils;

import java.util.ArrayList;

public class Para {
    //master to slave
    public int state = 1;
    public String msg = "master to slave!";
    public int globalCenter = 0;
    public ArrayList<float[]> w;
    public ArrayList<double[]> grad;
    public double beta;
    public int time;

    //slave to master
    public String slaveName = "slave";
    public int localCenter = 0;
    public double acc;
    public int num;
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

    public ArrayList<float[]> getW() {
        return w;
    }

    public void setW(ArrayList<float[]> w) {
        this.w = w;
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

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
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

    public void setGrad(ArrayList<double[]> grad) {
        this.grad = grad;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public ArrayList<double[]> getGrad() {
        return grad;
    }

    public double getBeta() {
        return beta;
    }


    @Override
    public String toString() {
        return "Para{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                ", globalCenter=" + globalCenter +
                ", w=" + w +
                ", grad=" + grad +
                ", beta=" + beta +
                ", time=" + time +
                ", slaveName='" + slaveName + '\'' +
                ", localCenter=" + localCenter +
                ", acc=" + acc +
                ", num=" + num +
                ", sendtime=" + sendtime +
                ", runtime=" + runtime +
                '}';
    }
}
