package com.yunbiao.cccm.utils;

import android.os.Handler;

import com.yunbiao.cccm.common.Const;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/12/3.
 */

public class ThreadUtil {
    private static ThreadUtil threadUtil;
    private ExecutorService mSingleThread;
    private ExecutorService commonPool;
    private Handler mHandler = new Handler();
    private final ExecutorService remotePool;

    public static synchronized ThreadUtil getInstance() {
        if (threadUtil == null) {
            threadUtil = new ThreadUtil();
        }
        return threadUtil;
    }

    public ThreadUtil() {
        mSingleThread = Executors.newSingleThreadExecutor();
        commonPool = Executors.newFixedThreadPool(Const.SYSTEM_CONFIG.DATA_HANDLE_THREAD_NUMBER);
        remotePool = Executors.newFixedThreadPool(Const.SYSTEM_CONFIG.REMOTE_THREAD_NUMBER);
    }

    public void runInSingleThread(Runnable runnable) {
        mSingleThread.execute(runnable);
    }

    /***
     * 运行在普通线程中
     * 可进行消息处理，数据解析，或其他操作。
     * 默认corePoolSize为4
     * @param runnable
     */
    public void runInCommonThread(Runnable runnable) {
        commonPool.execute(runnable);
    }

    /***
     * 运行在网络线程中
     * 专用于网络下载和网络请求
     * 默认corePoolSize为2
     * @param runnable
     */
    public void runInRemoteThread(Runnable runnable) {
        remotePool.execute(runnable);
    }

    public void runInUIThread(Runnable runnable) {
        mHandler.post(runnable);
    }

}