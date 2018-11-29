package com.yunbiao.yunbiaolocal;

import android.app.Application;

import com.yunbiao.yunbiaolocal.act.MainActivity;

/**
 * Created by Administrator on 2018/11/27.
 */

public class APP extends Application {

    private static APP instance;
    private static MainActivity mActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Application getContext(){
        return instance;
    }
    public static void setMainActivity(MainActivity mainActivity){
        mActivity = mainActivity;
    }
    public static MainActivity getMainActivity(){
        return mActivity;
    }
}
