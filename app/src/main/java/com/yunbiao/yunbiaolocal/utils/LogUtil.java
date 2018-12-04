package com.yunbiao.yunbiaolocal.utils;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.yunbiaolocal.Const;

/**
 * Created by Administrator on 2018/12/4.
 */

public class LogUtil {
    private static String TAG = Const.SYSTEM_CONFIG.LOG_TAG;

    public static void E(String log) {
        Log.e(TAG, log);
    }

    public static void E(String tag, String log) {
        Log.e(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void D(String log) {
        Log.d(TAG, log);
    }

    public static void D(String tag, String log) {
        Log.d(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void I(String log) {
        Log.i(TAG, log);
    }

    public static void I(String tag, String log) {
        Log.i(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void WTF(String log) {
        Log.wtf(TAG, log);
    }

    public static void WTF(String tag, String log) {
        Log.wtf(TextUtils.isEmpty(tag) ? TAG : tag, log);
    }

    public static void WTF(String log, Throwable t) {
        Log.wtf(TAG, log, t);
    }
}
