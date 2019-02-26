package com.yunbiao.cccm.net.listener;

/**
 * Created by Administrator on 2019/1/11.
 */


public abstract class FileDownloadListener implements MultiFileDownloadListener {
    @Override
    public void onBefore(int totalNum) {

    }

    @Override
    public void onStart(int currNum) {

    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onDownloadSpeed(long speed) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onError(Exception e, int currFileNum, int totalNum, String fileName) {

    }

    @Override
    public void onSuccess(int currFileNum, int totalNum, String fileName) {

    }

    @Override
    public void onCancel() {

    }
}