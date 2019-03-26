package com.yunbiao.cccm.local;

import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.local.control.PowerTool;
import com.yunbiao.cccm.local.model.InsertDataModel;
import com.yunbiao.cccm.local.model.VideoDataModel;
import com.yunbiao.cccm.net.resource.UpdateEvent;
import com.yunbiao.cccm.sd.HighVerSDController;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.xbill.DNS.Update;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/2/14.
 */

public class LocalManager {

    private static ArrayList<Timer> videoTimerList = new ArrayList<>();
    private static ArrayList<Timer> insertTimerList = new ArrayList<>();

    private static LocalManager instance;
    private static List<String> playList = new ArrayList<>();
    private static Map<String,String> previewMap = new HashMap<>();

    public static synchronized LocalManager getInstance(){
        if(instance == null){
            instance = new LocalManager();
        }
        return instance;
    }

    public List<String> getPlayList(){
        return playList;
    }

    public Map<String, String> getPreview(){
        return previewMap;
    }

    // 解析本地资源
    public void initData() {
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().clearPlayData();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
//                    File yunbiao = LowVerSDController.instance().getAppRootDir();
                    File yunbiao = LowVerSDController.instance().getAppRootDir();
                    //目录是否存在或可读
                    if (!yunbiao.exists() || !yunbiao.canRead()) {
                        ToastUtil.showShort(APP.getMainActivity(),"yunbiao目录不存在或拒绝读取");
                        return;
                    }
                    //筛选yunbiao目录下所有20xx-20xx类的目录
                    File[] files = yunbiao.listFiles(new VideoDirectoryFilter());
                    if (files == null || files.length == 0) {
                        ToastUtil.showShort(APP.getMainActivity(),"yunbiao目录下没有可播放资源");
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
                    playList.clear();
                    previewMap.clear();
                    EventBus.getDefault().postSticky(new UpdateEvent(UpdateEvent.UPDATE_PLAYLIST));

                    //解析播放列表
                    for (File file : files) {

                        File configFile = null;
                        File insertFile = null;
                        if(!file.isFile()){
                            configFile = new File(file,"config.xml");
                            insertFile = new File(file,"insert.xml");
                        }

                        if(insertFile.exists()){
                            MainController.getInstance().setHasInsert(true);
                            //关闭所有的插播任务
                            cancelTimerList(insertTimerList);

                            InsertDataModel insertDataModel = new XMLParse().parseInsertModel(insertFile);

                            int layerType = 1;
                            if(!TextUtils.isEmpty(insertDataModel.getConfig().getLayerType())){
                                layerType = Integer.valueOf(insertDataModel.getConfig().getLayerType());
                            }
                            MainController.getInstance().updateLayerType(layerType);

                            parseInsert(file,insertDataModel);
                        }

                        if(configFile.exists()){
                            MainController.getInstance().setHasConfig(true);
                            //关闭所有定时播放任务
                            cancelTimerList(videoTimerList);

                            //解析播放数据
                            VideoDataModel videoDataModel = new XMLParse().parseVideoModel(configFile);

                            //解析开关机时间
                            parseOnOff(videoDataModel);

                            //解析播放数据
                            parseVideo(file,videoDataModel);
                        }
                    }
                } else {
                    DocumentFile yunbiao = HighVerSDController.instance().getAppRootDir();
                    if(yunbiao == null || (!yunbiao.exists())){
                        ToastUtil.showShort(APP.getMainActivity(),"yunbiao目录不存在或拒绝读取");
                        return;
                    }

                    List<DocumentFile> docList = new ArrayList<>();
                    DocumentFile[] documentFiles = yunbiao.listFiles();
                    for (DocumentFile documentFile : documentFiles) {
                        if(documentFile.isDirectory() && documentFile.getName().matches("201\\d{5}-201\\d{5}")){
                            docList.add(documentFile);
                        }
                    }

                    //清空播放列表
                    playList.clear();
                    previewMap.clear();
                    //清空播放列表
                    EventBus.getDefault().postSticky(new UpdateEvent(UpdateEvent.UPDATE_PLAYLIST));

                    //解析播放列表
                    for (DocumentFile file : docList) {

                        LogUtil.E("321","file："+file.getUri());
                        DocumentFile configFile = null;
                        DocumentFile insertFile = null;
                        //如果是文件
                        if(!file.isFile()){
                            configFile = file.findFile("config.xml");
                            insertFile = file.findFile("insert.xml");
                        }

                        if(insertFile != null && insertFile.exists()){
                            LogUtil.E("321","file："+insertFile.getUri());
                            MainController.getInstance().setHasInsert(true);
                            //关闭所有的插播任务
                            cancelTimerList(insertTimerList);

                            LogUtil.E("321","file：解析insert");
                            InsertDataModel insertDataModel = new XMLParse().parseInsertModel(insertFile);
                            int layerType = 1;
                            if(!TextUtils.isEmpty(insertDataModel.getConfig().getLayerType())){
                                layerType = Integer.valueOf(insertDataModel.getConfig().getLayerType());
                            }
                            MainController.getInstance().updateLayerType(layerType);

                            parseInsert(file,insertDataModel);
                        }

                        if(configFile != null && configFile.exists()){
                            LogUtil.E("321","file："+configFile.getUri());
                            MainController.getInstance().setHasConfig(true);
                            //关闭所有定时播放任务
                            cancelTimerList(videoTimerList);
                            LogUtil.E("321","file：解析config");
                            //解析播放数据
                            VideoDataModel videoDataModel = new XMLParse().parseVideoModel(configFile);
                            LogUtil.E("321","file："+videoDataModel.toString());
                            //解析开关机时间
                            parseOnOff(videoDataModel);

                            //解析播放数据
                            parseVideo(file,videoDataModel);
                        }
                    }
                }
            }
        });
    }

    //解析播放视频数据
    private void parseVideo(File dirFile,VideoDataModel videoDataModel){
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

                    File video = new File(dirFile, "resource/" + videoStr);
                    String index = k + 1 > 9 ? k + 1 + " " : k + 1 + "  ";
                    if (!video.exists()) {
                        playList.add(index + videoStr + "(无)");
                        continue;
                    }
                    playList.add(index + videoStr);

                    previewMap.put(videoName, Uri.fromFile(video).toString());

                    videoList.add(Uri.fromFile(video).toString());
                }

                //没有可播的放视频时，不添加定时任务
                if (videoList.size() == 0) {
                    continue;
                }

                //添加定时任务
                LogUtil.E("123", "开始时间" + playDay + times[0]);
                LogUtil.E("123", "结束时间" + playDay + times[1]);
                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);

                //播放结束时间小于当前时间时，不添加定时任务
                if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                    continue;
                }

                //播放定时任务
                Timer beginTimer = new Timer();
                beginTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().startPlay(videoList);
                    }
                }, beginTime);

                //停止定时任务
                Timer endTimer = new Timer();
                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().stopPlay();
                    }
                }, new Date(endTime.getTime() - 10000));

                //添加定时任务到任务列表
                videoTimerList.add(beginTimer);
                videoTimerList.add(endTimer);
            }
        }
    }

    //解析播放视频数据
    private void parseVideo(DocumentFile dirFile,VideoDataModel videoDataModel){
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        DocumentFile resource = dirFile.findFile("resource");

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

                    DocumentFile video = resource.findFile(videoStr);

                    String index = k + 1 > 9 ? k + 1 + " " : k + 1 + "  ";
                    if (video == null || !video.exists()) {
                        playList.add(index + videoStr + "(无)");
                        continue;
                    }
                    playList.add(index + videoStr);

                    previewMap.put(videoName, video.getUri().toString());

                    videoList.add(video.getUri().toString());
                }

                //没有可播的放视频时，不添加定时任务
                if (videoList.size() == 0) {
                    continue;
                }

                EventBus.getDefault().postSticky(new UpdateEvent(UpdateEvent.UPDATE_PLAYLIST));

                //添加定时任务
