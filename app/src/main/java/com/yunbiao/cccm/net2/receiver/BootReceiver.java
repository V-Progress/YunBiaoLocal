package com.yunbiao.cccm.net2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.activity.MainActivity;
import com.yunbiao.cccm.net2.control.PowerOffTool;
import com.yunbiao.cccm.net2.utils.CommonUtils;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartReceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);
        if (action.equals(ACTION)) {
            //自动开关机
            new Thread(machineRestartRun).start();
            //开机重置开关机设置标志，A20定时关机会重走程序，定时开关机失效，然后加上这个标志
            Log.i(TAG, "重启当前时间：" + CommonUtils.getStringDate());
            try {
                //开机恢复之前保存的声音的大小，中恒板子关机实际上是屏幕休眠，但是开机是休眠时间到先关机后开机，rom是这样的，
                AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            PowerOffTool.getInstance().initPowerData();
        }
    };
}
