package com.yunbiao.cccm.layout.bean;

public class WebDetail {

    private Boolean autoflus;
    private String flustime;
    private String webType;

    public String getWebType() {
        return webType;
    }

    public void setWebType(String webType) {
        this.webType = webType;
    }

    public Boolean getAutoFlus() {
        return autoflus;
    }

    public void setAutoFlus(Boolean autoFlus) {
        this.autoflus = autoFlus;
    }

    public String getFlusTime() {
        return flustime;
    }

    public void setFlusTime(String flusTime) {
        this.flustime = flusTime;
    }

    @Override
    public String toString() {
        return "WebDetail{" +
                "autoFlus=" + autoflus +
                ", flusTime='" + flustime + '\'' +
                ", webType='" + webType + '\'' +
                '}';
    }
}
