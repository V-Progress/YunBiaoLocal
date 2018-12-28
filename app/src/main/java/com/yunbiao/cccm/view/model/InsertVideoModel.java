package com.yunbiao.cccm.view.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/12/5.
 */

public class InsertVideoModel implements Serializable {

    private String fileurl;
    protected String playCurTime;
    protected String playDate;

    public String getPlayCurTime() {
        return playCurTime;
    }

    public void setPlayCurTime(String playCurTime) {
        this.playCurTime = playCurTime;
    }

    public String getPlayDate() {
        return playDate;
    }

    public void setPlayDate(String playDate) {
        this.playDate = playDate;
    }
    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    @Override
    public String toString() {
        return "InsertVideoModel{" +
                "fileurl='" + fileurl + '\'' +
                ", playCurTime='" + playCurTime + '\'' +
                ", playDate='" + playDate + '\'' +
                '}';
    }
}
