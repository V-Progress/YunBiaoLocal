package com.yunbiao.cccm.layout.bean;

import com.yunbiao.cccm.layout.view.baseControls.bean.ControlsDetail;

/**
 * Created by Administrator on 2018/11/27.
 */

public class Center{
    Container container;
    String[] content;
    String id;
    ImageDetail imageDetail;
    String type;
    TextDetail textDetail;
    WebDetail webmsg;
    private ControlsDetail controlsDetail;//倒计时内容详细
    private Integer windowType; //type 14 下的二级windowType  1天气 2日历 3汇率 4倒计时

    public ControlsDetail getControlsDetail() {
        return controlsDetail;
    }

    public void setControlsDetail(ControlsDetail controlsDetail) {
        this.controlsDetail = controlsDetail;
    }

    public WebDetail getWebmsg() {
        return webmsg;
    }

    public void setWebmsg(WebDetail webmsg) {
        this.webmsg = webmsg;
    }

    public Integer getWindowType() {
        return windowType;
    }

    public void setWindowType(Integer windowType) {
        this.windowType = windowType;
    }

    public WebDetail getWebDetail() {
        return webmsg;
    }

    public void setWebDetail(WebDetail webDetail) {
        this.webmsg = webDetail;
    }

    public TextDetail getTextDetail() {
        return textDetail;
    }

    public void setTextDetail(TextDetail textDetail) {
        this.textDetail = textDetail;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ImageDetail getImageDetail() {
        return imageDetail;
    }

    public void setImageDetail(ImageDetail imageDetail) {
        this.imageDetail = imageDetail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Center{" +
                "container=" + container +
                ", id='" + id + '\'' +
                ", imageDetail=" + imageDetail +
                ", type='" + type + '\'' +
                '}';
    }
}