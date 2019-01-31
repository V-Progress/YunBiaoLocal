package com.yunbiao.cccm.netcore.bean;

/**
 * Created by Administrator on 2018/12/4.
 */

public class LoginModel {

    private String deviceName;
    private String password;
    private String pwd;
    private String serNum;
    private String status;
    private String ticket;
    private int layerType = 1;

    public int getLayerType() {
        return layerType;
    }

    public void setLayerType(int layerType) {
        this.layerType = layerType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getSerNum() {
        return serNum;
    }

    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
