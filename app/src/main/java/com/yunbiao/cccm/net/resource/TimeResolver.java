package com.yunbiao.cccm.net.resource;

import com.yunbiao.cccm.log.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2019/3/1.
 */

public class TimeResolver {
    private static String TAG = "TimeResolver";

    private static Date todayDate;
    private static SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    static {
        todayDate = new Date(System.currentTimeMillis());
    }

    //解析播放时间，没有date的情况下默认为当天
    public static Date[] resolve(String startStr, String endStr) {
        try {
            String endTime = correctTime(endStr) + ":00";
            String startTime = correctTime(startStr) + ":00";

            String currDateStr = yyyyMMdd.format(todayDate);
            //转换成date格式
            Date start = yyyyMMddHH_mm_ss.parse(currDateStr + startTime);
            Date end = yyyyMMddHH_mm_ss.parse(currDateStr + endTime);

            LogUtil.D(TAG, currDateStr + startTime);
            LogUtil.D(TAG, currDateStr + endTime);

            return new Date[]{start, end};
        } catch (Exception e) {
            LogUtil.E(TAG, "解析插播时间出错：" + e.getMessage());
            return null;
        }
    }

    //修正播放时间
    private static String correctTime(String time) {
        String[] beginTimes = time.split(":");
        for (int i = 0; i < beginTimes.length; i++) {
            String temp = beginTimes[i];
            if (temp.length() <= 1) {
                temp = "0" + temp;
            }
            beginTimes[i] = temp;
        }
        return beginTimes[0] + ":" + beginTimes[1];
    }

    //解析播放时间
    public static Date[] resolveTime(String playDate, String playTime) {
        try {
            if (!playDate.contains("-")) {
                throw new Exception("playDate formal error!");
            }
            if (!playTime.contains("-")) {
                throw new Exception("playTime formal error!");
            }
            //切割开始结束时间
            String[] dates = playDate.split("-");
            String[] times = playTime.split("-");
            //获取当年月日
            Date currDateTime = new Date(System.currentTimeMillis());
            String currDateStr = yyyyMMdd.format(currDateTime);
            //转换成date格式
            Date currDate = yyyyMMdd.parse(currDateStr);
            Date beginDate = yyyyMMdd.parse(dates[0]);
            Date endDate = yyyyMMdd.parse(dates[1]);
            //对比
            if (currDate.getTime() < beginDate.getTime() || currDate.getTime() > endDate.getTime()) {
                return null;
            }

            //修正时间字符串
            String sTime = currDateStr + correctTime(times[0]) + ":00";
            String eTime = currDateStr + correctTime(times[1]) + ":00";
            //转换成date格式
            final Date beginTime = yyyyMMddHH_mm_ss.parse(sTime);
            final Date endTime = yyyyMMddHH_mm_ss.parse(eTime);

            if (endTime.getTime() < yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(currDateTime)).getTime()) {
                return null;
            }

            return new Date[]{beginTime, endTime};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析播放时长
    public static Date[] resolveTimeLong(String playDate, String playTime) {
        try {
            //当前时间
            long currTime = System.currentTimeMillis();
            //开始时间
            Date start = new Date(currTime);
            String todayStr = yyyyMMdd.format(start);
            Date today = yyyyMMdd.parse(todayStr);

            String[] split = playDate.split("-");
            Date startDate = yyyyMMdd.parse(split[0]);
            Date endDate = yyyyMMdd.parse(split[1]);
            if (!(today.getTime() >= startDate.getTime() && today.getTime() <= endDate.getTime())) {
                LogUtil.D(TAG, "不在插播字幕时间内");
                return null;
            }

            Date end = new Date(currTime + (Integer.valueOf(playTime) * 1000));
            return new Date[]{start, end};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
