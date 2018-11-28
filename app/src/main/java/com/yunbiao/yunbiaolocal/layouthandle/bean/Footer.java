package com.yunbiao.yunbiaolocal.layouthandle.bean;

/**
 * Created by Administrator on 2018/11/27.
 */

public class Footer{
    String background;
    String enabled;
    String fontColor;
    String fontFamily;
    String fontSize;
    String footerText;
    String isPlay;
    String playTime;

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

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getIsPlay() {
        return isPlay;
    }

    public void setIsPlay(String isPlay) {
        this.isPlay = isPlay;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    @Override
    public String toString() {
        return "Footer{" +
                "background='" + background + '\'' +
                ", enabled='" + enabled + '\'' +
                ", fontColor='" + fontColor + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", footerText='" + footerText + '\'' +
                ", isPlay='" + isPlay + '\'' +
                ", playTime='" + playTime + '\'' +
                '}';
    }
}