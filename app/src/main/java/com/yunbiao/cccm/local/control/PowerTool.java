package com.yunbiao.cccm.local.control;

import android.text.TextUtils;

import com.yunbiao.cccm.net.control.power.OnOffTool;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PowerTool {

    /**
     * 设置机器的开关机时间
     */
    public static void setPowerRunTime(String powerOffTime , String powerOnTime) {
        if (TextUtils.isEmpty(powerOnTime) || TextUtils.isEmpty(powerOffTime) ) {
            OnOffTool.setDisabled();
            return ;
        }
        Long[] powerOn = getPowerTime("1,2,3,4,5,6,7;" + powerOnTime);
        Long[] powerOff = getPowerTime("1,2,3,4,5,6,7;" + powerOffTime);

        if (powerOn != null && powerOff != null) {

            Long offh = powerOff[0] * 24 + powerOff[1];
            Long offm = powerOff[2];

            Long onh = powerOn[0] * 24 + powerOn[1];
            Long onm = powerOn[2];

            if ((onh * 60 + onm) > (offh * 60 + offm)) {
                long offset = (onh * 60 + onm) - (offh * 60 + offm);
                onm = offset % 60;
                onh = offset / 60;
            }
            //停止开关机
            OnOffTool.setPowerOnOff((byte) 0, (byte) 4, (byte) 0, (byte) 4, (byte) 0);
            //启动开关机
            OnOffTool.setEnabled(onh.byteValue(), onm.byteValue(), offh.byteValue(), offm.byteValue());
        } else {
            OnOffTool.setDisabled();
        }
    }

    /**
     * 获取下一个开关机时间
     *
     * @param
     * @return
     */
    public static Long[] getPowerTime(String runTimeStr) {
        Long[] runTimeLong = null;

        // 获取当天星期几
        Date currentDate = new Date();
        Integer weekDay = getWeekDay(currentDate);

        // 获取时间
        String powerOn = runTimeStr;
        if (powerOn != null && !powerOn.equals("")) {
            // 判断是否存在运行策略
            String[] powerOnArray = powerOn.split(";");
            String runDate = powerOnArray[0];
            String runTime = powerOnArray[1];

            String[] timeDateArray = runTime.split(":");
            Date onDate = new Date();
            onDate.setHours(Integer.parseInt(timeDateArray[0]));
            onDate.setMinutes(Integer.parseInt(timeDateArray[1]));
            if (runDate.indexOf(weekDay.toString()) != -1) {// 如果当天是运行策略中包含的周期
                if (onDate.getTime() > currentDate.getTime()) {
                    // 如果开机时间大于当前时间，就需要设置
                    runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
                } else {
                    // 推迟到运行周期中的下一天的这个时间
                    Integer betweenDay = getBetweenDay(runDate, weekDay);
                    onDate.setDate(onDate.getDate() + betweenDay);
                    runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
                }
            } else {
                // 如果运行策略中没有这一天
                Integer nextDay = getNextDay(runDate, weekDay);
                onDate.setDate(onDate.getDate() + nextDay);
                runTimeLong = formatDuring(onDate.getTime()
                        - currentDate.getTime());
            }
        }
        return runTimeLong;
    }

    public static Integer getNextDay(String runDate, Integer currentWeekDay) {
        Integer between = 0;
        String[] runDateArray = runDate.split(",");
        for (int i = 0; i < runDateArray.length; i++) {
            Integer runDateA = Integer.parseInt(runDateArray[i]);
            if (runDateA > currentWeekDay) {
                between = runDateA - currentWeekDay;
                break;
            }
        }
        return between;
    }

    public static Integer getBetweenDay(String runDate, Integer currentWeekDay) {
        Integer between = 0;
        String[] runDateArray = runDate.split(",");
        for (int i = 0; i < runDateArray.length; i++) {
            Integer runDateA = Integer.parseInt(runDateArray[i]);
            if (runDateA == currentWeekDay) {
                if (i == runDateArray.length - 1) {
                    between = Integer.parseInt(runDateArray[0])
                            + (7 - currentWeekDay);
                } else {
                    runDateA = Integer.parseInt(runDateArray[i + 1]);
                    between = runDateA - currentWeekDay;
                }
                break;
            }
        }
        return between;
    }

    /**
     * 获取时间间隔
     *
     * @param mss
     * @return
     */
    public static Long[] formatDuring(Long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return new Long[]{days, hours, minutes, seconds};
    }

    public static Integer getWeekDay(Date date) {
        SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
        String weekDay = dateFm.format(date);
        Integer week = 0;
        if (weekDay.equals("星期一")) {
            week = 1;
        } else if (weekDay.equals("星期二")) {
            week = 2;
        } else if (weekDay.equals("星期三")) {
            week = 3;
        } else if (weekDay.equals("星期四")) {
            week = 4;
        } else if (weekDay.equals("星期五")) {
            week = 5;
        } else if (weekDay.equals("星期六")) {
            week = 6;
        } else if (weekDay.equals("星期日")) {
            week = 7;
        }
        return week;
    }
}
