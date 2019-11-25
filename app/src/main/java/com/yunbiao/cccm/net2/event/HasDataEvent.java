package com.yunbiao.cccm.net2.event;

/**
 * Created by Administrator on 2019/11/15.
 */

public class HasDataEvent {

    private boolean hasData;
    private int type;

    public HasDataEvent(boolean hasData, int type) {
        this.hasData = hasData;
        this.type = type;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
