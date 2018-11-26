package com.yunbiao.yunbiaolocal.viewfactory.bean;

/**
 * Created by Administrator on 2018/7/23.
 */

public class AdsData {
    private String resourceId;//资源id
    private String playTime;//播放时长
    private String playNum;//播放次数
    private String isLog;//是否记录日志（垫片不记录）
    private String url;//广告路径

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    public String getPlayNum() {
        return playNum;
    }

    public void setPlayNum(String playNum) {
        this.playNum = playNum;
    }

    public String getIsLog() {
        return isLog;
    }

    public void setIsLog(String isLog) {
        this.isLog = isLog;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
