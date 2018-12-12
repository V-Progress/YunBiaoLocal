package com.yunbiao.yunbiaolocal.layout.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/11/27.
 */

public class LayoutModel {
    private List<Center> center;
    private String eDate;
    private String end;
    private Footer footer;
    private Header header;
    private String layoutType;
    private List<Move> move;
    private String resClearModel;
    private String runType;
    private String sDate;
    private String start;
    private String weekDay;

    public List<Center> getCenter() {
        return center;
    }

    public void setCenter(List<Center> center) {
        this.center = center;
    }

    public String geteDate() {
        return eDate;
    }

    public void seteDate(String eDate) {
        this.eDate = eDate;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Footer getFooter() {
        return footer;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public List<Move> getMove() {
        return move;
    }

    public void setMove(List<Move> move) {
        this.move = move;
    }

    public String getResClearModel() {
        return resClearModel;
    }

    public void setResClearModel(String resClearModel) {
        this.resClearModel = resClearModel;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    @Override
    public String toString() {
        return "LayoutModel{" +
                "center=" + center +
                ", eDate='" + eDate + '\'' +
                ", end='" + end + '\'' +
                ", footer=" + footer +
                ", header=" + header +
                ", layoutType='" + layoutType + '\'' +
                ", move=" + move +
                ", resClearModel='" + resClearModel + '\'' +
                ", runType='" + runType + '\'' +
                ", sDate='" + sDate + '\'' +
                ", start='" + start + '\'' +
                ", weekDay='" + weekDay + '\'' +
                '}';
    }
}
