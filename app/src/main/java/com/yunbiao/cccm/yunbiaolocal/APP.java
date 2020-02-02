package com.yunbiao.cccm.yunbiaolocal;

import android.content.Context;

/**
 * Created by Administrator on 2019/2/27.
 */

public class APP {
    private static Context mContext;
    private static MainActivity mainActivity;

    public static void onCreate(Context context) {
        mContext = context;
    }

    public static void setMainActivity(MainActivity act){
        mainActivity = act;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static Context getContext(){
        return mContext;
    }
}
