package com.yunbiao.cccm.net2.db;

/**
 * Created by Administrator on 2018/12/27.
 */

public class ConfigResponse {
    private String message;
    private int result;
    private Data dateJson;

    public static class Data{
        private String configUrl;
        private String ftpServiceUrl;

        public String getConfigUrl() {
            return configUrl;
        }

        public void setConfigUrl(String configUrl) {
            this.configUrl = configUrl;
        }

        public String getFtpServiceUrl() {
            return ftpServiceUrl;
        }

        public void setFtpServiceUrl(String ftpServiceUrl) {
            this.ftpServiceUrl = ftpServiceUrl;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "configUrl='" + configUrl + '\'' +
                    ", ftpServiceUrl='" + ftpServiceUrl + '\'' +
                    '}';
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Data getDataJson() {
        return dateJson;
    }

    public void setDataJson(Data dataJson) {
        this.dateJson = dataJson;
    }

    @Override
    public String toString() {
        return "ConfigResponse{" +
                "message='" + message + '\'' +
                ", result='" + result + '\'' +
                ", dataJson=" + dateJson +
                '}';
    }
}
