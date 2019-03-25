package com.yunbiao.cccm.net.resource;

/**
 * Created by Administrator on 2019/3/12.
 */

public class UpdateEvent{
    public static int UPDATE_PLAYLIST = 1;

    public UpdateEvent(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}