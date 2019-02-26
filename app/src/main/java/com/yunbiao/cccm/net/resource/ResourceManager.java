package com.yunbiao.cccm.net.resource;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.net.bean.ConfigResponse;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;
import com.yunbiao.cccm.net.resource.resolve.VideoDataResolver;
import com.yunbiao.cccm.net.resource.resolve.XMLParse;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.net.download.BPDownloadManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private String currDownloadPlayDay;
    private BPDownloadManager downloadManager;

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
     * 加载已缓存的网络播放数据
     */
    public void loadNetData() {
        VideoDataResolver.getInstance().initPlayList();
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initNetData() {
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
        map.put("playDate", date);
        LogUtil.D(TAG, "请求" + date + "的数据");

        try {
            //请求获取资源
            Response response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
            if (response == null) {
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_REQ_CONFIG, null), 0, 0, "");
                throw new IOException("request play Resource Error");
            }
            //取出响应
            String responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_REQ_CONFIG, null), 0, 0, "");
                throw new IOException("response's body is null");
            }
            LogUtil.D(TAG, "播放资源：" + responseStr);

            //转换成bean
            ConfigResponse configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.D(TAG, configResponse.getMessage());
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_REQ_CONFIG, new Exception(configResponse.getMessage())), 0, 0, "");
                throw new Exception(configResponse.getMessage());
            }

            //获取两种地址
            ConfigResponse.Data dataJson = configResponse.getDataJson();
            ftpServiceUrl = dataJson.getFtpServiceUrl();
            String configUrl = dataJson.getConfigUrl();

            //下载config文件
            Response configXml = NetUtil.getInstance().downloadSync(configUrl);
            if (configXml == null) {
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_DOWNLOAD_CONFIG, null), 0, 0, "");
                throw new IOException("download response's body is null");
            }

            //取出config内容
            String configStr = configXml.body().string();
            if (TextUtils.isEmpty(configStr)) {
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_DOWNLOAD_CONFIG, null), 0, 0, "");
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
            } else {
                CacheManager.FILE.putTommResource(videoDataStr);
            }

            //解析下载地址列表
            List<String> urlList = resolveDownloadList(videoDataModel);

            //开始下载
            download(urlList);

        } catch (Exception e) {
            e.printStackTrace();
            //请求明天时会将isInit置为false
            if (e instanceof XmlPullParserException) {
                fileDownloadListener.onError(new YunBiaoException(YunBiaoException.FAILED_RESOLVE_CONFIG, e), 0, 0, "");
            }
            if (isInit) {
                requestRes(TYPE_TOMMO);
                MainController.getInstance().setHasConfig(false);
            } else {
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

                currDownloadPlayDay = DateUtil.yyyy_MM_dd_Format(playday);

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
        return urlList;
    }

    //开始多文件下载
    private void download(final List<String> urlList) {
        cancel();
        downloadManager = new BPDownloadManager(getClass(),fileDownloadListener);
        downloadManager.startDownload(urlList);
    }

    public void cancel() {
        if(downloadManager != null){
            downloadManager.cancel();
        }
    }

    FileDownloadListener fileDownloadListener = new FileDownloadListener() {

        private Timer timer;//计算速度监听
        private long realSpeed = 0;//实时下载速度
        private boolean isRuning = false;//计算速度监听是否已开启
        final double BYTES_PER_MIB = 1024 * 1024;//计算基数，M

        @Override
        public void onBefore(int totalNum) {
            if (isInit) {
                if (totalNum <= 0) {
                    MainController.getInstance().setHasConfig(false);
                } else {
                    MainController.getInstance().setHasConfig(true);
                }
            }

            MainController.getInstance().openConsole();
            MainController.getInstance().initProgress(totalNum);

            String msg = ("准备下载" + currDownloadPlayDay + "的资源...共有：" + totalNum + "个文件");
            LogUtil.D(TAG, msg);
            MainController.getInstance().updateConsole(msg);

            startGetSpeed();//开始计算速度
        }

        @Override
        public void onStart(int currNum) {
            LogUtil.D(TAG, "开始下载: " + currNum);
            if (isInit) {
                LogUtil.E(TAG,"初始化数据111");
                MainController.getInstance().initPlayData();
            }

            MainController.getInstance().updateList();

            MainController.getInstance().updateParentProgress(currNum);
            MainController.getInstance().updateChildProgress(0);
            MainController.getInstance().updateConsole("开始下载第" + currNum + "个文件");
        }

        @Override
        public void onProgress(int progress) {
            MainController.getInstance().updateChildProgress(progress);
        }

        @Override
        public void onDownloadSpeed(long speed) {
            realSpeed += speed;
        }

        @Override
        public void onSuccess(int currFileNum, int totalNum, String fileName) {
            LogUtil.D(TAG, "下载成功: " + currFileNum);
            MainController.getInstance().updateParentProgress(currFileNum);
            MainController.getInstance().updateConsole("第" + currFileNum + "个文件下载完成：" + fileName);
            NetUtil.getInstance().uploadProgress(currDownloadPlayDay, currFileNum + "/" + totalNum, fileName, YunBiaoException.SUCCESS);
        }

        @Override
        public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
            String errMsg;
            if (!TextUtils.isEmpty(e.getMessage())) {
                errMsg = e.getMessage();
            } else {
                errMsg = e.getClass().getSimpleName();
            }
            LogUtil.D(TAG, "下载错误: " + errMsg);
            MainController.getInstance().updateConsole("第" + currFileNum + "个文件下载错误:" + errMsg);

            if (e instanceof YunBiaoException) {
                YunBiaoException ye = (YunBiaoException) e;
                NetUtil.getInstance().uploadProgress(TextUtils.isEmpty(currDownloadPlayDay)?"":currDownloadPlayDay, currFileNum + "/" + totalNum, fileName, ye.getErrCode());
            }
        }

        @Override
        public void onFinish() {
            LogUtil.D(TAG, "下载结束");
            urlList.clear();
            cancelSpeedTimer();

            MainController.getInstance().updateParentProgress(urlList.size());
            MainController.getInstance().updateConsole(currDownloadPlayDay + "的资源下载完毕");

            if (isInit) {
                MainController.getInstance().initPlayData();
                requestRes(TYPE_TOMMO);//请求明天时会将isInit置为false
            } else {
                MainController.getInstance().updateList();
                MainController.getInstance().updateConsole("已全部下载结束");
                MainController.getInstance().closeConsole();
            }
        }

        @Override
        public void onCancel() {
            MainController.getInstance().updateConsole("已取消下载");
            MainController.getInstance().closeConsole();
        }

        //开始计算速度
        private void startGetSpeed() {
            if (!isRuning) {
                if (timer == null) {
                    timer = new Timer();
                }
                isRuning = true;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        double v = realSpeed / BYTES_PER_MIB;
                        String speed;
                        if (v < 1) {
                            v = v * 1024;
                            speed = String.format("%.1f", v) + "k/s";
                        } else {
                            speed = v + "m/s";
                        }
                        MainController.getInstance().updateSpeed(speed);
                        realSpeed = 0;
                    }
                }, 1000, 1000);
            }
        }

        //取消计算
        private void cancelSpeedTimer() {
            MainController.getInstance().updateSpeed("0k/s");
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            isRuning = false;
        }
    };
}
