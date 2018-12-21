package com.yunbiao.yunbiaolocal.netcore;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.resolve.VideoDataModel;
import com.yunbiao.yunbiaolocal.resolve.XMLParse;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.TimerExecutor;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DownloadService extends Service {
    private final String TAG = getClass().getSimpleName();
    private final long downloadDelay = 60000;//失败后延迟下载时间
    private String ftpServiceUrl;//FTP文件地址

    @Override
    public void onCreate() {
        LogUtil.E(TAG, "onCreate");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.E(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.E(TAG, "onStartCommand");
        getResource("1");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final String REQ_FAILED_TAG = "-1";

    /***
     * 获取布局资源
     * @param id 1：今天 2：明天
     */
    public void getResource(final String id) {
        LogUtil.D(TAG, "开始获取布局资源");
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("type", id);

        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.GET_RESOURCE, map, new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtil.E(TAG, e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtil.D(TAG, "------------" + response);
                JSONObject jsonObject = JSON.parseObject(response);
                String result = jsonObject.getString("result");
                String message = jsonObject.getString("message");
                if (TextUtils.equals(REQ_FAILED_TAG, result)) {
                    LogUtil.E(TAG, message);
                    return;
                }

                JSONObject dateJson = jsonObject.getJSONObject("dateJson");
                ftpServiceUrl = dateJson.getString("ftpServiceUrl");

                String configUrl = dateJson.getString("configUrl");
                downloadConfigXML(configUrl);
            }
        });
    }

    //下载config文件
    private void downloadConfigXML(final String url) {
        NetUtil.getInstance().downloadFile(url, ResourceConst.LOCAL_RES.APP_MAIN_DIR, new NetUtil.OnDownLoadListener() {
            @Override
            public void onStart(String fileName) {
                LogUtil.D(TAG, "开始下载config文件");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "下载中---" + progress);
            }

            @Override
            public void onComplete(File response) {
                LogUtil.D(TAG, "下载完成：" + response.getAbsolutePath());
                resolveConfigXML(response);
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onError(Exception e) {
                LogUtil.E(TAG, "下载错误：" + e.getMessage() + ",60秒后重新下载");
                TimerExecutor.getInstance().delayExecute(downloadDelay, new TimerExecutor.OnTimeOutListener() {
                    @Override
                    public void execute() {
                        LogUtil.D(TAG, "重新开始下载config文件");
                        downloadConfigXML(url);
                    }
                });
            }
        });
    }

    private void resolveConfigXML(File response) {
        List<String> videoNameList = new ArrayList<>();
        VideoDataModel videoDataModel = new XMLParse().parseJsonModel(response);
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        for (VideoDataModel.Play play : playlist) {
            List<VideoDataModel.Play.Rule> rules = play.getRules();
            for (VideoDataModel.Play.Rule rule : rules) {
                String res = rule.getRes();
                String[] split = res.split(",");
                for (String rPath : split) {
                    if (!TextUtils.isEmpty(rPath.replace("\n", "").trim())) {
                        videoNameList.add(new StringBuilder(ftpServiceUrl).append(rPath).toString());
                    }
                }

            }
        }

        BPDownloadUtil.getInstance().breakPointDownload(videoNameList, new BPDownloadUtil.MutiFileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                LogUtil.E(TAG,"准备开始下载，共有：" + totalNum);
            }

            @Override
            public void onProgress(int currNum, int progress) {
                LogUtil.E(TAG,"当前下载的是第 " + currNum + " 个文件，进度：" + progress);
            }

            @Override
            public void onSuccess(int currFileNum) {
                LogUtil.E(TAG,"第 " + currFileNum + " 个文件下载完成");
            }

            @Override
            public void onFinish() {
                LogUtil.E(TAG,"资源全部下载结束");
            }

            @Override
            public void onError(Exception e) {
                LogUtil.E(TAG,"下载出现错误：" + e.getMessage());
            }
        });
    }
}
