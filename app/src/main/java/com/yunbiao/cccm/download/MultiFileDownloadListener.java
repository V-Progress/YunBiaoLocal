package com.yunbiao.cccm.download;

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
     * single file downloading success
     * @param currFileNum 当前下载数的索引
     */
    void onSuccess(int currFileNum);

    /***
     * single file downloaded error
     * @param e 单个文件下载错误
     */
    void onError(Exception e);

    /***
     * downloading finish
     */
    void onFinish();

    /***
     * Unable to deal with downloading
     * @param e 无法处理的异常，会导致下载流程无法继续进行
     */
    void onFailed(Exception e);
}
