package com.yunbiao.yunbiaolocal.utils;

import android.os.Environment;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/11/22.
 */

public class NetUtil {
    private static final String URL = "https://dl.google.com/android/android-sdk_r24.4.1-windows.zip";
    private static NetUtil mInstance;
    private RequestCall build;
    private final String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    public synchronized static NetUtil getInstance(){
        if(mInstance == null){
            mInstance = new NetUtil();
        }
        return mInstance;
    }

    public void stop(){
        if(build != null){
            build.cancel();
        }
    }

    public void requestNet(String params,StringCallback stringCallback){
        build = OkHttpUtils.post()
                .addParams("",params)
                .build()
                .connTimeOut(30000)
                .readTimeOut(30000)
                .writeTimeOut(30000);
        build.execute(stringCallback);
    }

    public void downLoadFile(final OnDownLoadListener onDownLoadListener){
        if(build == null){
            build = OkHttpUtils
                    .get()
                    .url(URL)
                    .tag(getClass().getSimpleName())
                    .build()
                    .connTimeOut(30000)
                    .readTimeOut(30000)
                    .writeTimeOut(30000);
        }
        build.cancel();
        String fileName = URL.substring(URL.lastIndexOf("/") + 1);
        onDownLoadListener.onStart(fileName);

        build.execute(new FileCallBack(rootDir,fileName) {
            int mainProg;
            @Override
            public void onError(Call call, Exception e, int id) {
                onDownLoadListener.onError(e);
                onDownLoadListener.onFinish();
            }

            @Override
            public void onResponse(File response, int id) {
                onDownLoadListener.onComplete(response);
                onDownLoadListener.onFinish();
            }

            @Override
            public void inProgress(float progress, long total, int id) {
                int i = (int) (100 * progress);
                if(mainProg != i){
                    onDownLoadListener.onDownloading(i+"%");
                }
                mainProg = i;
            }
        });
    }

    public interface OnDownLoadListener{
        void onStart(String fileName);
        void onDownloading(String progress);
        void onComplete(File response);
        void onFinish();
        void onError(Exception e);
    }
}
