package com.yunbiao.cccm.utils;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.common.Const;

/**
 * Created by Administrator on 2018/12/4.
 */

public class LogUtil {
    private static String TAG = Const.SYSTEM_CONFIG.LOG_TAG;
    private static boolean ISLOG = Const.SYSTEM_CONFIG.IS_LOG;

    public static void E(String log) {
        if(!ISLOG){
            return;
        }
        Log.e(TAG, log);
    }

    public static void E(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.e(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void D(String log) {
        if(!ISLOG){
            return;
        }
        Log.d(TAG, log);
    }

    public static void D(String tag, String log) {
        if(!ISLOG){
            return;
        }
        Log.d(TextUtils.isEmpty(tag) ? TAG : tag, log);
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
