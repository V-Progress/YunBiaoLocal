package com.yunbiao.cccm.net.listener;

/**
 * Created by Wei.Zhang on 2018/12/24.
 */

/***
 * NOTICE: this interface is running in handle Thread
 */
public interface MultiFileDownloadListener {
    /***
     * before download
     * @param totalNum 下载文件的总数
     */
    void onBefore(int totalNum);

    /***
     * start download
     * @param currNum 当前下载数的索引
     */
    void onStart(int currNum);

    /***
     * download progress
     * @param progress 进度（发生变化时回调）
     */
    void onProgress(int progress);

    /***
     * download speed(real time)
     * @param speed 实时速度
     */
    void onDownloadSpeed(long speed);

    /***
     * downloading finish
     */
    void onFinish();

    void onError(Exception e, int currFileNum, int totalNum, String fileName);

    void onSuccess(int currFileNum, int totalNum, String fileName);
}
