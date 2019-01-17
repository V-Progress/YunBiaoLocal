package com.yunbiao.cccm.control.power;

import com.xboot.stdcall.posix;
import com.yunbiao.cccm.utils.LogUtil;

import java.util.Calendar;

public class OnOffTool {
    private static String TAG = "OnOffTool";
    public static void setEnabled(byte on_h, byte on_m, byte off_h, byte off_m) {
        LogUtil.D(TAG,"开关机时间:offH:" + off_h + " off_m:" + off_m + " on_h:" + on_h + " on_m:" + on_m);
        setPowerOnOff(on_h, on_m, off_h, off_m, (byte) 3);
    }

    public static void setDisabled() {
        setPowerOnOff((byte)0,  (byte)3,  (byte)0,  (byte)3, (byte) 0);
    }

    /**
     * 所有时间都是相对时间，（关机时间=实际关机时间-当前时间）
     * （开机时间=实际开机时间-当前时间-关机时间）
     *
     * @param off_h  关机小时
     * @param off_m  关机分钟
     * @param on_h   开机小时
     * @param on_m   开机分钟
     * @param enable 自动开关机状态  0：关     3：开
     * @return
     */
    public static void setPowerOnOff(byte off_h, byte off_m, byte on_h, byte on_m, byte enable) {
        try{
            int fd, ret;
            //  byte buf[] = { 0, 3, 0, 3 };
            fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
            ret = posix.poweronoff(off_h, off_m, on_h, on_m, enable, fd);
            posix.close(fd);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getCurTime() {
        String time = "";
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        time = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
        return time;
    }
}
