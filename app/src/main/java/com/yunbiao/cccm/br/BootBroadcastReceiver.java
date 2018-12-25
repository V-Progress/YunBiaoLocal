package com.yunbiao.cccm.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yunbiao.cccm.act.MainActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartSeceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
