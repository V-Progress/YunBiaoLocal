package com.yunbiao.cccm.resolve;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class VideoDataResolver {
    private final String RES_DIR = ResourceConst.LOCAL_RES.RES_SAVE_PATH;
    public static List<String> playList;
    public static Map<String, String> previewMap;
    public static String timer = "开机时间：--:--\n关机时间：--:--";
    private List<Timer> timerList;

    public List<String> getPlayList() {
        if (playList != null && playList.size() > 0) {
            return playList;
        }
        return new ArrayList<>();
    }

    public void resolvePlayLists() {
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                String todayResource = CacheManager.FILE.getTodayResource();
                if (TextUtils.isEmpty(todayResource)) {
                    LogUtil.E("今日数据缓存为null");
                    return;
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

                VideoDataModel videoDataModel = new Gson().fromJson(todayResource, VideoDataModel.class);

                List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
                try {
                    //获取每天的播放数据
                    for (VideoDataModel.Play play : playlist) {
                        String playDay = play.getPlayday().trim();
                        List<VideoDataModel.Play.Rule> rules = play.getRules();

                        //解析播放日期
                        String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));

                        for (VideoDataModel.Play.Rule rule : rules) {
                            String[] times = rule.getDate().trim().split("-");
                            playList.add(playDate + "\t\t\t" + times[0] + "-" + times[1]);

                            final StringBuilder videoPath = new StringBuilder();
                            //分割单条
                            String[] ress = rule.getRes().split(",");
                            for (int ind = 0; ind < ress.length; ind++) {
                                String videoStr = ress[ind];
                                //分割名称
                                String[] pathStr = videoStr.split("/");
                                String videoName = pathStr[pathStr.length - 1].trim().replace("\n", "");
                                if (!TextUtils.isEmpty(videoName)) {
                                    String index = ind + 1 > 9 ? ind + 1 + " " : ind + 1 + "  ";
                                    File video = new File(RES_DIR, videoName);
                                    if (!video.exists()) {
                                        playList.add(index + videoName + "(无)");
                                        continue;
                                    }
                                    playList.add(index + videoName);
                                    // TODO: 2018/12/26 暂时只用文件名做主键
                                    previewMap.put(/*DateUtil.yyyyMMdd_Format(new Date()) + */videoName, video.getPath());
                                    if (videoPath.length() > 0) {
                                        videoPath.append(",");
                                    }
                                    videoPath.append(video.getPath());
                                }
                            }

                            //获取开关机时间
                            if (timerString == null) {
                                timerString = new String[]{videoDataModel.getConfig().getStart().trim(), videoDataModel.getConfig().getEnd().trim()};
                            }

                            //没有可播的放视频时，不添加定时任务
                            if (videoPath.length() == 0) {
                                continue;
                            }

                            //添加定时任务
//                            LogUtil.E("123", "开始时间" + playDay + times[0]);
//                            LogUtil.E("123", "结束时间" + playDay + times[1]);
                            Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                            Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);
//                            LogUtil.E(/*file.getPath() + */"  " + DateUtil.yyyy_MM_dd_HH_mm_Format(beginTime) + " 至 " + DateUtil.yyyy_MM_dd_HH_mm_Format(endTime));

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
                                            MainController.getInstance().startPlay(videoPath.toString());
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

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    // TODO: 2018/12/28 解析本地资源
    public void resolveLocalResource() {
        Log.d("log", "播放设置");
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                File yunbiao = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);

                //目录是否存在或可读
                if (!yunbiao.exists() || !yunbiao.canRead()) {
                    LogUtil.E(yunbiao.getName() + " 目录不存在或拒绝读取");
                    return;
                }

                //筛选yunbiao目录下所有20xx-20xx类的目录
                File[] files = yunbiao.listFiles(new VideoDirectoryFilter());
                if (files == null || files.length == 0) {
                    LogUtil.E(yunbiao.getName() + " 没有资源");
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
                    try {
                        for (VideoDataModel.Play play : playlist) {
                            String playDay = play.getPlayday().trim();
                            List<VideoDataModel.Play.Rule> rules = play.getRules();

                            //解析播放日期
                            String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));
                            for (VideoDataModel.Play.Rule rule : rules) {
                                String[] times = rule.getDate().trim().split("-");

                                playList.add(playDate + "\t\t\t" + times[0] + "-" + times[1]);

                                String[] res = rule.getRes().trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");
                                final StringBuilder videoPath = new StringBuilder();

                                for (int k = 0; k < res.length; k++) {
                                    File video = new File(file, "resource/" + res[k]);
                                    String index = k + 1 > 9 ? k + 1 + " " : k + 1 + "  ";
                                    if (!video.exists()) {
                                        playList.add(index + res[k] + "(无)");
                                        continue;
                                    }
                                    playList.add(index + res[k]);

                                    previewMap.put(DateUtil.yyyyMMdd_Format(new Date()) + res[k], video.getPath());
                                    if (videoPath.length() > 0)
                                        videoPath.append(",");
                                    videoPath.append(video.getPath());
                                }

                                //获取开关机时间
                                if (timerString == null) {
                                    timerString = new String[]{videoDataModel.getConfig().getStart().trim(), videoDataModel.getConfig().getEnd().trim()};
                                }

                                //没有可播的放视频时，不添加定时任务
                                if (videoPath.length() == 0) {
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
                                                MainController.getInstance().startPlay(videoPath.toString());
                                            }
                                        });
                                    }
                                }, beginTime);

                                //停止定时任务
                                Timer endTimer = new Timer();
                                endTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Log.d("log", "停止播放");
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

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    /*if (timerString != null && !TextUtils.isEmpty(timerString[0]) && !TextUtils.isEmpty(timerString[1])) {
                        timer = "开机时间：" + timerString[0] + "\n关机时间：" + timerString[1];
                    }
                    //定时开关机
                    if (timerString != null) {
                        PowerOffTool.setPowerRunTime(timerString[0], timerString[1]);
                    }*/
                }
            }
        });
    }
}
