package com.yunbiao.cccm.control.power;

import android.content.Intent;
import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.control.PowerOffTool;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/8/10 0010.
 */
public class PowerControllerTool {
    private static final String TAG = "PowerControllerTool";

    public static void getPowerContrArray(String powerFlag) {
        Long[] powerOn = PowerOffTool.getInstance().getPowerTime(PowerOffTool.POWER_ON);
        Long[] powerOff = PowerOffTool.getInstance().getPowerTime(PowerOffTool.POWER_OFF);
        Intent intent = new Intent(powerFlag);
        // 如果开关机时间没有设置，就进行网络获取
        if (powerOn != null && powerOff != null) {
            Long offh = powerOff[0] * 24 + powerOff[1];
            Long offm = powerOff[2];
            // 0 23 26
            // onh:0 onm:1
            // 0 23 25
            // offh:23 offm:25
            Long onh = powerOn[0] * 24 + powerOn[1];
            Long onm = powerOn[2];

            Calendar powerOffDate = Calendar.getInstance();
            powerOffDate.add(Calendar.MINUTE, (int) (offh * 60 + offm));
            Calendar powerOnDate = Calendar.getInstance();
            if ((onh * 60 + onm) > (offh * 60 + offm)) {
                long offset = (onh * 60 + onm) - (offh * 60 + offm);
                onm = offset % 60;
                onh = offset / 60;
                powerOnDate.add(Calendar.MINUTE, (int) (offh * 60 + offm) + (int) (onh * 60 + onm));
            } else {
                powerOnDate.add(Calendar.MINUTE, (24 * 60) + (int) (onh * 60 + onm));
            }
            Log.e(TAG, "getPowerContrArray: " + " offh: " + offh + " offm: " + offm + "   onh: " + onh + " onm: " + onm);
            Log.e(TAG, "getPowerContrArray: " + " setPowerOffDate:   " + powerOffDate.getTime().toLocaleString() + "   setPowerOnDate:   " + powerOnDate.getTime().toLocaleString());
            if (powerOn != null && powerOff != null) {
                intent.putExtra("timeon", new int[]{powerOnDate.get(Calendar.YEAR), powerOnDate.get(Calendar.MONTH) + 1, powerOnDate.get(Calendar.DAY_OF_MONTH), powerOnDate.get(Calendar.HOUR_OF_DAY), powerOnDate.get(Calendar.MINUTE)});
                intent.putExtra("timeoff", new int[]{powerOffDate.get(Calendar.YEAR), powerOffDate.get(Calendar.MONTH) + 1, powerOffDate.get(Calendar.DAY_OF_MONTH), powerOffDate.get(Calendar.HOUR_OF_DAY), powerOffDate.get(Calendar.MINUTE)});
                intent.putExtra("enable", true);
            } else {
                intent.putExtra("enable", false);//使能开关机功能，设为false，则为关闭
            }
        } else {
            intent.putExtra("enable", false);//使能开关机功能，设为false，则为关闭
        }
        APP.getMainActivity().sendBroadcast(intent);
    }
}
