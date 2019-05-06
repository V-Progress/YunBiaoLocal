package com.yunbiao.cccm.activity;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.utils.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MyProtectService extends Service {
    private static final String TAG = "MyProtectService";

    //看门狗service
    private String packageName = "com.yunbiao.cccm";
    private String packageClassName = "com.yunbiao.cccm.activity.MainActivity";

    private final static int DELAY_TIME = 120 * 1000;//120s轮询一次
    private final static Timer timer = new Timer();
    private Date mCurrDate;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrDate = DateUtil.getTodayDate();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            APP.restart();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "守护服务已开启...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Date todayDate = DateUtil.getTodayDate();
                if(!mCurrDate.equals(todayDate)){
                    mCurrDate = todayDate;
                    Random random = new Random();
                    int delay = random.nextInt(60);
                    delay = delay * 1000;
                    handler.sendEmptyMessageDelayed(0,delay);
                }

                if (!isMyAppRunning(MyProtectService.this, packageName)) {
                    startTargetActivity(packageName, packageClassName);
                }
            }
        },DELAY_TIME,DELAY_TIME);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 根据报名判断app是否运行
     */
    private boolean isMyAppRunning(Context context, String packageName) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过包名和类名来开启活动
     */
    private void startTargetActivity(String packageName, String className) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}
