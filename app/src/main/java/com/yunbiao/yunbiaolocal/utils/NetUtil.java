package com.yunbiao.yunbiaolocal.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.netcore.DownloadListener;
import com.yunbiao.yunbiaolocal.netcore.DownloadTask;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/11/22.
 */

public class NetUtil {
    private static NetUtil mInstance;
    private RequestCall build;
    private final String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private DownloadTask downloadTask;

    public synchronized static NetUtil getInstance() {
        if (mInstance == null) {
            mInstance = new NetUtil();
        }
        return mInstance;
    }

    public void stop() {
        if (build != null) {
            OkHttpUtils.getInstance().cancelTag(this);
        }
    }

    public void post(String url, Map<String, String> params, StringCallback stringCallback) {
        OkHttpUtils.post()
                .url(url)
                .params(params)
                .tag(this)
                .build()
                .execute(stringCallback);
    }

    public void breakPointDownLoad(String url,DownloadListener listener){
        if(downloadTask == null){
            downloadTask = new DownloadTask(listener);
        }
        downloadTask.execute(url);
    }

    private String lastCacheUrl;
    public void downLoadFile(final String url , final OnDownLoadListener onDownLoadListener){
        if(TextUtils.equals(url,lastCacheUrl)){
            LogUtil.E("此文件正在下载。");
            return;
        }
        lastCacheUrl = url;
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        onDownLoadListener.onStart(fileName);
        OkHttpUtils.getInstance().cancelTag(this);

        if(!url.startsWith("http://") && !url.startsWith("https")){
            onDownLoadListener.onError(new Exception("Unsupported download protocols"));
            onDownLoadListener.onFinish();
            return;
        }

        try{
            File file = new File(rootDir);
            String[] list = file.list();
            for (String fn : list) {
                if(fn.contains(fileName)){
                    LogUtil.E("内存中已有该文件");
                    onDownLoadListener.onComplete(new File(rootDir + "/"+fn));
                    onDownLoadListener.onFinish();
                    return;
                }
            }

            OkHttpUtils.get()
                    .url(url)
                    .tag(this)
                    .build()
                    .execute(new FileCallBack(rootDir, fileName) {
                        int mainProg;

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            try {
                                downLoadFile(url,onDownLoadListener);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            onDownLoadListener.onError(e);
                            onDownLoadListener.onFinish();
                        }

                        @Override
                        public void onResponse(File response, int id) {
                            onDownLoadListener.onComplete(response);
                        }

                        @Override
                        public void inProgress(float progress, long total, int id) {
                            int i = (int) (100 * progress);
                            if (mainProg != i) {
                                onDownLoadListener.onDownloading(i);
                            }
                            mainProg = i;
                        }

                        @Override
                        public void onAfter(int id) {
                            onDownLoadListener.onFinish();
                        }
                    });
        }catch (NullPointerException | IllegalArgumentException e){
            onDownLoadListener.onError(e);
        }

    }

    public interface OnDownLoadListener {
        void onStart(String fileName);

        void onDownloading(int progress);

        void onComplete(File response);

        void onFinish();

        void onError(Exception e);
    }

}
