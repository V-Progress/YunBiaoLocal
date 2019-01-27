package com.yunbiao.cccm.resolve;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MenuActivity;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.DialogUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class VideoDataResolver {
    private static VideoDataResolver instance;
    private final String RES_DIR = ResourceConst.LOCAL_RES.RES_SAVE_PATH;
    //播放列表和预览列表
    public static List<String> playList = new ArrayList<>();
    public static Map<String, String> previewMap = new HashMap<>();
    public static List<PlayModel> playModelList = new ArrayList<>();
    private List<Timer> timerList;

    private final String todayStr;
    private final String tommStr;


    public List<String> getPlayList() {
        return playList;
    }

    public synchronized static VideoDataResolver getInstance() {
        if (instance == null) {
            instance = new VideoDataResolver();
        }
        return instance;
    }

    private VideoDataResolver() {
        todayStr = DateUtil.getTodayStr();
        tommStr = DateUtil.getTommStr();
    }

    private void showProgress() {
        MenuActivity menuActivity = APP.getMenuActivity();
        if (menuActivity != null && menuActivity.isForeground()) {
            DialogUtil.getInstance().showProgressDialog(menuActivity, "读取本地资源", "读取中...");
        }
    }

    private void dissmissProgress(String msg) {
        MenuActivity menuActivity = APP.getMenuActivity();
        if (menuActivity != null && menuActivity.isForeground()) {
            DialogUtil.getInstance().dissmissProgress(APP.getMainActivity(), msg);
        }
    }

    // 解析本地资源
    public void resolveLocalResource() {
        showProgress();
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                File yunbiao = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);

                //目录是否存在或可读
                if (!yunbiao.exists() || !yunbiao.canRead()) {
                    dissmissProgress(yunbiao.getName() + "目录不存在或拒绝读取");
                    return;
                }

                //筛选yunbiao目录下所有20xx-20xx类的目录
                File[] files = yunbiao.listFiles(new VideoDirectoryFilter());
                if (files == null || files.length == 0) {
                    dissmissProgress(yunbiao.getName() + "目录没有资源");
                    return;
                }

                //文件夹排序
                Map<Long, File> fileMap = new HashMap<>();
                Long[] fileKey = new Long[files.length];
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    fileKey[i] = Long.valueOf(file.getName().replace("-", ""));
                    fileMap.put(fileKey[i], file);
                }
                Arrays.sort(fileKey);
                files = new File[files.length];
                for (int j = 0; j < files.length; j++) {
                    files[j] = fileMap.get(fileKey[j]);
                }

                //清空播放列表
                playList = new ArrayList<>();
                previewMap = new HashMap<>();

                //关闭所有定时播放任务
                if (timerList != null) {
                    for (Timer timer : timerList) {
                        timer.cancel();
                    }
                }
                timerList = new ArrayList<>();

                //开关机时间
                String[] timerString = null;

                //解析播放列表
                for (File file : files) {
                    VideoDataModel videoDataModel = new XMLParse().parseVideoModel(file);
                    List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();

                    for (VideoDataModel.Play play : playlist) {
                        String playDay = play.getPlayday().trim();
                        List<VideoDataModel.Play.Rule> rules = play.getRules();

                        //解析播放日期
                        String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));
                        for (VideoDataModel.Play.Rule rule : rules) {
                            String[] times = rule.getDate().trim().split("-");

                            playList.add(playDate + "\t\t\t" + times[0] + "-" + times[1]);

                            String[] res = rule.getRes().trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");

                            final List<String> videoList = new ArrayList<String>();

                            for (int k = 0; k < res.length; k++) {

                                String videoStr = res[k];
                                //分割名称
                                String[] pathStr = videoStr.split("/");
                                String videoName = pathStr[pathStr.length - 1].trim().replace("\n", "");

                                File video = new File(file, "resource/" + videoStr);
                                String index = k + 1 > 9 ? k + 1 + " " : k + 1 + "  ";
                                if (!video.exists()) {
                                    playList.add(index + videoStr + "(无)");
                                    continue;
                                }
                                playList.add(index + videoStr);

                                previewMap.put(/*DateUtil.yyyyMMdd_Format(new Date()) + res[k]*/videoName, video.getPath());

                                videoList.add(video.getPath());
                            }

                            //获取开关机时间
                            if (timerString == null) {
                                timerString = new String[]{videoDataModel.getConfig().getStart().trim(), videoDataModel.getConfig().getEnd().trim()};
                            }

                            //没有可播的放视频时，不添加定时任务
                            if (videoList.size() == 0) {
                                continue;
                            }

                            //添加定时任务
//                                LogUtil.E("123", "开始时间" + playDay + times[0]);
//                                LogUtil.E("123", "结束时间" + playDay + times[1]);
                            Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                            Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);
                            LogUtil.E(file.getPath() + "  " + DateUtil.yyyy_MM_dd_HH_mm_Format(beginTime) + " 至 " + DateUtil.yyyy_MM_dd_HH_mm_Format(endTime));

                            //播放结束时间小于当前时间时，不添加定时任务
                            if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                                continue;
                            }

                            //播放定时任务
                            Timer beginTimer = new Timer();
                            beginTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainController.getInstance().startPlay(videoList);
                                        }
                                    });
                                }
                            }, beginTime);

                            //停止定时任务
                            Timer endTimer = new Timer();
                            endTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainController.getInstance().stopPlay();
                                        }
                                    });
                                }
                            }, new Date(endTime.getTime() - 10000));

                            //添加定时任务到任务列表
                            timerList.add(beginTimer);
                            timerList.add(endTimer);
                        }
                    }

//                    if (timerString != null && !TextUtils.isEmpty(timerString[0]) && !TextUtils.isEmpty(timerString[1])) {
//                        timer = "开机时间：" + timerString[0] + "\n关机时间：" + timerString[1];
//                    }
//                        //定时开关机
//                        if (timerString != null) {
//                            PowerOffTool.setPowerRunTime(timerString[0], timerString[1]);
//                        }
                }
                dissmissProgress("读取完毕，共有：" + playList.size() + "条数据");

            }
        });
    }

    /***
     * 新的初始化播放列表的方法
     */
    public void initPlayList() {
        playList.clear();
        previewMap.clear();
        playModelList.clear();

        String todayResource = CacheManager.FILE.getTodayResource();
        VideoDataModel todayData = null;
        if (!TextUtils.isEmpty(todayResource)) {
            todayData = new Gson().fromJson(todayResource, VideoDataModel.class);
        }
        if (todayData != null) {
            resolvePlayLists(todayData, todayStr);

            createTimer(playModelList);
        }

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
                playList.add(playDate + "\t\t\t" + times[0] + "-" + times[1]);

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

                    //生成File
                    File videoFile = new File(RES_DIR, videoName);
                    if (!videoFile.exists()) {
                        playList.add(index + videoName + "(无)");
                        continue;
                    }

                    videoList.add(videoFile.getPath());
                    playList.add(index + videoName);
                    previewMap.put(videoName, videoFile.getPath());
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
        if(playModelList == null && playModelList.size() <= 0){
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
