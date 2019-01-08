package com.yunbiao.cccm.utils;

import android.os.SystemProperties;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.Const;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/11/22.
 */

public class NetUtil {
    private static NetUtil mInstance;
    private RequestCall build;
    private final String rootDir = ResourceConst.LOCAL_RES.APP_MAIN_DIR;

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

    public void get(String url, Callback callback) {
        OkHttpUtils.get()
                .url(url)
                .tag(this)
                .build()
                .execute(callback);
    }

    /***
     * 异步POST请求
     * @param url
     * @param params
     * @param stringCallback
     */
    public void post(String url, Map<String, String> params, StringCallback stringCallback) {
        OkHttpUtils.post()
                .url(url)
                .params(params)
                .tag(this)
                .build()
                .execute(stringCallback);
    }

    /***
     * 同步POST请求
     * @param url 地址
     * @param params 参数
     * @return
     */
    public Response postSync(String url, Map<String, String> params){
        Response response = null;
        try{
            response = OkHttpUtils.post().url(url).params(params).tag(this).build().execute();
        }catch (IOException e){
            LogUtil.E(url + " 请求失败:"+ e.getMessage() + " ，重新请求");
            try {
                response = OkHttpUtils.post().url(url).params(params).tag(this).build().execute();
            } catch (IOException e1) {
                LogUtil.E(url + " 请求失败:"+ e1.getMessage());
            }
        }
        return response;
    }

    /***
     * 同步GET下载
     * @param url 地址
     * @return 返回文件内容
     * @throws IOException
     */
    public Response downloadSync(String url) throws IOException {
        return OkHttpUtils.get().url(url).tag(this).build().execute();
    }

    /***
     * 异步文件下载
     * @param url
     * @param onDownLoadListener
     */
    public void downloadFile(final String url, final OnDownLoadListener onDownLoadListener) {
        downloadFile(url, rootDir, onDownLoadListener);
    }

    public void downloadFile(final String url, String localPath, final OnDownLoadListener onDownLoadListener) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        onDownLoadListener.onStart(fileName);
        OkHttpUtils.getInstance().cancelTag(this);

        if (!url.startsWith("http://") && !url.startsWith("https")) {
            onDownLoadListener.onError(new Exception("Unsupported download protocols"));
            onDownLoadListener.onFinish();
            return;
        }

        try {
            OkHttpUtils.get()
                    .url(url)
                    .tag(this)
                    .build()
                    .execute(new FileCallBack(localPath, fileName) {
                        int mainProg;

                        @Override
                        public void onError(Call call, Exception e, int id) {
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
                                onDownLoadListener.onProgress(i);
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

    /***
     * 截屏文件上传
     * @param imgUrl
     * @param stringCallback
     */
    public void postScreenShoot(String imgUrl, StringCallback stringCallback) {
        HashMap hashMap = new HashMap();
        hashMap.put("sid", HeartBeatClient.getDeviceNo());
        OkHttpUtils.post()
                .url(ResourceConst.REMOTE_RES.SCREEN_UPLOAD_URL)
                .params(hashMap)
                .addFile("screenimage", imgUrl, new File(imgUrl))
                .tag(this)
                .build()
                .execute(stringCallback);
    }

    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());
                map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
                map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
                map.put("diskSpace", CommonUtils.getMemoryTotalSize());
                map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + Const.VERSION_TYPE.TYPE);

                String ori = SystemProperties.get("persist.sys.hwrotation");
                LogUtil.E("当前屏幕方向：" + ori);
                map.put("screenRotate", ori);
                map.put("deviceCpu", CommonUtils.getCpuName() + " " + CommonUtils.getNumCores() + "核" + CommonUtils
                        .getMaxCpuFreq() + "khz");
                map.put("useSpace", CommonUtils.getMemoryUsedSize());

                map.put("latitude", CacheManager.SP.getLatitude());
                map.put("longitude", CacheManager.SP.getLongitude());
                map.put("address", CacheManager.SP.getAddress());
                map.put("cityName", CacheManager.SP.getCityName());
                map.put("addressHeight", CacheManager.SP.getAltitude());
                map.put("mac", CommonUtils.getLocalMacAddress());//设备的本机MAC地址
                map.put("camera", CommonUtils.checkCamera());
                map.put("deviceIp", CommonUtils.getIpAddress());//当前设备IP地址
                LogUtil.E("上传设备信息：" + map.toString());
                post(ResourceConst.REMOTE_RES.UPLOAD_DEVICE_INFO, map, new StringCallback() {
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

        void onProgress(int progress);

        void onComplete(File response);

        void onFinish();

        void onError(Exception e);
    }

}
