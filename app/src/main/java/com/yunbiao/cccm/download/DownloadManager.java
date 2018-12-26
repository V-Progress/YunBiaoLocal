package com.yunbiao.cccm.download;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.resolve.VideoDataModel;
import com.yunbiao.cccm.resolve.XMLParse;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.VIntent;
import okhttp3.Call;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DownloadManager {
    private final String TAG = getClass().getSimpleName();
    private final long downloadDelay = 60000;//失败后延迟下载时间
    private final String REQ_FAILED_TAG = "-1";
    private final int TYPE_TODAY = 1;
    private final int TYPE_TOMMO = 2;
    private boolean isInit = true;//是否init，只有在是的情况下，会从1开始请求，请求到2的时候会将isInit置为false
    private String ftpServiceUrl;//FTP文件地址

    private static DownloadManager instance;
    private final List<String> urlList;

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    public DownloadManager() {
        urlList = new ArrayList<>();
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initResData() {
        isInit = true;
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(String.valueOf(TYPE_TODAY));
            }
        });
    }

    /***
     * 单项请求configXML，将isInit置为false，请求完毕后不会重复请求。
     * @param type
     */
    public void requestConfigXML(final String type) {
        isInit = false;
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(type);
            }
        });
    }

    private void requestRes(final String type) {
        if(isInit){
            //开启控制台
            MainController.getInstance().openConsole();
            MainController.getInstance().updateConsole("检查播放资源...");
        }

        LogUtil.E("日期类型是：" + type);
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("type", type);
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.GET_RESOURCE, map, new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

                LogUtil.E(TAG, e.getMessage());
                MainController.getInstance().updateConsole("检查资源失败...");
                MainController.getInstance().closeConsole();
//                try {
//                    Thread.sleep(downloadDelay);
//                    requestRes(type);//请求异常之后再次请求
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
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
                downloadConfigXML(type,configUrl);
            }
        });
    }

    //下载config文件
    private void downloadConfigXML(final String type, final String url) {
        NetUtil.getInstance().downloadFile(url, ResourceConst.LOCAL_RES.APP_MAIN_DIR, new NetUtil.OnDownLoadListener() {
            @Override
            public void onStart(String fileName) {
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "下载中---" + progress);
            }

            @Override
            public void onComplete(File response) {
                LogUtil.D(TAG, "下载完成：" + response.getAbsolutePath());
                List<String> strings = null;
                try {
                    strings = resolveDownloadList(type,response);

                    download(strings);
                } catch (UnsupportedEncodingException e) {

                    MainController.getInstance().updateConsole("下载失败...");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onError(Exception e) {
                LogUtil.E(TAG, "下载错误：" + e.getMessage() + ",60秒后重新下载");
                MainController.getInstance().updateConsole("下载失败...");
                MainController.getInstance().closeConsole();
//                try {
//                    Thread.sleep(downloadDelay);
//                    LogUtil.D(TAG, "重新开始下载config文件");
//                    downloadConfigXML(type, url);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
            }
        });
    }

    //解析XML文件
    private List<String> resolveDownloadList(String type, File response) throws UnsupportedEncodingException {

        VideoDataModel videoDataModel = new XMLParse().parseJsonModel(response);
        String videoModleStr = new Gson().toJson(videoDataModel);

        Integer typeInt = Integer.valueOf(type);
        switch (typeInt) {
            case TYPE_TODAY:
                String todayResource = CacheManager.FILE.getTodayResource();
                LogUtil.E("对比1："+todayResource);
                LogUtil.E("对比2："+videoModleStr);
                if(!TextUtils.equals(videoModleStr,todayResource)){
                    CacheManager.FILE.putTodayResource(videoModleStr);
                }
                break;
            case TYPE_TOMMO:
                String tommorowResource = CacheManager.FILE.getTommorowResource();
                LogUtil.E("对比1："+tommorowResource);
                LogUtil.E("对比2："+videoModleStr);
                if(!TextUtils.equals(videoModleStr,tommorowResource)){
                    CacheManager.FILE.putTommorowResource(videoModleStr);
                }
                break;
        }

        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        for (VideoDataModel.Play play : playlist) {
            String today = DateUtil.yyyyMMdd_Format(new Date());
            //判断是否有今天的数据
            if (TextUtils.equals(play.getPlayday(), today)) {
                List<VideoDataModel.Play.Rule> rules = play.getRules();
                for (VideoDataModel.Play.Rule rule : rules) {
                    String res = rule.getRes();
                    String[] split = res.split(",");
                    for (String rPath : split) {
                        String urlStr = rPath.replace("\n", "").trim().replace("%20", "");
                        if (!TextUtils.isEmpty(urlStr)) {
                            if (!urlList.contains(urlStr)) {
                                urlList.add(ftpServiceUrl + urlStr);
                            }

                        }
                    }
                }
            }
        }
        return urlList;
    }

    //开始多文件下载
    private void download(final List<String> urlList) {
        BPDownloadUtil.getInstance().breakPointDownload(urlList, new MutiFileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                if(isInit){
                    MainController.getInstance().updateConsole("共有：" + totalNum+"个文件");
                    MainController.getInstance().initProgress(totalNum);
                }
            }

            @Override
            public void onStart(int currNum) {
                MainController.getInstance().updateConsole("开始下载第"+currNum+1+"个文件");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.E(TAG, "进度：" + progress);
            }

            @Override
            public void onDownloadSpeed(long speed) {
            }

            @Override
            public void onSuccess(int currFileNum) {
                MainController.getInstance().updateProgress(currFileNum+1);
                MainController.getInstance().updateConsole("下载完成");
                if(isInit){
                    MainController.getInstance().initPlayData();
                }
            }

            @Override
            public void onError(Exception e) {
                MainController.getInstance().updateConsole("下载错误:"+e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onFinish() {
                urlList.clear();
                if (isInit) {
                    MainController.getInstance().updateProgress(urlList.size());
                    MainController.getInstance().updateConsole("已全部下载结束");
                    MainController.getInstance().closeConsole();
                    LogUtil.D("dataTag不为2，继续开始请求");
                    requestConfigXML(String.valueOf(TYPE_TOMMO));//请求明天时会将isInit置为false
                }
            }

            @Override
            public void onFailed(Exception e) {
                MainController.getInstance().updateConsole("下载失败，请检查网络或Config:"+e.getMessage());
            }
        });
    }
}
