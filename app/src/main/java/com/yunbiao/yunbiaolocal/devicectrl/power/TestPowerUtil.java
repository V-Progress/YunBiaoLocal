package com.yunbiao.yunbiaolocal.devicectrl.power;

import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.utils.DateUtil;
import com.yunbiao.yunbiaolocal.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/12/6.
 */

public class TestPowerUtil {

    public static void resolvePowerData(List<PowerModel> powerModels) {
        for (PowerModel powerModel : powerModels) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

            int intWeek = calendar.get(Calendar.DAY_OF_WEEK);



            intWeek = intWeek-1;
            if(intWeek < 1){
                intWeek = 7;
            }

            Date tDay = calendar.getTime();
            String s = DateUtil.yyyyMMdd_Format(tDay);
            LogUtil.E("今天的日期："+s);
            LogUtil.E("今天是星期"+corretWeek(intWeek));
            //关机
            if(TextUtils.equals("0",String.valueOf(powerModel.getRunType()))){
                if(powerModel.getRunDate().contains(String.valueOf(intWeek))){
                    LogUtil.E("关机策略中包含今天，添加定时关机");
                }
            }


            calendar.setTime(tDay);
            calendar.add(calendar.DATE,1);
            Date tmDay = calendar.getTime();
            String s1 = DateUtil.yyyyMMdd_Format(tmDay);
            int tomWeek = intWeek + 1;
            if(tomWeek > 7){
                tomWeek = 1;
            }
            LogUtil.E("明天的日期："+s1);
            LogUtil.E("明天是星期："+corretWeek(intWeek+1));

            if(TextUtils.equals("1",String.valueOf(powerModel.getRunType()))){
                String tmWeek = String.valueOf(tomWeek);
                if(powerModel.getRunDate().contains(tmWeek)){
                    LogUtil.E("开机策略中包含明天，添加定时开机");
                }
            }
            LogUtil.E("-----------------");
        }
    }

    private static int corretWeek(int week){
        week = week - 1;
        if(week<1)
            week = 7;
        if(week>7)
            week = 1;
        return week;
    }

    private void getWeek(){

    }


}
