package com.yunbiao.cccm.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yunbiao.cccm.act.MainActivity;
import com.yunbiao.cccm.devicectrl.Test;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartSeceiver";
    private String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        String action = intent.getAction();
        if (action.equals(ACTION)) {
            LogUtil.E("开机成功，获取开关机数据");
            //自动开关机
            ThreadUtil.getInstance().runInCommonThread(machineRestartRun);
        }
    }

    public Runnable machineRestartRun = new Runnable() {
        public void run() {
            Test.getPowerOffTool().machineStart();
        }
    };
}
