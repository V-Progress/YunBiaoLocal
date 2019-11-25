package com.yunbiao.cccm.net2.control.power;

import android.content.Intent;

import com.yunbiao.cccm.APP;

import java.io.File;

public class ZHBroadControl {
    private static ZHBroadControl zhBroadControl = null;

    private ZHBroadControl() {
    }

    public static ZHBroadControl getZhBroadControl() {
        if (zhBroadControl == null) {
            synchronized (ZHBroadControl.class) {
                if (zhBroadControl == null) {
                    zhBroadControl = new ZHBroadControl();
                }
            }
        }
        return zhBroadControl;
    }

    void rebootMachine() {
        Intent cmdIntent = new Intent("com.zhsd.setting.syscmd");
        cmdIntent.putExtra("cmd", "reboot");
        APP.getMainActivity().sendBroadcast(cmdIntent);
    }

    void setPowerOnOff(String msg) {
        Intent powerOnOffTimerIntent = new Intent("com.zhsd.setting.POWER_ON_OFF_TIMER");
        powerOnOffTimerIntent.putExtra("data", msg);
        powerOnOffTimerIntent.putExtra("owner", "0");
        APP.getContext().sendBroadcast(powerOnOffTimerIntent);
    }

    public void screenCut(String path, String sid) {
        Intent cmdIntent = new Intent("com.zhsd.setting.syscmd");
        cmdIntent.putExtra("cmd", "screencap");
        cmdIntent.putExtra("fullscreen", true);
        //scale<=1.0,不缩放;  scale>1.0,图片长宽按scale缩放...
        //缩放的越大,截屏之后，保存图片的速度也更快.
        cmdIntent.putExtra("scale", 1.5f);//1.5是缩放级别
        //位置自己设置
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        cmdIntent.putExtra("filepath", path + sid + ".png");
        APP.getMainActivity().sendBroadcast(cmdIntent);
    }


}
