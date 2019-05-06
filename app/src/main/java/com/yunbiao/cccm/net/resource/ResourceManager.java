package com.yunbiao.cccm.net.resource;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.net.download.BPDownloadManager;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.net.process.Requester;
import com.yunbiao.cccm.net.process.Resolver;
import com.yunbiao.cccm.net.process.Retryer;
import com.yunbiao.cccm.utils.BackupUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.net.model.VideoDataModel;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

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

    private static Date todayDate;
    private static Date tommDate;

    private Queue<Downloader.DlBean> urlListQueue = new LinkedList();
    private List<String> reqDatelist = new ArrayList<>();
    private Downloader downloader;
    private static List<String> playList = new ArrayList<>();
    private static Queue<String> faileQueue = new LinkedList<>();

    //今日数据缓存（只有每次请求完毕时才会初始化）
    private static VideoDataModel.Play todayPlay;

    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public ResourceManager() {
        todayDate = DateUtil.getTodayDate();
        tommDate = DateUtil.getTommDate(DateUtil.getTodayStr());

        for (int i = 0; i < REQ_DAY_NUM; i++) {
            String date = DateUtil.yyyy_MM_dd_Format(new Date(todayDate.getTime() + (dayMilliSec * i)));
            reqDatelist.add(date);
        }
    }

    /***
     * 加载已缓存的网络播放数据
     */
    public void loadData() {
        Resolver.resolveTodayData(todayPlay);
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

                BackupUtil.readBackup(new BackupUtil.ReadBackupListener() {
                    @Override
                    public void getData(List<VideoDataModel.Play> list) {
                        MainController.getInstance().updateConsole("检查备份...共有"+list.size()+"个列表");

                        for (VideoDataModel.Play play : list) {
                            String playday = play.getPlayday();
                            Date date = DateUtil.yyyyMMdd_Parse(playday);
                            if (date.equals(todayDate)) {
                                todayPlay = play;
                                Resolver.resolveTodayData(todayPlay);//如果有今天的数据，则直接开始播放，未请求到时也不会暂停，请求到以后该内容会被替换，并且重新解析
                                continue;
                            } else if (date.after(todayDate)) {
                                continue;//今天之后的数据不解析
                            }
                            List<String> tempList = Resolver.resolvePlay(play);
                            if (tempList != null || tempList.size() > 0) {
                                playList.addAll(tempList);
                            }
                        }
                        EventBus.getDefault().postSticky(new UpdateEvent(UpdateEvent.UPDATE_PLAYLIST));

                        MainController.getInstance().updateConsole("请求Config...");
                        Requester.requestConfig(reqDatelist,new Requester.RequestConfigListener() {
                            @Override
                            public void noData(Date reqDate) {
                                String date = DateUtil.yyyy_MM_dd_Format(reqDate);
                                MainController.getInstance().updateConsole(date+" 暂无数据");
                                //检查今日数据，如果有则解析播放
                                if (todayDate.equals(reqDate)) {
                                    List<String> tempList = Resolver.resolvePlay(todayPlay);
                                    if (tempList != null && tempList.size() > 0) {
                                        playList.addAll(tempList);
                                    }
                                    MainController.getInstance().setHasConfig(false);
                                }
                            }

                            @Override
                            public void onError(Date reqDate, String errCode) {
                                if (todayDate.equals(reqDate)) {
                                    List<String> tempList = Resolver.resolvePlay(todayPlay);
                                    if (tempList != null || tempList.size() > 0) {
                                        playList.addAll(tempList);
                                    }
                                }
                                String date = DateUtil.yyyy_MM_dd_Format(reqDate);
                                MainController.getInstance().updateConsole(date+" 请求失败");
                                NetUtil.getInstance().uploadProgress(date, "0/0", "", Integer.valueOf(errCode));
                            }

                            @Override
                            public void onSuccess(Date reqDate,String ftpServiceUrl, String playUrl, VideoDataModel videoDataModel) {
                                String date = DateUtil.yyyy_MM_dd_Format(reqDate);
                                MainController.getInstance().updateConsole(date+" 请求成功");
                                List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
                                for (VideoDataModel.Play play : playlist) {
                                    String playday = play.getPlayday();
                                    Date playDate = DateUtil.yyyyMMdd_Parse(playday);
                                    if (!reqDate.equals(playDate)) {
                                        continue;//如果不是请求日的数据就跳过
                                    }

                                    List<String> tempList = Resolver.resolvePlay(play);
                                    if (tempList != null || tempList.size() > 0) {
                                        playList.addAll(tempList);//解析播放列表
                                    }

                                    List<VideoDataModel.Play.Rule> rules = play.getRules();
                                    if (rules == null || rules.size() <= 0) {
                                        continue;//如果数据为空
                                    }

                                    //如果是今天的数据
                                    if (todayDate.equals(playDate)) {
                                        MainController.getInstance().setHasConfig(true);
                                        LogUtil.E("123", "今日数据：" + play.toString());
                                        todayPlay = play;
                                        BackupUtil.backup(DateUtil.yyyyMMdd_Format(todayDate), todayPlay);
                                    }

                                    Downloader.DlBean dlBean = new Downloader.DlBean();
                                    dlBean.date = date;
                                    dlBean.play = play;
                                    dlBean.urlList = Resolver.resolveUrl(ftpServiceUrl, playUrl, rules);
                                    urlListQueue.offer(dlBean);
                                }
                            }

                            @Override
                            public void onFinish() {
                                EventBus.getDefault().postSticky(new UpdateEvent(UpdateEvent.UPDATE_PLAYLIST));

                                MainController.getInstance().updateConsole("请求Config结束，准备下载...");
                                cancel();
                                down(urlListQueue);
                            }
                        });
                    }
                });
            }
        });
    }

    /***
     * 开始下载
     * @param queue
     */
    private void down(final Queue<Downloader.DlBean> queue) {
        if (queue == null || queue.size() <= 0) {
            if (faileQueue.size() > 0) {
                MainController.getInstance().updateConsole("检测到近两天内有下载失败的数据");
                Retryer retryer = new Retryer(faileQueue, new Retryer.FinishListener() {
                    @Override
                    public void finish() {
                        Resolver.resolveTodayData(todayPlay);
                        down(queue);
                    }
                });
                retryer.start();
            }
            return;
        }

        Downloader.DlBean poll = queue.peek();
        Date reqDate = DateUtil.yyyy_MM_dd_Parse(poll.date);
        if (reqDate.after(tommDate) && faileQueue.size() > 0) {//日期不是今天也不是明天的时候检查错误列表
            MainController.getInstance().updateConsole("检测到近两天内有下载失败的数据");
            Retryer retryer = new Retryer(faileQueue, new Retryer.FinishListener() {
                @Override
                public void finish() {
                    Resolver.resolveTodayData(todayPlay);
                    down(queue);
                }
            });
            retryer.start();
            return;
        }
        poll = queue.poll();

        //正常下载
        downloader = new Downloader(poll, new Downloader.FinishListener() {
            @Override
            public void onLatelyResourceError(String s) {
                faileQueue.offer(s);
            }

            @Override
            public void finish() {
                down(queue);
            }
        });
        downloader.setDate(todayDate, tommDate);
        downloader.go();
    }

    /****
     * 取消下载
     */
    public void cancel() {
        if (downloader != null) {
            downloader.cancel();
            downloader = null;
        }
    }

    public List<String> getPlayList() {
        return playList;
    }

    private void clearPlayList() {
        playList.clear();
    }

    public void clearTimer() {
        Resolver.clearTimer();
    }


    /***
     * 下载内部类
     */
    static class Downloader extends FileDownloadListener {
        interface FinishListener {
            void onLatelyResourceError(String s);
            void finish();
        }

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
        private Date todayDate;
        private Date tommDate;

        public Downloader(Downloader.DlBean dlBean, Downloader.FinishListener listener) {
            date = dlBean.date;
            urlList = dlBean.urlList;
            totalNum = urlList.size();
            finishListener = listener;
        }

        public void setDate(Date tdDate,Date tmDate){
            todayDate = tdDate;
            tommDate = tmDate;
        }

        //开始多文件下载
        public void go() {
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
                Resolver.resolveTodayData(todayPlay);
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

            // TODO: 2019/4/12 如果是今天和明天的数据则添加到错误列表中
            Date reqDate = DateUtil.yyyy_MM_dd_Parse(date);
            if(reqDate.equals(todayDate) || reqDate.equals(tommDate)){
                for (String fileUrl : urlList) {
                    boolean contains = fileUrl.contains(fileName);
                    if(contains){
                        if(finishListener != null){
                            finishListener.onLatelyResourceError(fileUrl);
                        }
                    }
                }
            }
            // TODO: 2019/4/12 如果是今天和明天的数据则添加到错误列表中

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

        public static class DlBean {
            public String date;
            public VideoDataModel.Play play;
            public List<String> urlList;

            @Override
            public String toString() {

                return "DlBean{" +
                        "date='" + date + '\'' +
                        ", play=" + (play != null) +
                        ", urlList=" + urlList.size() +
                        '}';
            }
        }
    }

}
