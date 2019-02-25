package com.yunbiao.cccm.utils;

import com.yunbiao.cccm.common.Const;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/11/27.
 */

public class TimerUtil {
    private Timer mTimer;
    private int recLen = Const.SYSTEM_CONFIG.MENU_STAY_DURATION;
    private OnScreenTask onScreenTask;
    private static OnTimerListener onTimerListener;
    private static TimerUtil instance;

    public synchronized static TimerUtil getInstance(OnTimerListener lis){
        onTimerListener = lis;
        if(instance == null){
            instance = new TimerUtil();
        }
        return instance;
    }

    private class OnScreenTask extends TimerTask {
        @Override
        public void run() {
            ThreadUtil.getInstance().runInUIThread(new Runnable() {
                @Override
                public void run() {
                    recLen--;
                    onTimerListener.onTiming(recLen);
                    if (recLen < 0) {
                        mTimer.cancel();
                        onTimerListener.onTimeFinish();
                    }
                }
            });
        }
    }

    public void pause(){
        if(mTimer != null){
            mTimer.cancel();
            onScreenTask.cancel();
        }
    }

    public void start(int time){
        mTimer = new Timer();
        onScreenTask = new OnScreenTask();

        if(time != 0){
            recLen = time;
        }
        onTimerListener.onTimeStart();
        mTimer.schedule(onScreenTask, 1000, 1000);
    }


    public static void delayExecute(long delay, final OnTimerListener onTimerListener){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(onTimerListener != null){
                    onTimerListener.onTimeFinish();
                }
            }
        };
        new Timer().schedule(timerTask,delay);
    }

    public static class OnTimerListener{
        public void onTimeStart(){

        }//tvShowOnscreenTime.setVisibility(View.VISIBLE);
        public void onTiming(int recLen){

        }//tvShowOnscreenTime.setText("" + recLen);
        public void onTimeFinish(){

        }//tvShowOnscreenTime.setVisibility(View.GONE);
    }
}
