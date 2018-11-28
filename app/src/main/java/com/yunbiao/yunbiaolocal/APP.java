package com.yunbiao.yunbiaolocal;

import android.app.Application;

/**
 * Created by Administrator on 2018/11/27.
 */

public class APP extends Application {

    private static APP instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Application getContext(){
        return instance;
    }

}
