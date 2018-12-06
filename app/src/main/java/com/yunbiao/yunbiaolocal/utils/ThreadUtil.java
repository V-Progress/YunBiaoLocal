package com.yunbiao.yunbiaolocal.utils;

import android.os.Handler;

import com.yunbiao.yunbiaolocal.common.Const;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/12/3.
 */

public class ThreadUtil {
    private static ThreadUtil threadUtil;
    private ExecutorService mSingleThread;
    private ExecutorService thread4Pool;
    private Handler mHandler = new Handler();

    public static synchronized ThreadUtil getInstance(){
        if(threadUtil == null){
            threadUtil = new ThreadUtil();
        }
        return threadUtil;
    }

    public ThreadUtil() {
        mSingleThread = Executors.newSingleThreadExecutor();
        thread4Pool = Executors.newFixedThreadPool(Const.SYSTEM_CONFIG.DATA_RESOLVE_THREAD_NUMBER);
    }

    public void runInSingleThread(Runnable runnable){
        mSingleThread.execute(runnable);
    }

    public void runInFixedThread(Runnable runnable){
        thread4Pool.execute(runnable);
    }

    public void runInUIThread(Runnable runnable){
        mHandler.post(runnable);
    }

}
