package com.yunbiao.yunbiaolocal.utils;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/12/3.
 */

public class ThreadUtil {
    private static ThreadUtil threadUtil;
    private ExecutorService mSingleThread;
    private ExecutorService thread4Pool;

    public static synchronized ThreadUtil getInstance(){
        if(threadUtil == null){
            threadUtil = new ThreadUtil();
        }
        return threadUtil;
    }

    public ThreadUtil() {
        mSingleThread = Executors.newSingleThreadExecutor();
        thread4Pool = Executors.newFixedThreadPool(4);
    }

    public void runInSingleThread(Runnable runnable){
        mSingleThread.execute(runnable);
    }

    public void runInFixedThread(Runnable runnable){
        thread4Pool.execute(runnable);
    }

    public void runInUIThread(Runnable runnable){
        new Handler().post(runnable);
    }
}
