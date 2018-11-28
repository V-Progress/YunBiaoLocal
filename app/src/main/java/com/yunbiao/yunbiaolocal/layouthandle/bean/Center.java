package com.yunbiao.yunbiaolocal.layouthandle.bean;

import com.yunbiao.yunbiaolocal.viewfactory.bean.Container;
import com.yunbiao.yunbiaolocal.viewfactory.bean.ImageDetail;

/**
 * Created by Administrator on 2018/11/27.
 */

public class Center{
    Container container;
    String id;
    ImageDetail imageDetail;
    String type;

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