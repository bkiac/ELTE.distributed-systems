package hu.elte.agent;

import java.util.List;

public class Agent {

    private int serialNumber;
    private List<String> names;
    private String msg;

    public Agent(int serialNumber, List<String> names, String msg) {
        this.serialNumber = serialNumber;
        this.names = names;
        this.msg = msg;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return serialNumber + ". " + names + " '" + msg + "'";
    }
}
