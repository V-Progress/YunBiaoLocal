package com.yunbiao.cccm.net2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class USBBroadcastReceiver extends BroadcastReceiver{
    private String tempAction;

    public USBBroadcastReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        String path = intent.getDataString();
        if (TextUtils.equals(action, tempAction)) {
            return;
        }
        tempAction = action;

        //判断插拔动作
        if (TextUtils.equals(Intent.ACTION_MEDIA_MOUNTED, action)) {//U盘插入


        } else if (TextUtils.equals(Intent.ACTION_MEDIA_UNMOUNTED, action) || TextUtils.equals(Intent.ACTION_MEDIA_REMOVED, action)) {//3288移除只发这个

        }
    }
}