//                LogUtil.E("123", "开始时间" + playDay + times[0]);
//                LogUtil.E("123", "结束时间" + playDay + times[1]);
                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);

                //播放结束时间小于当前时间时，不添加定时任务
                if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                    continue;
                }

                //播放定时任务
                Timer beginTimer = new Timer();
                beginTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().startPlay(videoList);
                    }
                }, beginTime);

                //停止定时任务
                Timer endTimer = new Timer();
                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().stopPlay();
                    }
                }, new Date(endTime.getTime() - 10000));

                //添加定时任务到任务列表
                videoTimerList.add(beginTimer);
                videoTimerList.add(endTimer);
            }
        }
    }
    //解析开关机数据
    private void parseOnOff(VideoDataModel videoDataModel){
        VideoDataModel.Config config = videoDataModel.getConfig();
        String end = config.getEnd();
        String start = config.getStart();

        //定时开关机
        PowerTool.setPowerRunTime(end,start);
    }

    //解析插播数据
    private void parseInsert(File dirFile,InsertDataModel insertDataModel){
        List<InsertDataModel.Play> playlist = insertDataModel.getPlaylist();

        //遍历插播列表
        for (InsertDataModel.Play play : playlist) {
            String playDay = play.getPlayday().trim();//playDay
            List<InsertDataModel.Play.Rule> rules = play.getRules();//取出播放规则

            //遍历播放规则
            for (final InsertDataModel.Play.Rule rule : rules) {
                //取播放时间
                String[] times = rule.getDate().trim().split("-");
                //播放列表
                String[] res = rule.getRes().trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");
                //视频列表
                final List<String> videoList = new ArrayList<>();
                for (int k = 0; k < res.length; k++) {
                    String videoName = res[k];
                    File video = new File(dirFile, "resource/" + videoName);
                    //如果视频不存在则跳过
                    if (!video.exists()) {
                        continue;
                    }
                    //如果视频存在把视频地址添加到视频列表中
                    videoList.add(video.getPath());
                }

                //没有可播的放视频时，不添加定时任务
                if (videoList.size() == 0) {
                    continue;
                }

                //添加定时任务
//                LogUtil.E("123", "开始时间" + playDay + times[0]);
//                LogUtil.E("123", "结束时间" + playDay + times[1]);
                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);

                //播放结束时间小于当前时间时，不添加定时任务
                if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                    continue;
                }

                //播放定时任务
                Timer beginTimer = new Timer();
                beginTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
//                        boolean isCycle = Integer.valueOf(rule.getIsCycle()) == 1;
                        MainController.getInstance().startInsert(true,videoList,false);
                    }
                }, beginTime);

                //停止定时任务
                Timer endTimer = new Timer();
                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().stopInsert();
                    }
                }, new Date(endTime.getTime() - 10000));

                //添加定时任务到任务列表
                insertTimerList.add(beginTimer);
                insertTimerList.add(endTimer);
            }
        }
    }

    //解析插播数据
    private void parseInsert(DocumentFile dirFile,InsertDataModel insertDataModel){
        List<InsertDataModel.Play> playlist = insertDataModel.getPlaylist();
        DocumentFile resource = dirFile.findFile("resource");

        //遍历插播列表
        for (InsertDataModel.Play play : playlist) {
            String playDay = play.getPlayday().trim();//playDay
            List<InsertDataModel.Play.Rule> rules = play.getRules();//取出播放规则

            //遍历播放规则
            for (final InsertDataModel.Play.Rule rule : rules) {
                //取播放时间
                String[] times = rule.getDate().trim().split("-");
                //播放列表
                String[] res = rule.getRes().trim().replace("，", ",").replaceAll("\\s*,\\s*", ",").split(",");
                //视频列表
                final List<String> videoList = new ArrayList<>();
                for (int k = 0; k < res.length; k++) {
                    String videoName = res[k];
                    DocumentFile video = resource.findFile(videoName);

                    //如果视频不存在则跳过
                    if (video == null || !video.exists()) {
                        continue;
                    }
                    //如果视频存在把视频地址添加到视频列表中
                    videoList.add(video.getUri().toString());
                }

                //没有可播的放视频时，不添加定时任务
                if (videoList.size() == 0) {
                    continue;
                }

                //添加定时任务
//                LogUtil.E("321", "开始时间" + playDay + times[0]);
//                LogUtil.E("321", "结束时间" + playDay + times[1]);
                Date beginTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[0]);
                Date endTime = DateUtil.yyyyMMddHH_mm_Parse(playDay + times[1]);

                //播放结束时间小于当前时间时，不添加定时任务
                if ((endTime.getTime() - 10000 < System.currentTimeMillis())) {
                    continue;
                }

                //播放定时任务
                Timer beginTimer = new Timer();
                beginTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().startInsert(true,videoList,false);
                    }
                }, beginTime);

                //停止定时任务
                Timer endTimer = new Timer();
                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainController.getInstance().stopInsert();
                    }
                }, new Date(endTime.getTime() - 10000));

                //添加定时任务到任务列表
                insertTimerList.add(beginTimer);
                insertTimerList.add(endTimer);
            }
        }
    }

    //清空定时任务列表
    private void cancelTimerList(List<Timer> timerList){
        if(timerList == null)
            return;
        for (Timer timer : timerList) {
            timer.cancel();
        }
        timerList.clear();
    }
}
