package com.yunbiao.cccm.net.resource.resolve;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.net.control.actions.XBHActions;
import com.yunbiao.cccm.net.resource.TimeResolver;
import com.yunbiao.cccm.net.resource.model.InsertVideoModel;
import com.yunbiao.cccm.net.view.TipToast;
import com.yunbiao.cccm.sdOperator.HighVerSDController;
import com.yunbiao.cccm.sdOperator.LowVerSDController;
import com.yunbiao.cccm.utils.CommonUtils;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/3/1.
 */

public class InsertDataResolver {
    private static InsertDataResolver instance;
    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private final Date todayDate;

    private List<Timer> timerList = new ArrayList<>();

    public static InsertDataResolver instance(){
        if(instance == null){
            synchronized(InsertDataResolver.class){
                if(instance == null){
                    instance = new InsertDataResolver();
                }
            }
        }
        return instance;
    }

    private InsertDataResolver(){
        todayDate = new Date(System.currentTimeMillis());
    }

    public void init(){
        String insertData = CacheManager.FILE.getInsertData();
        if(TextUtils.isEmpty(insertData)){
            LogUtil.D("插播缓存为空");
            return;
        }

        InsertVideoModel insertVideo = new Gson().fromJson(insertData, InsertVideoModel.class);
        if (insertVideo == null) {
            return;
        }

        Date today = null;
        try {
            today = yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(todayDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        InsertVideoModel.InsertData dateJson = insertVideo.getDateJson();
        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();
        if(insertArray == null || insertArray.size()<=0){
            MainController.getInstance().setHasInsert(false);
            return;
        }
        for (InsertVideoModel.Data data : insertArray) {
            if(data == null){
                continue;
            }
            String content = data.getContent();
            String endTime = data.getEndTime();
            String startTime = data.getStartTime();
            boolean isCycle = data.getIsCycle() == 1;
            Integer playType = data.getPlayType();

            List<String> playList = new ArrayList<>();
            Date[] dateArray = TimeResolver.resolve(startTime, endTime);

            if (today.after(dateArray[1])) {
                continue;
            }

            switch (playType) {
                case 1:
                    String[] insertArr = content.split(",");
                    for (String insertUrl : insertArr) {
                        String[] split = insertUrl.split("/");
                        String name = split[split.length - 1];

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                            DocumentFile insertResource = HighVerSDController.instance().findResource(name);
                            if(insertResource != null && insertResource.exists()){
                                playList.add(insertResource.getUri().toString());
                            }
                        } else {
                            File file = LowVerSDController.instance().findResource(name);
                            if(file.exists()){
                                playList.add(Uri.fromFile(file).toString());
                            }
                        }

                    }

                    if(playList.size() <= 0){
                        continue;
                    }

                    handleVideo(isCycle,playList,dateArray[0],dateArray[1]);
                    break;
                case 2:
                    handleLive(isCycle, content, dateArray[0], dateArray[1]);
                    break;
                case 3:
                    handleInput(dateArray[0], dateArray[1]);
                    break;
            }

        }


    }

    //处理视频
    private void handleVideo(final boolean isCycle, final List<String> playList, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, playList);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }
    //处理直播数据
    private void handleLive(final boolean isCycle, final String liveUrl, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add(liveUrl);
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, list);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }
    //处理插播信号源流程
    private void handleInput(Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E("切换到HDMI信号");
                MainController.getInstance().setHasInsert(true);
                checkHDMI(true);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E("结束HDMI信号");
                checkHDMI(false);
            }
        });
    }
    /***
     * 切换HDMI信号 todo 待测试
     * @param isHdmi
     */
    private void checkHDMI(boolean isHdmi) {
        Integer broadType = CommonUtils.getBroadType();
        if (broadType == 4) {//判断是不是小百合
            Intent intent = new Intent();
            intent.setAction(isHdmi ? XBHActions.CHANGE_TO_HDMI : XBHActions.CHANGE_TO_VGA);
            APP.getContext().sendBroadcast(intent);
        } else {
            TipToast.showLongToast(APP.getContext(), "本设备不支持该功能");
        }
    }

    /***定时执行开关任务
     * ==============================================================================
     */
    private void timeExecute(Date startTime, final TimerTask startTask, Date endTime, final TimerTask endTask) {
        Timer startTimer = new Timer();
        Timer endTimer = new Timer();

        startTimer.schedule(startTask, startTime);
        endTimer.schedule(endTask, endTime);

        timerList.add(startTimer);
        timerList.add(endTimer);
    }

    private void clearTimer() {
        if (timerList != null && timerList.size() > 0) {
            for (Timer timer : timerList) {
                timer.cancel();
            }
            timerList.clear();
        }
    }

    public void stopInsert(){
        clearTimer();
        MainController.getInstance().stopInsert();
    }
}
