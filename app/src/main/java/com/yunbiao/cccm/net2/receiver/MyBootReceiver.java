package com.yunbiao.cccm.net2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.SplashActivity;

/**
 * Created by Administrator on 2019/11/27.
 */

public class MyBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            try {
                //开机恢复之前保存的声音的大小，中恒板子关机实际上是屏幕休眠，但是开机是休眠时间到先关机后开机，rom是这样的，
                AudioManager audioManager = (AudioManager) APP.getContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent i = new Intent(context, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
