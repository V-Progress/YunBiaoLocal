package com.yunbiao.cccm.net.resource.resolve;

import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;
import com.yunbiao.cccm.sdOperator.HighVerSDController;
import com.yunbiao.cccm.sdOperator.LowVerSDController;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigDataResolver {
    private static ConfigDataResolver instance;

    private static List<PlayModel> playModelList = new ArrayList<>();
    private List<Timer> timerList;

    private final String todayStr;
    private final String tommStr;

    public static ConfigDataResolver getInstance() {
        if (instance == null) {
            synchronized (ConfigDataResolver.class) {
                if (instance == null) {
                    instance = new ConfigDataResolver();
                }
            }
        }
        return instance;
    }

    private ConfigDataResolver() {
        todayStr = DateUtil.getTodayStr();
        tommStr = DateUtil.getTommStr();
    }

    /***
     * 新的初始化播放列表的方法
     */
    public void initPlayList() {
        //初始化之前先清除所有的资源
        ResourceConst.clearPalyList();
        playModelList.clear();

        //初始化今天的资源
        String todayResource = CacheManager.FILE.getTodayResource();
        VideoDataModel todayData = null;
        if (!TextUtils.isEmpty(todayResource)) {
            todayData = new Gson().fromJson(todayResource, VideoDataModel.class);
        }
        if (todayData != null) {
            resolvePlayLists(todayData, todayStr);

            createTimer(playModelList);
        }

        //初始化明天的资源
        String tommResource = CacheManager.FILE.getTommResource();
        VideoDataModel tommData = null;
        if (!TextUtils.isEmpty(tommResource)) {
            tommData = new Gson().fromJson(tommResource, VideoDataModel.class);
        }

        if (tommData != null) {
            resolvePlayLists(tommData, tommStr);
        }
    }

    private void resolvePlayLists(VideoDataModel videoDataModel, String date) {
        if (videoDataModel == null) {
            return;
        }

        List<VideoDataModel.Play> list = videoDataModel.getPlaylist();

        //获取每天的播放数据
        for (VideoDataModel.Play play : list) {
            String playDay = play.getPlayday().trim();
            //只解析当前日期的数据
            if (!TextUtils.equals(date, playDay)) {
                continue;
            }

            List<VideoDataModel.Play.Rule> rules = play.getRules();
            //解析播放日期
            String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));

            for (VideoDataModel.Play.Rule rule : rules) {
                PlayModel playModel = new PlayModel();
                List<String> videoList = new ArrayList<>();

                String[] times = rule.getDate().trim().split("-");//播放时间
                ResourceConst.addPlayItem(playDate + "\t\t\t" + times[0] + "-" + times[1]);

                //分割单条
                String[] ress = rule.getRes().split(",");
                for (int ind = 0; ind < ress.length; ind++) {
                    String videoName = ress[ind].trim().replace("\n", "");
                    //分割名称
                    if (TextUtils.isEmpty(videoName)) {
                        continue;
                    }
                    //生成播放列表的index
                    String index = ind + 1 > 9 ? ind + 1 + " " : ind + 1 + "  ";

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        //生成File
                        File video = LowVerSDController.instance().findResource(videoName);
                        if (!video.exists()) {
                            ResourceConst.addPlayItem(index + videoName + "(无)");
                            continue;
                        }

                        LogUtil.E("视频路径："+Uri.fromFile(video).toString());
                        videoList.add(Uri.fromFile(video).toString());
                        ResourceConst.addPlayItem(index + videoName);
                        ResourceConst.addPreviewItem(videoName, Uri.fromFile(video).toString());
                    } else {
                        DocumentFile video = HighVerSDController.instance().findResource(videoName);
                        if (video == null || (!video.exists())) {
                            ResourceConst.addPlayItem(index + videoName + "(无)");
                            continue;
                        }

                        LogUtil.E("视频路径："+video.getUri().toString());
                        videoList.add(video.getUri().toString());
                        ResourceConst.addPlayItem(index + videoName);
                        ResourceConst.addPreviewItem(videoName, video.getUri().toString());
                    }
                }

                //没有可播的放视频时，不添加定时任务
                if (videoList.size() <= 0) {
                    continue;
                }

                //添加定时任务
                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);
                if (beginTime == null || endTime == null) {
                    continue;
                }

                //播放结束时间小于当前时间时，不添加定时任务
                if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                    continue;
                }

                playModel.setStartTime(beginTime);
                playModel.setEndTime(endTime);
                playModel.setVideoList(videoList);
                playModelList.add(playModel);
            }
        }
    }

    private void createTimer(List<PlayModel> list) {
        if (playModelList == null && playModelList.size() <= 0) {
            return;
        }

        //关闭所有定时播放任务
        if (timerList != null) {
            for (Timer timer : timerList) {
                timer.cancel();
            }
        }
        timerList = new ArrayList<>();

        for (final PlayModel playModel : list) {
            Date startTime = playModel.getStartTime();
            Date endTime = playModel.getEndTime();

            Timer startTimer = new Timer();
            Timer endTimer = new Timer();
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainController.getInstance().startPlay(playModel.getVideoList());
                }
            }, startTime);

            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainController.getInstance().stopPlay();
                }
            }, endTime);

            timerList.add(startTimer);
            timerList.add(endTimer);
        }
    }

    class PlayModel {
        private Date startTime;
        private Date endTime;
        private List<String> videoList;

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public List<String> getVideoList() {
            return videoList;
        }

        public void setVideoList(List<String> videoList) {
            this.videoList = videoList;
        }

        @Override
        public String toString() {
            return "PlayModel{" +
                    "startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", videoList=" + videoList +
                    '}';
        }
    }
}
