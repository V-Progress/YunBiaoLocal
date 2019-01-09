package com.yunbiao.cccm.download;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.InsertManager;
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
import com.yunbiao.cccm.view.model.InsertVideoModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
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
    private final long downloadDelay = 60000;//失败后延迟下载时间
    private final String REQ_FAILED_TAG = "-1";
    private final int TYPE_TODAY = 1;
    private final int TYPE_TOMMO = 2;
    private boolean isInit = false;
    private String ftpServiceUrl;//FTP文件地址

    private static ResourceManager instance;
    private final List<String> urlList;

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

    /***
     * 单项请求configXML，将isInit置为false，请求完毕后不会重复请求。
     * @param type
     */
    public void requestConfigXML(final int type) {
        if (type == TYPE_TOMMO) {
            isInit = false;
        }
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(type);
            }
        });
    }

    private void requestRes(final int type) {
        if (type == TYPE_TODAY) {
            isInit = true;
        }

        LogUtil.D(TAG,"请求"+ (type == TYPE_TODAY?"今天":"明天") +"的数据");
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("type", String.valueOf(type));

        try {
            //请求获取config
            Response response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
            if (response == null) {
                throw new IOException("request play Resource Error");
            }
            //取出响应
            String responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                throw new IOException("response's body is null");
            }
            LogUtil.D(TAG,"播放资源："+responseStr);

            //转换成bean
            ConfigResponse configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.D(TAG, configResponse.getMessage());
                if (isInit) {
                    requestConfigXML(TYPE_TOMMO);//请求明天时会将isInit置为false
                }
                return;
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

            //缓存
            if (type == TYPE_TODAY) {
                //判断如果是今天的数据再进行缓存
                CacheManager.FILE.putTodayResource(videoDataStr);
            }

            //解析下载地址
            List<String> urlList = resolveDownloadList(videoDataModel);

            //开始下载
            download(urlList);

        } catch (Exception e) {
            LogUtil.E(TAG,"处理播放资源出现异常："+e.getMessage());
        }
    }

    //解析XML文件
    private List<String> resolveDownloadList(VideoDataModel videoDataModel) {
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        String today = DateUtil.yyyyMMdd_Format(new Date());

        for (VideoDataModel.Play play : playlist) {
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
        BPDownloadUtil.getInstance().breakPointDownload(urlList, new FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                if (isInit) {
                    //开启控制台
                    if(totalNum <= 0){
                        return;
                    }
                    MainController.getInstance().openConsole();
                    MainController.getInstance().updateConsole("准备下载资源...共有：" + totalNum + "个文件");
                    MainController.getInstance().initProgress(totalNum);
                }
            }

            @Override
            public void onStart(int currNum) {
                MainController.getInstance().updateConsole("开始下载第" + (currNum + 1) + "个文件");
            }

            @Override
            public void onProgress(int progress) {
                MainController.getInstance().updateProgressStr(progress+"%");
            }

            @Override
            public void onSuccess(int currFileNum) {
                MainController.getInstance().updateProgress((currFileNum + 1));
                MainController.getInstance().updateConsole("下载完成");
            }

            @Override
            public void onError(Exception e) {
                MainController.getInstance().updateConsole("下载错误:" + e.getMessage());
            }

            @Override
            public void onFinish() {
                urlList.clear();
                if (isInit) {
                    MainController.getInstance().initPlayData(true);
                    MainController.getInstance().updateProgress(urlList.size());
                    MainController.getInstance().updateConsole("已全部下载结束");
                    MainController.getInstance().closeConsole();
                    LogUtil.D("dataTag不为2，继续开始请求");
                    requestConfigXML(TYPE_TOMMO);//请求明天时会将isInit置为false
                }
            }

            @Override
            public void onFailed(Exception e) {
                MainController.getInstance().updateConsole("下载失败，请检查网络或Config:" + e.getMessage());
            }
        });

    }

    public static abstract class FileDownloadListener implements MultiFileDownloadListener {
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
        public void onSuccess(int currFileNum) {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onFailed(Exception e) {

        }
    }
}
