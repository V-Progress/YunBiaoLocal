package com.yunbiao.cccm.net.control.power;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net.control.PowerOffTool;
import com.yunbiao.cccm.net.control.actions.JYDActions;

public class  JYDBroadControl {
    private static final String TAG = "JYDBroadControl";

    /**
     * setJYDOnOff("2", "127", "15", "30");
     * weeks 星期一 1、星期二 2、星期三 4、星期四 8、星期五 16、星期六 32、星期日 64，累加
     * 如果是星期一+星期二就为5，如果每天就是127
     * hours 0-23
     * minutes 0-59
     */

    public JYDBroadControl() {
        String on = PowerOffTool.getInstance().getPowerParam(PowerOffTool.POWER_ON); //1,2,3,4,5,6,7;08:00
        String off = PowerOffTool.getInstance().getPowerParam(PowerOffTool.POWER_OFF);

        if (TextUtils.isEmpty(on) || TextUtils.isEmpty(off)) {
            return;
        }

        String onWeeks = getWeeks(on);
        String onHours = getHour(on);
        String onMinutes = getMinutes(on);

        String offWeeks = getWeeks(off);
        String offHours = getHour(off);
        String offMinutes = getMinutes(off);

        setJYDOnOff("1", onWeeks, onHours, onMinutes);//开机
        setJYDOnOff("2", offWeeks, offHours, offMinutes);//关机
    }

    //获取week
    private String getWeeks(String power) {
        int sum = 0;
        String substring = power.substring(0, power.indexOf(";") - 1);
        String[] split = substring.split(",");
        for (String aSplit : split) {
            int num = Integer.valueOf(aSplit);
            int count = (int) Math.pow(2, num - 1);
            sum += count;
        }
        return String.valueOf(sum);
    }

    //获取hour
    private String getHour(String power) {
        String substring = power.substring(power.indexOf(";") + 1, power.indexOf(":"));
        return substring;
    }

    //获取minutes
    private String getMinutes(String power) {
        String substring = power.substring(power.indexOf(":") + 1, power.length());
        return substring;
    }

    /**
     * flag 1-on   2-off
     */
    private static void setJYDOnOff(String flag, String weeks, String hour, String minutes) {
        Intent intent = new Intent(JYDActions.UPDATEALARM);
        intent.putExtra("flag", flag);
        intent.putExtra("weeks", weeks);
        intent.putExtra("hour", hour);
        intent.putExtra("minutes", minutes);
        APP.getMainActivity().sendBroadcast(intent);
        Log.e(TAG, "setJYDOnOff: " + flag + "-" + weeks + "-" + hour + "：" + minutes);
    }
}
