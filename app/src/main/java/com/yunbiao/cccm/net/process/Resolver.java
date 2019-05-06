package com.yunbiao.cccm.net.process;

import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.net.model.VideoDataModel;
import com.yunbiao.cccm.sd.HighVerSDController;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.utils.DateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/4/26.
 */

public class Resolver {
    private static List<Timer> todayTimerList = new ArrayList<>();

    public static void clearTimer() {
        for (Timer timer : todayTimerList) {
            timer.cancel();
        }
        todayTimerList.clear();
    }

    /***
     * 解析当前下载地址
     * @param playUrl config中配置的Url
     * @param rules config中的rules
     * @return UrlList
     */
    public static List<String> resolveUrl(String ftpUrl, String playUrl, List<VideoDataModel.Play.Rule> rules) {
        List<String> urlList = new ArrayList<>();

        for (VideoDataModel.Play.Rule rule : rules) {
            String[] resArray = rule.getRes().split(",");
            for (String resName : resArray) {
                resName = resName.replace("\n", "").trim().replace("%20", "");
                if (TextUtils.isEmpty(resName))
                    continue;

                String resUrl = ftpUrl + playUrl + "/" + resName;
                if (urlList.contains(resUrl))
                    continue;

                urlList.add(resUrl);
            }
        }

        return urlList;
    }

    public static List<String> resolvePlay(VideoDataModel.Play play) {
        if (play == null) {
            return null;
        }
        List<VideoDataModel.Play.Rule> rules = play.getRules();
        String playDay = play.getPlayday().trim();
        String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));
        List<String> tempPL = new ArrayList<>();
        for (VideoDataModel.Play.Rule rule : rules) {
            String[] times = rule.getDate().trim().split("-");//播放时间
            tempPL.add("*" + playDate + "\t\t\t" + times[0] + "-" + times[1]);
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

                tempPL.add(index + "*" + videoName);
            }
        }

        return tempPL;
    }

    public static void resolveTodayData(VideoDataModel.Play todayPlay) {
        for (Timer timer : todayTimerList) {
            timer.cancel();
        }

        if (todayPlay == null) {
            return;
        }

        String playDay = todayPlay.getPlayday();
        List<VideoDataModel.Play.Rule> rules = todayPlay.getRules();

        for (VideoDataModel.Play.Rule rule : rules) {
            final List<String> videoList = new ArrayList<>();

            String[] times = rule.getDate().trim().split("-");//播放时间
            //分割单条
            String[] ress = rule.getRes().split(",");
            for (int ind = 0; ind < ress.length; ind++) {
                //分割名称
                String videoName = ress[ind].trim().replace("\n", "");
                if (TextUtils.isEmpty(videoName)) {
                    continue;
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //生成File
                    File video = LowVerSDController.instance().findResource(videoName);
                    if (!video.exists()) {
                        continue;
                    }
                    videoList.add(Uri.fromFile(video).toString());
                } else {
                    DocumentFile video = HighVerSDController.instance().findResource(videoName);
                    if (video == null || (!video.exists())) {
                        continue;
                    }
                    videoList.add(video.getUri().toString());
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

            Timer startTimer = new Timer();
            Timer endTimer = new Timer();
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainController.getInstance().startPlay(videoList);
                }
            }, beginTime);

            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MainController.getInstance().stopPlay();
                }
            }, endTime);

            todayTimerList.add(startTimer);
            todayTimerList.add(endTimer);
        }
    }


}
