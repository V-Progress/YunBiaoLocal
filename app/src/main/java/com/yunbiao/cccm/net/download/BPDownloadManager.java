package com.yunbiao.cccm.net.download;

import android.os.Build;

import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.net.listener.MultiFileDownloadListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Administrator on 2019/2/22.
 */

public class BPDownloadManager {

    //下载线程
    private Thread downloadThread;

    //下载标识
    private Object mTag;

    //断点下载
    private BPDownload bpDownload;

    //多文件下载监听
    private MultiFileDownloadListener l;

    private int totalNum;

    public BPDownloadManager(Object tag, FileDownloadListener fileDownloadListener) {
        if (fileDownloadListener == null) {
            l = new FileDownloadListener() {

            };
        } else {
            l = fileDownloadListener;
        }

        mTag = tag;
    }


    /***
     * 断点下载
     * @param fileUrlList
     */
    public void startDownload(final List<String> fileUrlList) {
        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                download(fileUrlList);
            }
        });
        downloadThread.start();
    }

    /***
     * 取消当前下载
     */
    public void cancel() {
        l.onCancel();
        if(bpDownload != null){
            bpDownload.cancel();
        }
    }

    private void download(final List<String> fileUrlList) {
        if (l == null) {
            l = new FileDownloadListener() {
            };
        }

        //初始化一个队列，便于递归使用
        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.addAll(fileUrlList);

        //下载之前先调用一下before
        totalNum = urlQueue.size();
        l.onBefore(totalNum);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            bpDownload = new BPDownloadLowVer(mTag,totalNum,l);
        }else{
            bpDownload = new BPDownloadHighVer(mTag,totalNum,l);
        }

        bpDownload.breakPointDownload(urlQueue);
    }

}
