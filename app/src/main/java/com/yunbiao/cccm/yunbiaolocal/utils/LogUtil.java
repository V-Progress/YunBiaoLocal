package com.yunbiao.cccm.yunbiaolocal.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Administrator on 2018/12/4.
 */

public class LogUtil {
    private static String TAG = "123";
    private static boolean ISLOG = true;

    public static String generateTag(Object o){
        return o.getClass().getSimpleName().toString();
    }

    public static void E(String log) {
        if(!ISLOG){
            return;
        }
        E(null,log);
    }

    public static void E(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.e(TextUtils.isEmpty(tag) ? TAG : tag, "---"+log);
    }

    public static void D(String log) {
        if(!ISLOG){
            return;
        }
        D(null,log);
    }

    public static void D(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.d(TextUtils.isEmpty(tag) ? TAG : tag, "---"+log);
    }

    public static void I(String log) {
        if(!ISLOG){
            return;
        }
        Log.i(TAG, log);
    }

    public static void I(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.i(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void WTF(String log) {
        if(!ISLOG){
            return;
        }
        Log.wtf(TAG, log);
    }

    public static void WTF(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.wtf(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void WTF(String log, Throwable t) {
        if(!ISLOG){
            return;
        }
        Log.wtf(TAG, log, t);
    }
}
