package com.yunbiao.cccm.net2.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2018/6/12.
 */

public class DateUtil {
    private static final String Y_M_D = "yyyy-MM-dd";
    private static final String Y_M_D_H_M_S = "yyyy-MM-dd HH:mm:ss";
    private static final String Y_M_D_H_M = "yyyy-MM-dd HH:mm:00";

    private static DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private static DateFormat yyyy_MM_dd_HH_mm = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String yyyyMMdd_Format(Date d) {
        return yyyyMMdd.format(d);
    }

    public static Date yyyyMMdd_Parse(String s) {
        try {
            return yyyyMMdd.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getToday_str() {
        return DateUtil.yyyy_MM_dd_Format(new Date());
    }

    public static String getTomm_str() {
        return DateUtil.yyyy_MM_dd_Format(getTommDate(getTodayStr()));
    }

    public static String getTodayStr() {
        return DateUtil.yyyyMMdd_Format(new Date());
    }

    public static Date getTodayDate() {
        return DateUtil.yyyyMMdd_Parse(getTodayStr());
    }

    public static String getTommStr() {
        return getTommStr(getTodayStr());
    }

    public static String getTommStr(String todayStr) {
        return DateUtil.yyyyMMdd_Format(getTommDate(todayStr));
    }

    public static Date getTommDate(String todayStr) {
        Date today = DateUtil.yyyyMMdd_Parse(todayStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        int day = calendar.get(Calendar.DATE);
        calendar.set(Calendar.DATE, day + 1);
        return calendar.getTime();
    }

    public static String yyyy_MM_dd_Format(Date d) {
        return yyyy_MM_dd.format(d);
    }

    public static String yyyy_MM_dd_Format(String date) {
        Date parse = yyyyMMdd_Parse(date);
        return yyyy_MM_dd.format(parse);
    }

    public static Date yyyy_MM_dd_Parse(String s){
        try {
            return yyyy_MM_dd.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String yyyyMMddHH_mm_Format(Date d) {
        return yyyyMMddHH_mm.format(d);
    }

    public static Date yyyyMMddHH_mm_Parse(String s) {
        try {
            return yyyyMMddHH_mm.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String yyyy_MM_dd_HH_mm_Format(Date d) {
        return yyyy_MM_dd_HH_mm.format(d);
    }

    public static Date yyyy_MM_dd_HH_mm_Parse(String s) throws ParseException {
        return yyyy_MM_dd_HH_mm.parse(s);
    }

    private DateUtil() {
    }

    private static volatile DateUtil dateUtil = new DateUtil();

    public static DateUtil getInstance() {
        return dateUtil;
    }

    /**
     * @param currentDate
     * @param format
     * @return
     */
    public static String dateToStrByFormat(Date currentDate, String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(currentDate);
    }

    public Date getNewDateByFormat(String fromat) {
        Date date = new Date();
        if (Y_M_D_H_M_S.equals(fromat)) {
            return date;
        }
        String dateToStr = dateToStr(date, fromat);
        return strToDate(dateToStr, Y_M_D_H_M_S);
    }

    public String dateToStr(Date date, String formatStr) {
        String dateStr = "";
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        dateStr = format.format(date);
        return dateStr;
    }

    public Date strToDate(String dateStr, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取今天指定时分秒date
     *
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public Date getTodyDateByset(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date time = calendar.getTime();
        String s = dateToStr(time, Y_M_D_H_M_S);
        Log.e("getTodyDateByset", "getTodyDateByset: " + s);
        return calendar.getTime();
    }

    /**
     * 获取指定今天指定时分秒时间，如果设定时间已过就自增下一天
     *
     * @param hour
     * @param minute
     * @param second
     */
    public Date getSetDate(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date date = calendar.getTime(); //第一次执行定时任务的时间
        //如果第一次执行定时任务的时间 小于当前的时间
        //此时要在 第一次执行定时任务的时间加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(new Date())) {
            date = this.addDay(date, 1);
        }
        return date;
    }

    /**
     * 增加或减小天数
     *
     * @param date
     * @param num
     * @return
     */
    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    /**
     * 判断开始时间是否小于结束时间
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public boolean compareDate(Date startDate, Date endDate) {
        return startDate.getTime() < endDate.getTime();
    }
}
