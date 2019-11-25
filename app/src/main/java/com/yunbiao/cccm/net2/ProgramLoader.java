package com.yunbiao.cccm.net2;

import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.net2.activity.MainController;
import com.yunbiao.cccm.net2.db.Daily;
import com.yunbiao.cccm.net2.db.DaoManager;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.db.TimeSlot;
import com.yunbiao.cccm.net2.utils.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2019/11/19.
 */

public class ProgramLoader {
    private static ProgramLoader programLoader = new ProgramLoader();
    private List<Timer> timerList = new ArrayList<>();
    private final ScheduledExecutorService scheduledExecutorService;

    private List<String> currentPlayList = new ArrayList<>();
    private String today;

    private ProgramLoader() {
        today = DateUtil.getToday_str();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static ProgramLoader getInstance() {
        return programLoader;
    }

    public interface LoadProgramListener{
        void onStart(String date);
        void noProgram();
        void noTimeSlot();
        void onOutTime(String time);
        void onTaskAdded(String timeSlot);
        void onProgramStart(String start, int size);
        void onProgramStop(String end);
        void onFinished();
    }

    public static class AutoLogProgramListener implements LoadProgramListener{

        @Override
        public void onStart(String date) {
            ConsoleDialog.addProgramLog("开始加载节目：" + date);
        }

        @Override
        public void noProgram() {
            ConsoleDialog.addProgramLog("无节目安排");
        }

        @Override
        public void noTimeSlot() {
            ConsoleDialog.addProgramLog("未配置时间段");
        }

        @Override
        public void onOutTime(String time) {
            ConsoleDialog.addProgramLog("已排除过期时间段：" + time);
        }

        @Override
        public void onTaskAdded(String timeSlot) {
            ConsoleDialog.addProgramLog("播放任务已添加：" + timeSlot);
        }

        @Override
        public void onProgramStart(String start, int size) {
            ConsoleDialog.addProgramLog("开始播放：" + start + "，节目数量：" + size);
        }

        @Override
        public void onProgramStop(String end) {
            ConsoleDialog.addProgramLog("开始播放：" + end);
        }

        @Override
        public void onFinished() {
            ConsoleDialog.addProgramLog("当前节目加载完毕");
        }
    }

    public void loadProgram(final LoadProgramListener listener) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onStart(today);
                }

                d("节目加载器开始执行---------------------------");
                if (timerList.size() > 0) {
                    for (Timer timer : timerList) {
                        timer.cancel();
                    }
                }

                Daily currentDaily = DaoManager.get().queryByDate(today);
                if (currentDaily == null) {
                    if(listener != null){
                        listener.noProgram();
                    }
                    d("无节目111");
                    return;
                }

                List<TimeSlot> timeSlots = currentDaily.getTimeSlots();
                if (timeSlots == null || timeSlots.size() <= 0) {
                    if(listener != null){
                        listener.noTimeSlot();
                    }
                    d("无节目222");
                    return;
                }

                for (TimeSlot timeSlot : currentDaily.getTimeSlots()) {
                    try {
                        timeSlot.setStartDate(DateUtil.yyyy_MM_dd_HH_mm_Parse(currentDaily.getDate() + " " + timeSlot.getStart()));
                        timeSlot.setEndDate(DateUtil.yyyy_MM_dd_HH_mm_Parse(currentDaily.getDate() + " " + timeSlot.getEnd()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                sendProgram(currentDaily,listener);
            }
        }, 3, TimeUnit.SECONDS);
    }

    private void sendProgram(Daily currentDaily, final LoadProgramListener listener) {
        d("开始加载：" + currentDaily.getDate());

        currentTime = null;
        Date currDate = new Date();
        List<TimeSlot> timeList = currentDaily.getTimeSlots();

        for (final TimeSlot time : timeList) {
            if (currDate.after(time.getEndDate())) {
                if(listener != null){
                    listener.onOutTime(time.getStart() + " --- " + time.getEnd());
                }
                d("已过时：" + time.getStart() + " --- " + time.getEnd());
                continue;
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    currentTime = time;
                    d("开始播放啦 --- " + time.getStart());
                    time.setPlaying(true);

                    currentPlayList = start(time);
                    MainController.getInstance().startPlay(currentPlayList);

                    if(listener != null){
                        listener.onProgramStart(time.getStart(),currentPlayList.size());
                    }

                    Log.e(TAG, "start: 发送节目表");
                    for (String program : currentPlayList) {
                        Log.e(TAG, "start: " + program);
                    }
                }
            }, time.getStartDate());

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(listener != null){
                        listener.onProgramStop(time.getEnd());
                    }
                    d("停止播放啦 --- " + time.getEnd());
                    time.setPlaying(false);
                    currentTime = null;
                    stop();
                }
            }, time.getEndDate());

            timerList.add(timer);
            if(listener != null){
                listener.onTaskAdded(time.getStart() + " --- " + time.getEnd());
            }
            d("任务已添加：" + time.getStart() + " --- " + time.getEnd());
        }

        if(listener != null){
            listener.onFinished();
        }
    }

    private TimeSlot currentTime;
//    public void add() {
//        if(currentTime == null){
//            return;
//        }
//
//        Daily daily = DaoManager.get().queryByDate(today);
//        List<TimeSlot> timeSlots = daily.getTimeSlots();
//
//        for (TimeSlot timeSlot : timeSlots) {
//            if(TextUtils.equals(currentTime.getStart(),timeSlot.getStart()) && TextUtils.equals(currentTime.getEnd(),timeSlot.getEnd())){
//
//            }
//        }
//    }

    private void stop() {
        MainController.getInstance().stopPlay();
    }

    private List<String> start(TimeSlot timeSlot) {
        List<String> list = new ArrayList<>();
        for (ItemBlock itemBlock : timeSlot.getItemBlocks()) {
            DocumentFile file = PathManager.instance().getResDocFileDir().findFile(itemBlock.getName());
            if(file == null || !file.exists()){
                Log.e(TAG, "start: 文件不存在：" + itemBlock.getName());
            } else {
                Log.e(TAG, "start: 文件存在：" + itemBlock.getName());
                list.add(file.getUri().toString());
            }
        }

        return list;
    }

    private static final String TAG = "ProgramLoader";

    private void d(String log) {
        Log.d(TAG, log);
    }
}
