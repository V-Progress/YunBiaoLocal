package com.yunbiao.cccm.devicectrl.power;

/**
 * Created by Administrator on 2018/12/6.
 */

public class PowerModel {

    private String deviceId;
    private String id;
    private String key;
    private String runDate;
    private String runTime;
    private Integer runType;
    private Integer status;

    @Override
    public String toString() {
        return "PowerModel{" +
                "deviceId='" + deviceId + '\'' +
                ", id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", runDate='" + runDate + '\'' +
                ", runTime='" + runTime + '\'' +
                ", runType='" + runType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public Integer getRunType() {
        return runType;
    }

    public void setRunType(Integer runType) {
        this.runType = runType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
