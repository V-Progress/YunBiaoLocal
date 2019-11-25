package com.yunbiao.cccm.net2;

import android.util.Log;

/**
 * Created by Administrator on 2019/11/21.
 */

public class LogHandler {
    private static final String TAG = "LogHandler";
    public static void log2Console(String log){
        Log.d(TAG, log);
        ConsoleDialog.addTextLog(log);
    }
}
