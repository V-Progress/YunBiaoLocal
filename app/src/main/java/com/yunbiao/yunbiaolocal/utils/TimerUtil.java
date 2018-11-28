package com.yunbiao.yunbiaolocal.utils;

import android.app.Activity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/11/27.
 */

public class TimerUtil {
    private static TimerUtil instance;
    private Timer mTimer;
    private int recLen = 60;
    private OnScreenTask onScreenTask;
    private static Activity mActivity;
    private OnTimerListener onTimerListener;

    public synchronized static TimerUtil getInstance(Activity activity){
        mActivity = activity;
        if(instance == null){
            instance = new TimerUtil();
        }
        return instance;
    }

    private class OnScreenTask extends TimerTask {
        @Override
        public void run() {
            mActivity.runOnUiThread(new Runnable() {//UI thread
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
        Log.e("123","暂停！");
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

    public TimerUtil listen(OnTimerListener onTimerListener){
        this.onTimerListener = onTimerListener;
        return instance;
    }

    public interface OnTimerListener{
        void onTimeStart();//tvShowOnscreenTime.setVisibility(View.VISIBLE);
        void onTiming(int recLen);//tvShowOnscreenTime.setText("" + recLen);
        void onTimeFinish();//tvShowOnscreenTime.setVisibility(View.GONE);
    }
}
