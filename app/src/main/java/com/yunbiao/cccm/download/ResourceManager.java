package com.yunbiao.cccm.download;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.netcore.bean.ConfigResponse;
import com.yunbiao.cccm.resolve.VideoDataModel;
import com.yunbiao.cccm.resolve.XMLParse;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * 现请求获取config，下载config，解析config，下载资源
 * 为了便于管理，请求全部为同步请求
 * <p>
 * Created by Administrator on 2018/12/21.
 */

public class ResourceManager {
    private final String TAG = getClass().getSimpleName();
    private final String REQ_FAILED_TAG = "-1";
    private final int TYPE_TODAY = 1;
    private final int TYPE_TOMMO = 2;
    private boolean isInit = false;
    private String ftpServiceUrl;//FTP文件地址

    private static ResourceManager instance;
    private final List<String> urlList;
    private BPDownloadUtil downloadUtil;
    private String currDownloadPlayDay;
    private int totalNum;

    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public ResourceManager() {
        urlList = new ArrayList<>();
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initResData() {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(TYPE_TODAY);
            }
        });
    }

    private void requestRes(final int type) {
        //判断当前请求的类型，如果是今天的，将初始化置为true，以便请求结束的时候
        isInit = type == TYPE_TODAY;
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        String date = isInit ? DateUtil.getToday_str() : DateUtil.getTomm_str();
        LogUtil.D(TAG, "请求" + date + "的数据");
        map.put("playDate", date);
//        map.put("type",String.valueOf(isInit?TYPE_TODAY:TYPE_TOMMO));

        try {
            //请求获取资源
            Response response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
            if (response == null) {
                throw new IOException("request play Resource Error");
            }
            //取出响应
            String responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                throw new IOException("response's body is null");
            }
            LogUtil.D(TAG, "播放资源：" + responseStr);

            //转换成bean
            ConfigResponse configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.D(TAG, configResponse.getMessage());
                throw new Exception(configResponse.getMessage());
            }

            //获取两种地址
            ConfigResponse.Data dataJson = configResponse.getDataJson();
            ftpServiceUrl = dataJson.getFtpServiceUrl();
            String configUrl = dataJson.getConfigUrl();

            //下载config文件
            Response configXml = NetUtil.getInstance().downloadSync(configUrl);
            if (configXml == null) {
                throw new IOException("download response's body is null");
            }

            //取出config内容
            String configStr = configXml.body().string();
            if (TextUtils.isEmpty(configStr)) {
                throw new IOException("config.xml is null");
            }

            //转换成bean
            VideoDataModel videoDataModel = new XMLParse().parseVideoModel(configStr);
            String videoDataStr = new Gson().toJson(videoDataModel);
            LogUtil.D(TAG, "Config：" + videoDataStr);

            //缓存
            if (isInit) {
                //判断如果是今天的数据再进行缓存
                CacheManager.FILE.putTodayResource(videoDataStr);
            }else{
                CacheManager.FILE.putTommResource(videoDataStr);
            }

            //解析下载地址列表
            List<String> urlList = resolveDownloadList(videoDataModel);

            //开始下载
            download(urlList);

        } catch (Exception e) {
            e.printStackTrace();
            //请求明天时会将isInit置为false
            if(isInit){
                requestRes(TYPE_TOMMO);
                MainController.getInstance().updateMenu(false);
            }else{
                MainController.getInstance().closeConsole();
            }
            LogUtil.E(TAG, "处理播放资源出现异常：" + e.getMessage());
        }
    }

    //解析XML文件
    private List<String> resolveDownloadList(VideoDataModel videoDataModel) {
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        String playurl = videoDataModel.getConfig().getPlayurl();

        String dayStr = DateUtil.yyyyMMdd_Format(new Date());

        if (!isInit) {//如果是明天的数据则取明天的时间
            dayStr = DateUtil.getTommStr(dayStr);
        }

        for (VideoDataModel.Play play : playlist) {
            String playday = play.getPlayday();

            //不是今天也不是明天的数据
            if (TextUtils.isEmpty(playday)) {
                continue;
            }

            if (TextUtils.equals(playday, dayStr)) {

                currDownloadPlayDay = playday;

                List<VideoDataModel.Play.Rule> rules = play.getRules();
                for (VideoDataModel.Play.Rule rule : rules) {
                    String res = rule.getRes();
                    String[] resArray = res.split(",");
                    for (String resName : resArray) {
                        resName = resName.replace("\n", "").trim().replace("%20", "");
                        if (!TextUtils.isEmpty(resName)) {
                            String resUrl = ftpServiceUrl + playurl + "/" + resName;
                            if (!urlList.contains(resUrl)) {
                                urlList.add(resUrl);
                            }
                        }
                    }
                }
            }
        }
//        LogUtil.D(TAG, "下载列表：" + urlList.toString());
        totalNum = urlList.size();
        return urlList;
    }

    //开始多文件下载
    private void download(final List<String> urlList) {
        if (downloadUtil != null) {
            downloadUtil.cancel();
        }
        downloadUtil = new BPDownloadUtil(getClass(), fileDownloadListener);
        downloadUtil.breakPointDownload(urlList);
    }

    FileDownloadListener fileDownloadListener = new FileDownloadListener() {
        @Override
        public void onBefore(int totalNum) {
            if(isInit){
                if(totalNum <= 0){
                    MainController.getInstance().updateMenu(false);
                }else{
                    MainController.getInstance().updateMenu(true);
                }
            }

            MainController.getInstance().openConsole();
            MainController.getInstance().initProgress(totalNum);

            String msg = ("准备下载"+currDownloadPlayDay+"的资源...共有：" + totalNum + "个文件");
            LogUtil.D(TAG, msg);
            MainController.getInstance().updateConsole(msg);
        }

        @Override
        public void onStart(int currNum) {
            LogUtil.D(TAG, "开始下载: " + currNum);
            if(isInit){
                MainController.getInstance().initPlayData(true);
            }
            MainController.getInstance().updateParentProgress(currNum);
            MainController.getInstance().updateChildProgress(0);
            MainController.getInstance().updateConsole("开始下载第" + currNum + "个文件");
        }

        @Override
        public void onProgress(int progress) {
            MainController.getInstance().updateChildProgress(progress);
        }

        @Override
        public void onSuccess(int currFileNum, BPDownloadUtil.DownloadInfo downloadInfo) {
            LogUtil.D(TAG, "下载成功: " + currFileNum);
            MainController.getInstance().updateParentProgress(currFileNum);
            MainController.getInstance().updateConsole("下载完成");
            NetUtil.getInstance().uploadProgress(currDownloadPlayDay, currFileNum + "/" + totalNum, downloadInfo.getFileName(), true);
            if(!isInit){
                MainController.getInstance().updateList();
            }
        }

        @Override
        public void onError(int currFileNum, Exception e, BPDownloadUtil.DownloadInfo downloadInfo) {
            e.printStackTrace();
            LogUtil.D(TAG, "下载错误: " + e.getMessage());
            MainController.getInstance().updateConsole("下载错误:" + e.getMessage());
            NetUtil.getInstance().uploadProgress(currDownloadPlayDay, currFileNum + "/" + totalNum, downloadInfo.getFileName(), false);
        }

        @Override
        public void onFinish() {
            LogUtil.D(TAG, "下载结束");
            urlList.clear();

            MainController.getInstance().updateParentProgress(urlList.size());
            MainController.getInstance().updateConsole(currDownloadPlayDay + "的资源下载完毕");

            if (isInit) {
                MainController.getInstance().initPlayData(true);
                requestRes(TYPE_TOMMO);//请求明天时会将isInit置为false
            } else {
                MainController.getInstance().updateList();
                MainController.getInstance().updateConsole("已全部下载结束");
                MainController.getInstance().closeConsole();
            }
        }

        @Override
        public void onFailed(Exception e) {
            e.printStackTrace();
            LogUtil.D(TAG, "下载失败：" + e.getMessage());
            MainController.getInstance().updateConsole("下载失败，请检查网络或Config:" + e.getMessage());
        }
    };
}
