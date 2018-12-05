package com.yunbiao.yunbiaolocal.utils;

import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时任务类
 * Created by Administrator on 2018/11/29.
 */

public class TimerExecutor {
    private static TimerExecutor instance;
    private Timer timer;
    private List<String> timeList;

    private List<TimerTask> runningTasks = new ArrayList<>();

    public synchronized static TimerExecutor getInstance() {
        if (instance == null) {
            instance = new TimerExecutor();
        }
        return instance;
    }

    private TimerExecutor() {
        timer = new Timer(true);
        timeList = new ArrayList<>();
    }

    public interface OnTimeOutListener {
        void execute();
    }

    public void addInTimerQueue(Date execTime, final OnTimeOutListener onTimeOutListener) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.getInstance().runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onTimeOutListener != null) {
                            onTimeOutListener.execute();
                        }
                    }
                });
            }
        };
        runningTasks.add(timerTask);
        timer.schedule(timerTask, execTime);
    }

    public void closeQueue() {
        if (runningTasks != null && runningTasks.size() > 0) {
            for (TimerTask runningTask : runningTasks) {
                runningTask.cancel();
            }
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
}
