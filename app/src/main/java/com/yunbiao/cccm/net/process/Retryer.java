package com.yunbiao.cccm.net.process;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net.download.BPDownloadManager;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.utils.ConsoleUtil;

import java.util.Queue;

/**
 * Created by Administrator on 2019/4/25.
 */


public class Retryer extends FileDownloadListener {

    private BPDownloadManager downloadManager;
    private String currUrl;
    public interface FinishListener {
        void finish();
    }
    private Queue<String> mQueue;
    private FinishListener mListener;

    public Retryer(Queue<String> failedQueue,FinishListener listener){
        mQueue = failedQueue;
        mListener = listener;
    }

    public void start(){
        cancel();
        go();
    }

    private void go(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(mQueue == null || mQueue.size()<=0){
            onFinish();
            return;
        }
        currUrl = mQueue.poll();
        downloadManager = new BPDownloadManager(getClass(), this);
        downloadManager.downloadSingle(currUrl);
    }

    private void cancel(){
        if(downloadManager != null){
            downloadManager.cancel();
        }
    }

    @Override
    public void onStart(int currNum) {
        APP.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConsoleUtil.instance().openRetry();
                String name = currUrl.substring(currUrl.lastIndexOf("/")).substring(1);
                ConsoleUtil.instance().updateRetry(name,"0");
            }
        });
    }

    @Override
    public void onProgress(final int progress) {
        APP.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String name = currUrl.substring(currUrl.lastIndexOf("/")).substring(1);
                ConsoleUtil.instance().updateRetry(name,String.valueOf(progress));
            }
        });
    }

    @Override
    public void onSuccess(int currFileNum, int totalNum, String fileName) {
        go();
    }

    @Override
    public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
        mQueue.offer(currUrl);
        go();
    }

    @Override
    public void onFinish() {
        APP.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConsoleUtil.instance().closeRetry();
            }
        });
        mListener.finish();
    }
}