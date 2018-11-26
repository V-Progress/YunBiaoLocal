package com.yunbiao.yunbiaolocal.viewfactory.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/7/27.
 */

public class AdsInfo {
    private String startTime;
    private String endTime;
    private List<AdsData> adsData;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public List<AdsData> getAdsData() {
        return adsData;
    }

    public void setAdsData(List<AdsData> adsData) {
        this.adsData = adsData;
    }
}
