package com.yunbiao.cccm.net.resource;

import android.text.TextUtils;
import android.view.animation.ScaleAnimation;

import com.google.gson.Gson;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.activity.PlayListFragment;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.net.model.ConfigResponse;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;
import com.yunbiao.cccm.net.resource.resolve.ConfigResolver;
import com.yunbiao.cccm.net.resource.resolve.XMLParse;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.net.download.BPDownloadManager;

import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

/**
 * 现请求获取config，下载config，解析config，下载资源
 * 为了便于管理，请求全部为同步请求
 * <p>
 * Created by Administrator on 2018/12/21.
 */
// TODO: 2019/3/8  
public class ResourceManager {
    private final String TAG = getClass().getSimpleName();
    private static ResourceManager instance;

    private final long dayMilliSec = 24 * 60 * 60 * 1000;
    private final int REQ_DAY_NUM = 7;

    private static final String REQ_FAILED_TAG = "-1";
    private static final String FAILED_REQ_CONFIG = "1";//请求config失败
    private static final String FAILED_DOWNLOAD_CONFIG = "2";//下载config失败
    private static final String FAILED_RESOLVE_CONFIG = "3";//解析config失败

    private final Date todayDate;
    private String ftpServiceUrl;//FTP文件地址
    private Queue<DlBean> urlListQueue = new LinkedList();
    private List<String> reqDatelist;
    private Downloader downloader;

    public static List<VideoDataModel.Play> playList = new ArrayList<>();


    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public ResourceManager() {
        reqDatelist = new ArrayList<>();
        todayDate = DateUtil.getTodayDate();

        for (int i = 0; i < REQ_DAY_NUM; i++) {
            String date = DateUtil.yyyy_MM_dd_Format(new Date(todayDate.getTime() + (dayMilliSec * i)));
            reqDatelist.add(date);
        }
    }

    /***
     * 加载已缓存的网络播放数据
     */
    public void loadData() {
        ConfigResolver.getInstance().initPlayList();
    }

    private class DlBean {
        String date;
        VideoDataModel.Play play;
        List<String> urlList;

