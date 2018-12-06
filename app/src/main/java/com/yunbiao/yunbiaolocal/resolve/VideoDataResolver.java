package com.yunbiao.yunbiaolocal.resolve;

import android.util.Log;

import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.utils.DateUtil;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.ThreadUtil;

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
    public static List<String> playList;
    public static Map<String, String> previewMap;
    public static String timer = "开机时间：--:--\n关机时间：--:--";
    public List<Timer> timerList;

    public void setPlayList() {
        Log.d("log", "播放设置");
        ThreadUtil.getInstance().runInFixedThread(new Runnable() {
            @Override
            public void run() {
                File yunbiao = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);

                if (!yunbiao.canRead()) {
                    LogUtil.E(yunbiao.getName() + " 目录不存在或拒绝读取");
                    return;
                }

                File[] files = yunbiao.listFiles(new VideoDirectoryFilter());//筛选yunbiao目录下所有20xx-20xx类的目录
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
                    VideoDataModel videoDataModel = new XMLParse().parseJsonModel(file);
                    List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
                    try {
                        for (VideoDataModel.Play play : playlist) {
                            String playDay = play.getPlayday().trim();
                            List<VideoDataModel.Play.Rule> rules = play.getRules();

                            String playDate = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playDay));
                            for (VideoDataModel.Play.Rule rule : rules) {
                                String[] time = rule.getDate().trim().split("-");

                                playList.add(playDate + "\t\t\t" + time[0] + "-" + time[1]);

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
                                    timerString = new String[]{videoDataModel.getStart().trim(), videoDataModel.getEnd().trim()};
                                }

                                //没有可播的放视频时，不添加定时任务
                                if (videoPath.length() == 0) {
                                    continue;
                                }

                                //添加定时任务
                                LogUtil.E("123", "开始时间" + playDay + time[0]);
                                LogUtil.E("123", "结束时间" + playDay + time[1]);
                                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + time[0]);
                                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + time[1]);
                                Log.d("log", file.getPath() + "  " + DateUtil.yyyy_MM_dd_HH_mm_Format(beginTime) + " 至 " + DateUtil.yyyy_MM_dd_HH_mm_Format(endTime));

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
                                                APP.getMainActivity().vtmPlay(videoPath.toString());
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
                                                APP.getMainActivity().vtmStop();
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


                    /*JSONObject parseXML = new XMLParse().parseXML(file);//把XML转换成JSONObject
                    try {
                        JSONArray playlist = parseXML.getJSONArray("playlist");
                        for (int i = 0; i < playlist.length(); i++) {
                            JSONObject play = playlist.getJSONObject(i);
                            String playday = play.getString("playday").trim();
                            JSONArray rules = play.getJSONArray("rules");
                            String date = DateUtil.yyyy_MM_dd_Format(DateUtil.yyyyMMdd_Parse(playday));
                            for (int j = 0; j < rules.length(); j++) {
                                JSONObject rule = rules.getJSONObject(j);
                                String[] time = rule.getString("date").trim().split("-");
                                playList.add(date + "\t\t\t" + time[0] + "-" + time[1]);
                                String[] res = rule.getString("res").trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");
                                final StringBuilder videoPath = new StringBuilder();
                                //生成播放列表
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
                                if (timerString == null)
                                    timerString = new String[]{parseXML.getString("start").trim(), parseXML.getString("end").trim()};
                                //没有可播的放视频时，不添加定时任务
                                if (videoPath.length() == 0)
                                    continue;

                                //添加定时任务
                                Log.e("123", "开始时间" + playday + time[0]);
                                Log.e("123", "结束时间" + playday + time[1]);
                                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playday + time[0]);
                                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playday + time[1]);

                                //播放结束时间小于当前时间时，不添加定时任务
                                if ((endTime.getTime() - 10000 < System.currentTimeMillis()))
                                    continue;
                                //播放定时任务
                                Timer beginTimer = new Timer();
                                beginTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                APP.getMainActivity().vtmPlay(videoPath.toString());
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
                                                APP.getMainActivity().vtmStop();
                                            }
                                        });
                                    }
                                }, new Date(endTime.getTime() - 10000));
                                Log.d("log", file.getPath() + "  " + DateUtil.yyyy_MM_dd_HH_mm_Format(beginTime) + " 至 " + DateUtil.yyyy_MM_dd_HH_mm_Format(endTime));

                                //添加定时任务到任务列表
                                timerList.add(beginTimer);
                                timerList.add(endTimer);
                            }
                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (timerString != null && !TextUtils.isEmpty(timerString[0]) && !TextUtils.isEmpty(timerString[1]))
                    timer = "开机时间：" + timerString[0] + "\n关机时间：" + timerString[1];
                //定时开关机
                if (timerString != null)
                    PowerOffTool.setPowerRunTime(timerString[0], timerString[1]);*/
