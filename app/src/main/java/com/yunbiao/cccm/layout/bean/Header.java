package com.yunbiao.cccm.layout.bean;

/**
 * Created by Administrator on 2018/11/27.
 */

public class Header{
    String address;
    String background;
    String enabled;
    String fontColor;
    String fontFamily;
    String fontSize;
    String logoimg;
    String timeFormat;
    String timeShow;
    String weatherShow;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getLogoimg() {
        return logoimg;
    }

    public void setLogoimg(String logoimg) {
        this.logoimg = logoimg;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getTimeShow() {
        return timeShow;
    }

    public void setTimeShow(String timeShow) {
        this.timeShow = timeShow;
    }

    public String getWeatherShow() {
        return weatherShow;
    }

    public void setWeatherShow(String weatherShow) {
        this.weatherShow = weatherShow;
    }

    @Override
    public String toString() {
        return "Header{" +
                "address='" + address + '\'' +
                ", background='" + background + '\'' +
                ", enabled='" + enabled + '\'' +
                ", fontColor='" + fontColor + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", logoimg='" + logoimg + '\'' +
                ", timeFormat='" + timeFormat + '\'' +
                ", timeShow='" + timeShow + '\'' +
                ", weatherShow='" + weatherShow + '\'' +
                '}';
    }
}