        @Override
        public String toString() {

            return "DlBean{" +
                    "date='" + date + '\'' +
                    ", play=" + (play != null) +
                    ", urlList=" + urlList.size() +
                    '}';
        }
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initNetData() {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                clearPlayList();

                if (reqDatelist == null || reqDatelist.size() <= 0) {
                    LogUtil.E(TAG, "请求日期列表为空，异常");
                    return;
                }

                for (String date : reqDatelist) {
                    //开始请求
                    String reCode = reqConfig(date);
                    Date reqDate = DateUtil.yyyy_MM_dd_Parse(date);

                    switch (reCode) {
                        case REQ_FAILED_TAG://没有数据

                            if (reqDate != null && todayDate.equals(reqDate)) {
                                MainController.getInstance().setHasConfig(false);
                            }
                            break;
                        case FAILED_REQ_CONFIG://请求失败
                        case FAILED_DOWNLOAD_CONFIG:
                        case FAILED_RESOLVE_CONFIG:
                            NetUtil.getInstance().uploadProgress(date, "0/0", "", Integer.valueOf(reCode));
                            break;
                        default://请求成功
                            VideoDataModel videoDataModel = new Gson().fromJson(reCode, VideoDataModel.class);
                            VideoDataModel.Config config = videoDataModel.getConfig();
                            if (config == null) {
                                LogUtil.E(TAG, "下载Url为空，异常");
                                continue;
                            }

                            String playUrl = config.getPlayurl();
                            if (TextUtils.isEmpty(playUrl)) {
                                LogUtil.E(TAG, "下载Url为空，异常");
                                continue;
                            }
                            List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
                            for (VideoDataModel.Play play : playlist) {
                                String playday = play.getPlayday();
                                Date playDate = DateUtil.yyyyMMdd_Parse(playday);
                                String playDay = DateUtil.yyyy_MM_dd_Format(playDate);

                                //如果不是请求日的数据
                                if (!TextUtils.equals(date, playDay)) {
                                    continue;
                                }

                                //如果数据为空
                                List<VideoDataModel.Play.Rule> rules = play.getRules();
                                if (rules == null || rules.size() <= 0) {
                                    continue;
                                }

                                //如果是今天的数据
                                if (todayDate.equals(DateUtil.yyyy_MM_dd_Parse(date))) {
                                    MainController.getInstance().setHasConfig(true);
                                }

                                DlBean dlBean = new DlBean();
                                dlBean.date = date;
                                dlBean.play = play;
                                dlBean.urlList = resolveUrl(playUrl, rules);
                                updatePlayList(false,play);
                                urlListQueue.offer(dlBean);

                            }
                            break;
                    }
                }
                down(urlListQueue);
            }
        });
    }

    public void clearPlayList(){
        updatePlayList(true,null);
    }

    public void updatePlayList(boolean isClear,VideoDataModel.Play play){
        if(isClear){
            playList.clear();
        } else {
            playList.add(play);
        }
        EventBus.getDefault().post(PlayListFragment.UPDATE_TAG);
    }

    public List<VideoDataModel.Play> getPlayList(){
        return playList;
    }

    /***
     * 解析当前下载地址
     * @param playUrl config中配置的Url
     * @param rules config中的rules
     * @return UrlList
     */
    private List<String> resolveUrl(String playUrl, List<VideoDataModel.Play.Rule> rules) {
        List<String> urlList = new ArrayList<>();

        for (VideoDataModel.Play.Rule rule : rules) {
            String[] resArray = rule.getRes().split(",");
            for (String resName : resArray) {
                resName = resName.replace("\n", "").trim().replace("%20", "");
                if (TextUtils.isEmpty(resName))
                    continue;

                String resUrl = ftpServiceUrl + playUrl + "/" + resName;
                if (urlList.contains(resUrl))
                    continue;

                urlList.add(resUrl);
            }
        }

        return urlList;
    }

    /***
     * 请求config文件
     * @param date 需要获取的日期
     * @return 返回数字请求失败，返回Json请求成功
     */
    private String reqConfig(String date) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("playDate", date);
        LogUtil.D(TAG, "开始获取" + date + "的数据：" + map.toString());

        //请求获取资源
        Response response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
        if (response == null) {
            LogUtil.E(TAG, "获取Config地址失败，无响应");
            return FAILED_REQ_CONFIG;
        }
        //取出响应
        String responseStr = null;
        try {
            responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                LogUtil.E(TAG, "获取Config地址失败：response为null");
                return FAILED_REQ_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "获取Config地址失败：获取响应体失败" + e.getMessage());
            return FAILED_REQ_CONFIG;
        }
        LogUtil.D(TAG, "获取Config地址响应：" + responseStr);

        ConfigResponse configResponse;
        try {
            //转换成bean
            configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.D(TAG, date + "没有Config文件");
                return REQ_FAILED_TAG;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.E(TAG, "解析config地址响应失败：" + e.getClass().getSimpleName() + " " + e.getMessage());
            return FAILED_REQ_CONFIG;
        }

        //获取两种地址
        ConfigResponse.Data dataJson = configResponse.getDataJson();
        ftpServiceUrl = dataJson.getFtpServiceUrl();
        String configUrl = dataJson.getConfigUrl();

        //下载config文件
        Response configXml;
        try {
            configXml = NetUtil.getInstance().downloadSync(configUrl);
            if (configXml == null) {
                LogUtil.E(TAG, "下载config文件失败：下载结果为null");
                return FAILED_DOWNLOAD_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "下载config文件失败：IOException " + e.getMessage());
            return FAILED_DOWNLOAD_CONFIG;
        }

        //取出config内容
        String configStr;
        try {
            configStr = configXml.body().string();
            if (TextUtils.isEmpty(configStr)) {
                LogUtil.E(TAG, "下载config文件失败：空串");
                return FAILED_DOWNLOAD_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "下载config文件失败：IOException " + e.getMessage());
            return FAILED_DOWNLOAD_CONFIG;
        }

        //转换成bean
        VideoDataModel videoDataModel;
        try {
            videoDataModel = new XMLParse().parseVideoModel(configStr);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "config文件解析失败：IOException " + e.getMessage());
            return FAILED_RESOLVE_CONFIG;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "config文件解析失败：XmlPullParserException " + e.getMessage());
            return FAILED_RESOLVE_CONFIG;
        }

        String videoDataStr = new Gson().toJson(videoDataModel);
        LogUtil.D(TAG, date + "的config文件下载成功：" + videoDataStr);
        return videoDataStr;
    }

    private void down(final Queue<DlBean> queue) {
        cancel();
        downloader = new Downloader(queue.poll(), new FinishListener() {
            @Override
            public void finish() {
                down(queue);
            }
        });
        downloader.go();
    }

    public void cancel() {
        if (downloader != null) {
            downloader.cancel();
            downloader = null;
        }
    }

    interface FinishListener {
        void finish();
    }

    class Downloader extends FileDownloadListener {
        private int totalNum = 0;
        private int currNum = 0;
        final double BYTES_PER_MIB = 1024 * 1024;
        boolean isRunning = false;
        private List<String> urlList;
        private BPDownloadManager downloadManager;
        private String date;
        private long realSpeed;
        private FinishListener finishListener;
        private Timer timer;//计算速度监听

        public Downloader(DlBean dlBean, FinishListener listener) {
            date = dlBean.date;
            urlList = dlBean.urlList;
            totalNum = urlList.size();
            finishListener = listener;
        }

        //开始多文件下载
        private void go() {
            cancel();
            downloadManager = new BPDownloadManager(getClass(), this);
            downloadManager.startDownload(urlList);
        }

        public void cancel() {
            if (downloadManager != null) {
                downloadManager.cancel();
            }
        }

        @Override
        public void onBefore(int totalNum) {
            MainController.getInstance().initProgress(totalNum);
            String msg = ("准备下载" + date + "的资源...共有：" + totalNum + "个文件");
            MainController.getInstance().updateConsole(msg);

            startGetSpeed();//开始计算速度
        }

        @Override
        public void onStart(int currNum) {
            if (todayDate.equals(DateUtil.yyyy_MM_dd_Parse(date))) {
//                MainController.getInstance().initPlayData();
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
        public void onDownloadSpeed(long speed) {
            realSpeed += speed;
        }

        @Override
        public void onSuccess(int currFileNum, int totalNum, String fileName) {
            MainController.getInstance().updateParentProgress(currFileNum);
            MainController.getInstance().updateConsole("第" + currFileNum + "个文件下载完成：" + fileName);
            NetUtil.getInstance().uploadProgress(date, currFileNum + "/" + totalNum, fileName, YunBiaoException.SUCCESS);
        }

        @Override
        public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
            String errMsg;
            if (!TextUtils.isEmpty(e.getMessage())) {
                errMsg = e.getMessage();
            } else {
                errMsg = e.getClass().getSimpleName();
            }
            MainController.getInstance().updateConsole("第" + currFileNum + "个文件下载错误:" + errMsg);

            if (e instanceof YunBiaoException) {
                YunBiaoException ye = (YunBiaoException) e;
                if (ye.getErrCode() == YunBiaoException.FAILED_REQ_CONFIG) {
                    LogUtil.E("====" + CacheManager.FILE.getTodayResource());
                    ;
                    CacheManager.FILE.putTodayResource("");
                }

                NetUtil.getInstance().uploadProgress(TextUtils.isEmpty(date) ? "" : date, currFileNum + "/" + totalNum, fileName, ye.getErrCode());
            }
        }

        @Override
        public void onFinish() {
            cancelSpeedTimer();

            MainController.getInstance().updateParentProgress(urlList.size());
            MainController.getInstance().updateConsole(date + "的资源下载完毕");

            finishListener.finish();
        }

        @Override
        public void onCancel() {
            MainController.getInstance().updateConsole("已取消下载");
        }

        //开始计算速度
        private void startGetSpeed() {
            if (!isRunning) {
                if (timer == null) {
                    timer = new Timer();
                }
                isRunning = true;
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
            isRunning = false;
        }

    }

/*    private void requestRes(final int type) {
        //判断当前请求的类型，如果是今天的，将初始化置为true，以便请求结束的时候
        isInit = type == TYPE_TODAY;
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        String date = isInit ? DateUtil.getToday_str() : DateUtil.getTomm_str();
        map.put("playDate", date);
        LogUtil.D(TAG, "获取" + date + "的数据：" + map.toString());

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
    }*/
/*
    //开始多文件下载
    private void download(final List<String> urlList) {
        cancel();
        downloadManager = new BPDownloadManager(getClass(), fileDownloadListener);
        downloadManager.startDownload(urlList);
    }

    public void cancel() {
        if (downloadManager != null) {
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
                MainController.getInstance().setHasConfig(totalNum > 0);
            }

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
                LogUtil.E(TAG, "初始化数据111");
                MainController.getInstance().initPlayData();
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
                if (ye.getErrCode() == YunBiaoException.FAILED_REQ_CONFIG) {
                    LogUtil.E("====" + CacheManager.FILE.getTodayResource());
                    ;
                    CacheManager.FILE.putTodayResource("");
                }

                NetUtil.getInstance().uploadProgress(TextUtils.isEmpty(currDownloadPlayDay) ? "" : currDownloadPlayDay, currFileNum + "/" + totalNum, fileName, ye.getErrCode());
            }
        }

        @Override
        public void onFinish() {
            LogUtil.D(TAG, "下载结束");
//            urlList.clear();
            cancelSpeedTimer();

//            MainController.getInstance().updateParentProgress(urlList.size());
            MainController.getInstance().updateConsole(currDownloadPlayDay + "的资源下载完毕");


        }

        @Override
        public void onCancel() {
            MainController.getInstance().updateConsole("已取消下载");
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
    };*/
}
