package com.yunbiao.yunbiaolocal.io;

import android.os.Environment;

import com.yunbiao.yunbiaolocal.utils.ThreadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/12/3.
 */

public class VideoDataResolver {
    private static VideoDataResolver videoDataResolver;
    private final File yunbiaoDir;
    public List<Timer> timerList;

    public VideoDataResolver() {
        String filePath = Environment.getExternalStorageDirectory().toString();
        yunbiaoDir = new File(filePath + "/yunbiao");
    }

    public synchronized VideoDataResolver getInstance(){
        if(videoDataResolver == null){
            videoDataResolver = new VideoDataResolver();
        }
        return videoDataResolver;
    }

    public void resolvePlayList(){
        ThreadUtil.getInstance()
                .runInSingleThread(new Runnable() {
            @Override
            public void run() {
                //检测可读
                if(!yunbiaoDir.canRead()){
                    return;
                }
                //检测文件
                File[] files = yunbiaoDir.listFiles(new VideoDirectoryFilter());
                if(files == null || files.length == 0){
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
                for (int i = 0; i < files.length; i++) {
                    files[i] = fileMap.get(fileKey);
                }
                //清空播放列表
                List<String> playList = new ArrayList<String>();
                Map<String,String> previewMap = new HashMap<String, String>();
                //关闭所有定时任务
                if(timerList != null){
                    for (Timer timer : timerList) {
                        timer.cancel();
                    }
                }
                timerList = new ArrayList<Timer>();




            }
        });
    }
}
