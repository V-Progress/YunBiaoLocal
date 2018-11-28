package com.yunbiao.yunbiaolocal.layouthandle;

import android.os.Handler;

public class ThreadUitls {

    public static Thread runInThread(Runnable r) {
        Thread th = new Thread(r);
        th.start();
        return th;
    }

    public static Handler handler = new Handler();

    public static void runInUIThread(Runnable r) {
        handler.post(r);
    }

}