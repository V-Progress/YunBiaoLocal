package com.yunbiao.cccm.activity.weichat.bean;

public class WeiMessage implements  java.io.Serializable {

    private Integer type;
    private String content;
    private String msgId;
    private String userId;
    private String userName;
    private String headUrl;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }



    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
