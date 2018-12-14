package com.yunbiao.yunbiaolocal.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.netcore.DownloadListener;
import com.yunbiao.yunbiaolocal.netcore.DownloadTask;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

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

    public void get(String url, Callback callback){
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .build()
                .execute(callback);
    }

    public void post(String url, Map<String, String> params, StringCallback stringCallback) {
        OkHttpUtils.post()
                .url(url)
                .params(params)
                .tag(this)
                .build()
                .execute(stringCallback);
    }

    public void postScreenShoot(String imgUrl, StringCallback stringCallback) {
        HashMap hashMap = new HashMap();
        hashMap.put("sid", HeartBeatClient.getDeviceNo());
        OkHttpUtils.post()
                .url(ResourceConst.REMOTE_RES.SCREEN_UPLOAD_URL)
                .params(hashMap)
                .addFile("screenimage",imgUrl,new File(imgUrl))
                .tag(this)
                .build()
                .execute(stringCallback);
    }

    public void breakPointDownLoad(String url, DownloadListener listener) {
        if (downloadTask == null) {
            downloadTask = new DownloadTask(listener);
        }
        downloadTask.execute(url);
    }

    private String lastCacheUrl;

    public void downLoadFile(final String url, final OnDownLoadListener onDownLoadListener) {
        if (TextUtils.equals(url, lastCacheUrl)) {
            LogUtil.E("此文件正在下载。");
            return;
        }
        lastCacheUrl = url;
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        onDownLoadListener.onStart(fileName);
        OkHttpUtils.getInstance().cancelTag(this);

        if (!url.startsWith("http://") && !url.startsWith("https")) {
            onDownLoadListener.onError(new Exception("Unsupported download protocols"));
            onDownLoadListener.onFinish();
            return;
        }

        try {
            File file = new File(rootDir);
            String[] list = file.list();
            for (String fn : list) {
                if (fn.contains(fileName)) {
                    LogUtil.E("内存中已有该文件");
                    onDownLoadListener.onComplete(new File(rootDir + "/" + fn));
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
                                downLoadFile(url, onDownLoadListener);
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
        } catch (NullPointerException | IllegalArgumentException e) {
            onDownLoadListener.onError(e);
        }

    }

    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());
                map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
                map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
                map.put("diskSpace", CommonUtils.getMemoryTotalSize());
                map.put("useSpace", CommonUtils.getMemoryUsedSize());
                map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + Const.VERSION_TYPE.TYPE);
//                map.put("screenRotate", String.valueOf(SystemProperties.get("persist.sys.hwrotation")));
                map.put("deviceCpu", CommonUtils.getCpuName() + " " + CommonUtils.getNumCores() + "核" + CommonUtils
                        .getMaxCpuFreq() + "khz");
                map.put("deviceIp", CommonUtils.getIpAddress());//当前设备IP地址
                map.put("mac", CommonUtils.getLocalMacAddress());//设备的本机MAC地址
                NetUtil.getInstance()
                        .post(ResourceConst.REMOTE_RES.UPLOAD_DEVICE_INFO, map, new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                LogUtil.E(e.getMessage());
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                LogUtil.E(response);
                            }
                        });
            }
        });
    }

    public interface OnDownLoadListener {
        void onStart(String fileName);

        void onDownloading(int progress);

        void onComplete(File response);

        void onFinish();

        void onError(Exception e);
    }

}
