package com.yunbiao.cccm.yunbiaolocal.io;

import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.yunbiaolocal.MainActivity;
import com.yunbiao.cccm.yunbiaolocal.sd.HighVerSDController;
import com.yunbiao.cccm.yunbiaolocal.sd.LowVerSDController;
import com.yunbiao.cccm.yunbiaolocal.utils.LogUtil;
import com.yunbiao.cccm.yunbiaolocal.xboot.stdcall.PowerOffTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Video {
    public static List<String> playList;
    public static Map<String, String> previewMap;
    public static String timer = "开机时间：--:--\n关机时间：--:--";
    public List<Timer> timerList;

    public void initPlayList(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            setPlayList();
        } else {
            setPL();
        }
    }

    private void setPL(){
        DocumentFile yunbiao = HighVerSDController.instance().getAppRootDir();
        if(yunbiao == null || (!yunbiao.exists())){
            return;
        }
        LogUtil.D("yunbiao:"+yunbiao.getUri());

        DocumentFile[] documentFiles = yunbiao.listFiles();
        if(documentFiles == null || documentFiles.length <= 0){
            return;
        }
        LogUtil.D("files:"+documentFiles.toString());

        List<DocumentFile>  docList = new ArrayList<>();
        for (DocumentFile documentFile : documentFiles) {
            if(documentFile.isDirectory() && documentFile.getName().matches("201\\d{5}-201\\d{5}")){
                docList.add(documentFile);
            }
        }

        //清空播放列表
        playList = new ArrayList<>();
        previewMap = new HashMap<>();
        //关闭所有定时播放任务
        if (timerList != null)
            for (Timer timer : timerList)
                timer.cancel();
        timerList = new ArrayList<>();
        //开关机时间
        String[] timerString = null;
        //根据配置文件生成播放列表
        DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        DateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
        DateFormat yyyy_MM_dd_HH_mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (DocumentFile documentFile : docList) {
            JSONObject parseXML = new XMLParse().parseXML(documentFile);
            if(parseXML == null){
                LogUtil.E(documentFile.getUri() + "---config不存在");
                continue;
            }
            try {
                JSONArray playlist = parseXML.getJSONArray("playlist");
                for (int i = 0; i < playlist.length(); i++) {
                    JSONObject play = playlist.getJSONObject(i);
                    String playday = play.getString("playday").trim();
                    JSONArray rules = play.getJSONArray("rules");
                    String date = yyyy_MM_dd.format(yyyyMMdd.parse(playday));
                    for (int j = 0; j < rules.length(); j++) {
                        JSONObject rule = rules.getJSONObject(j);
                        String[] time = rule.getString("date").trim().split("-");
                        playList.add(date + "\t\t\t" + time[0] + "-" + time[1]);
                        String[] res = rule.getString("res").trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");
                        final StringBuilder videoPath = new StringBuilder();
                        //生成播放列表
                        for (int k = 0; k < res.length; k++) {

                            DocumentFile resource = documentFile.findFile("resource");
                            if(resource == null || (!resource.exists())){
                                continue;
                            }

                            DocumentFile videoFile = resource.findFile(res[k]);
                            String index = k + 1 > 9 ? k + 1 + " " : k + 1 + "  ";
                            if (!videoFile.exists()) {
                                playList.add(index + res[k] + "(无)");
                                continue;
                            }
                            playList.add(index + res[k]);
                            previewMap.put(yyyyMMdd.format(new Date()) + res[k], videoFile.getUri().toString());
                            if (videoPath.length() > 0)
                                videoPath.append(",");
                            videoPath.append(videoFile.getUri().toString());
                        }
                        //获取开关机时间
                        if (timerString == null)
                            timerString = new String[]{parseXML.getString("start").trim(), parseXML.getString("end").trim()};
                        //没有可播的放视频时，不添加定时任务
                        if (videoPath.length() == 0)
                            continue;
                        //添加定时任务
                        Date beginTime = yyyyMMddHH_mm.parse(playday + time[0]);
                        Date endTime = yyyyMMddHH_mm.parse(playday + time[1]);
                        //播放结束时间小于当前时间时，不添加定时任务
                        if ((endTime.getTime() - 10000 < System.currentTimeMillis()))
                            continue;
                        //播放定时任务
                        Timer beginTimer = new Timer();
                        beginTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.sendMessage("11", videoPath.toString());
                            }
                        }, beginTime);
                        //停止定时任务
                        Timer endTimer = new Timer();
                        endTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.sendMessage("10", null);
                            }
                        }, new Date(endTime.getTime() - 10000));
                        Log.d("log", documentFile.getUri().toString() + "  " + yyyy_MM_dd_HH_mm.format(beginTime) + " 至 " + yyyy_MM_dd_HH_mm.format(endTime));
                        //添加定时任务到任务列表
                        timerList.add(beginTimer);
                        timerList.add(endTimer);
                    }
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private void setPlayList() {
        Log.d("log", "播放设置");
//        String filePath= Environment.getExternalStorageDirectory().toString();
//        File yunbiao = new File(filePath+"/yunbiao");
//        if (!yunbiao.canRead())
//            return;
        File yunbiao = LowVerSDController.instance().getAppRootDir();
        if(yunbiao == null || (!yunbiao.exists())){
            return;
        }
        LogUtil.D("yunbiao:"+yunbiao.getPath());

        File[] files = yunbiao.listFiles(new VideoDirectoryFilter());
        if (files == null || files.length == 0)
            return;

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
        for (int j = 0; j < files.length; j++)
            files[j] = fileMap.get(fileKey[j]);

        //清空播放列表
        playList = new ArrayList<>();
        previewMap = new HashMap<>();
        //关闭所有定时播放任务
        if (timerList != null)
            for (Timer timer : timerList)
                timer.cancel();
        timerList = new ArrayList<>();
        //开关机时间
        String[] timerString = null;
        //根据配置文件生成播放列表
        DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        DateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
        DateFormat yyyy_MM_dd_HH_mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (File file : files) {
            JSONObject parseXML = new XMLParse().parseXML(file);
            try {
                JSONArray playlist = parseXML.getJSONArray("playlist");
                for (int i = 0; i < playlist.length(); i++) {
                    JSONObject play = playlist.getJSONObject(i);
                    String playday = play.getString("playday").trim();
                    JSONArray rules = play.getJSONArray("rules");
                    String date = yyyy_MM_dd.format(yyyyMMdd.parse(playday));
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
                            String videoUri = Uri.fromFile(video).toString();

                            previewMap.put(yyyyMMdd.format(new Date()) + res[k],videoUri);
                            if (videoPath.length() > 0)
                                videoPath.append(",");
                            videoPath.append(videoUri);
                        }
                        //获取开关机时间
                        if (timerString == null)
                            timerString = new String[]{parseXML.getString("start").trim(), parseXML.getString("end").trim()};
                        //没有可播的放视频时，不添加定时任务
                        if (videoPath.length() == 0)
                            continue;

                        //添加定时任务
                        Date beginTime = yyyyMMddHH_mm.parse(playday + time[0]);
                        Date endTime = yyyyMMddHH_mm.parse(playday + time[1]);
                        //播放结束时间小于当前时间时，不添加定时任务
                        if ((endTime.getTime() - 10000 < System.currentTimeMillis()))
                            continue;
                        //播放定时任务
                        Timer beginTimer = new Timer();
                        beginTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.sendMessage("11", videoPath.toString());
                            }
                        }, beginTime);
                        //停止定时任务
                        Timer endTimer = new Timer();
                        endTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.sendMessage("10", null);
                            }
                        }, new Date(endTime.getTime() - 10000));
                        LogUtil.D(file.getPath() + "  " + yyyy_MM_dd_HH_mm.format(beginTime) + " 至 " + yyyy_MM_dd_HH_mm.format(endTime));
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
            PowerOffTool.setPowerRunTime(timerString[0], timerString[1]);
    }
}
