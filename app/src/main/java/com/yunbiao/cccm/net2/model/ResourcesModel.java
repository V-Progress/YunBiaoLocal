package com.yunbiao.cccm.net2.model;

/**
 * Created by Administrator on 2018/12/21.
 */

public class ResourcesModel {

    private Integer result;
    private String message;
    private DataJson dataJson;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataJson getDataJson() {
        return dataJson;
    }

    public void setDataJson(DataJson dataJson) {
        this.dataJson = dataJson;
    }

    public static class DataJson{
        private String ftpServiceUrl;
        private String configUrl;

        public String getFtpServiceUrl() {
            return ftpServiceUrl;
        }

        public void setFtpServiceUrl(String ftpServiceUrl) {
            this.ftpServiceUrl = ftpServiceUrl;
        }

        public String getConfigUrl() {
            return configUrl;
        }

        public void setConfigUrl(String configUrl) {
            this.configUrl = configUrl;
        }
    }

    @Override
    public String toString() {
        return "ResourcesModel{" +
                "result=" + result +
                ", message='" + message + '\'' +
                ", dataJson=" + dataJson +
                '}';
    }
}
