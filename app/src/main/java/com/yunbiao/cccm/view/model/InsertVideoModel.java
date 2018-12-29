package com.yunbiao.cccm.view.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/12/5.
 */

public class InsertVideoModel implements Serializable {
    private String message;
    private Integer result;
    private InsertData dateJson;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public InsertData getDateJson() {
        return dateJson;
    }

    public void setDateJson(InsertData dateJson) {
        this.dateJson = dateJson;
    }

    public static class InsertData implements Serializable {
        private String hsdresourceUrl;
        private List<Data> insertArray;

        public String getHsdresourceUrl() {
            return hsdresourceUrl;
        }

        public void setHsdresourceUrl(String hsdresourceUrl) {
            this.hsdresourceUrl = hsdresourceUrl;
        }

        public List<Data> getInsertArray() {
            return insertArray;
        }

        public void setInsertArray(List<Data> insertArray) {
            this.insertArray = insertArray;
        }

        @Override
        public String toString() {
            return "InsertData{" +
                    "hsdresourceUrl='" + hsdresourceUrl + '\'' +
                    ", insertArray=" + insertArray +
                    '}';
        }
    }

    public static class Data implements Serializable {
        private String content;
        private String endTime;
        private Integer isCycle;
        private Integer playType;
        private String startTime;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Integer getIsCycle() {
            return isCycle;
        }

        public void setIsCycle(Integer isCycle) {
            this.isCycle = isCycle;
        }

        public Integer getPalyType() {
            return playType;
        }

        public void setPalyType(Integer playType) {
            this.playType = playType;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "content='" + content + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", isCycle='" + isCycle + '\'' +
                    ", palyType='" + playType + '\'' +
                    ", startTime='" + startTime + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "InsertVideoModel{" +
                "message='" + message + '\'' +
                ", result=" + result +
                ", dateJson=" + dateJson +
                '}';
    }
}
