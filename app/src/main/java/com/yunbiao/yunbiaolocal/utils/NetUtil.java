package com.yunbiao.yunbiaolocal.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.netcore.DownloadInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/11/22.
 */

public class NetUtil {
    private static final String URL = "http://cdn7.mydown.com/5c012160/85f38acfd007042510435ad5d4615748/newsoft/QQ9.0.8.exe";
    private static NetUtil mInstance;
    private RequestCall build;
    private final String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();

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

    public void downLoadFile(final OnDownLoadListener onDownLoadListener) throws Exception {
        String fileName = URL.substring(URL.lastIndexOf("/") + 1);
        onDownLoadListener.onStart(fileName);
        OkHttpUtils.getInstance().cancelTag(this);

        if(!URL.startsWith("http") && !URL.startsWith("https")){
            throw new Exception("Unsupported download protocols");
        }
        try{
            OkHttpUtils.get()
                    .url(URL)
                    .tag(this)
                    .build()
                    .execute(new FileCallBack(rootDir, fileName) {
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
                            if (mainProg != i) {
                                onDownLoadListener.onDownloading(i + "%");
                            }
                            mainProg = i;
                        }
                    });
        }catch (NullPointerException | IllegalArgumentException e){
            onDownLoadListener.onError(e);
        }

    }

    public interface OnDownLoadListener {
        void onStart(String fileName);

        void onDownloading(String progress);

        void onComplete(File response);

        void onFinish();

        void onError(Exception e);
    }

}
