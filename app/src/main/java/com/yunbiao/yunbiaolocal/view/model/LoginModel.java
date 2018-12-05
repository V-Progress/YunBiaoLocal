package com.yunbiao.yunbiaolocal.view.model;

/**
 * Created by Administrator on 2018/12/4.
 */

public class LoginModel {

    private String bindStatus;
    private String camera;
    private String cameraShow;
    private String deviceName;
    private String deviceQrCode;
    private String dtype;
    private String expireDate;
    private String isMirror;
    private String password;
    private String pwd;
    private String runKey;
    private String runStatus;
    private String serNum;
    private String shareStatus;
    private String status;
    private String ticket;

    public String getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getCameraShow() {
        return cameraShow;
    }

    public void setCameraShow(String cameraShow) {
        this.cameraShow = cameraShow;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceQrCode() {
        return deviceQrCode;
    }

    public void setDeviceQrCode(String deviceQrCode) {
        this.deviceQrCode = deviceQrCode;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getIsMirror() {
        return isMirror;
    }

    public void setIsMirror(String isMirror) {
        this.isMirror = isMirror;
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

    public String getRunKey() {
        return runKey;
    }

    public void setRunKey(String runKey) {
        this.runKey = runKey;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getSerNum() {
        return serNum;
    }

    public void setSerNum(String serNum) {
        this.serNum = serNum;
    }

    public String getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(String shareStatus) {
        this.shareStatus = shareStatus;
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

    @Override
    public String toString() {
        return "LoginModel{" +
                "bindStatus='" + bindStatus + '\'' +
                ", camera='" + camera + '\'' +
                ", cameraShow='" + cameraShow + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceQrCode='" + deviceQrCode + '\'' +
                ", dtype='" + dtype + '\'' +
                ", expireDate='" + expireDate + '\'' +
                ", isMirror='" + isMirror + '\'' +
                ", password='" + password + '\'' +
                ", pwd='" + pwd + '\'' +
                ", runKey='" + runKey + '\'' +
                ", runStatus='" + runStatus + '\'' +
                ", serNum='" + serNum + '\'' +
                ", shareStatus='" + shareStatus + '\'' +
                ", status='" + status + '\'' +
                ", ticket='" + ticket + '\'' +
                '}';
    }
}
