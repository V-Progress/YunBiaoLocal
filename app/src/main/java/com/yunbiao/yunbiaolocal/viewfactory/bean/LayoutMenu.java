package com.yunbiao.yunbiaolocal.viewfactory.bean;

public class LayoutMenu {

    private boolean isShow;
    private boolean timeShow;
    private boolean weatherShow;
    private Float fontSize;
    private String timeFormat;
    private String fontFamily;
    private String fontColor;
    private String logoImage;

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public Float getFontSize() {
        return fontSize;
    }

    public void setFontSize(Float fontSize) {
        this.fontSize = fontSize;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public boolean isTimeShow() {
        return timeShow;
    }

    public void setTimeShow(boolean timeShow) {
        this.timeShow = timeShow;
    }

    public boolean isWeatherShow() {
        return weatherShow;
    }

    public void setWeatherShow(boolean weatherShow) {
        this.weatherShow = weatherShow;
    }

    public String getBackGround() {
        return backGround;
    }

    public void setBackGround(String backGround) {
        this.backGround = backGround;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String backGround;
    private String address;
}
