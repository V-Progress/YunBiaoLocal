package com.yunbiao.yunbiaolocal.layouthandle;

import android.app.Activity;
import android.view.View;

import com.yunbiao.yunbiaolocal.act.Main2Activity;

/**
 * Created by Administrator on 2018/11/28.
 */

public class LayoutRefresher {

    private static LayoutRefresher instance;
    private static Activity mActivity;

    public synchronized static LayoutRefresher getInstance(){
        if(instance == null){
            instance = new LayoutRefresher();
        }
        return instance;
    }

    public Activity getMainActivity(){
        return mActivity;
    }

    public void registerAct(Activity activity){
        mActivity = activity;
    }

    public void updateLayout(View view){
        Main2Activity ma;
        if(mActivity instanceof Main2Activity){
            ma = (Main2Activity) mActivity;
            ma.absoluteLayout.addView(view);
        }
    }

    public void unRegister(){
        mActivity = null;
        instance = null;
    }


}
