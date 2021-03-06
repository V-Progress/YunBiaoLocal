package com.yunbiao.cccm.net2.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/12/5.
 */

public class InsertTextModel implements Serializable {

    private Content content;
    private String playType;
    private String text;

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public class Content implements Serializable {
        private String background;
        private String fontColor;
        private Integer fontSize;
        private String location;
        private String playCurTime;
        private String playDate;
        private Integer playSpeed;
        private String playTime;
        private String playType;
        private String speechCount;
        private String transparent;

        @Override
        public String toString() {
            return "Content{" +
                    "background='" + background + '\'' +
                    ", fontColor='" + fontColor + '\'' +
                    ", fontSize=" + fontSize +
                    ", location='" + location + '\'' +
                    ", playCurTime='" + playCurTime + '\'' +
                    ", playDate='" + playDate + '\'' +
                    ", playSpeed=" + playSpeed +
                    ", playTime='" + playTime + '\'' +
                    ", playType='" + playType + '\'' +
                    ", speechCount='" + speechCount + '\'' +
                    ", transparent='" + transparent + '\'' +
                    '}';
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getFontColor() {
            return fontColor;
        }

        public void setFontColor(String fontColor) {
            this.fontColor = fontColor;
        }

        public Integer getFontSize() {
            return fontSize;
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

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

        public Integer getPlaySpeed() {
            return playSpeed;
        }

        public void setPlaySpeed(Integer playSpeed) {
            this.playSpeed = playSpeed;
        }

        public String getPlayTime() {
            return playTime;
        }

        public void setPlayTime(String playTime) {
            this.playTime = playTime;
        }

        public String getPlayType() {
            return playType;
        }

        public void setPlayType(String playType) {
            this.playType = playType;
        }

        public String getSpeechCount() {
            return speechCount;
        }

        public void setSpeechCount(String speechCount) {
            this.speechCount = speechCount;
        }

        public String getTransparent() {
            return transparent;
        }

        public void setTransparent(String transparent) {
            this.transparent = transparent;
        }
    }

    @Override
    public String toString() {
        return "InsertTextModel{" +
                "content=" + content.toString() +
                ", playType='" + playType + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
