package com.yunbiao.yunbiaolocal.layout.view.baseControls;

import android.content.Context;
import android.view.View;
import android.widget.AbsoluteLayout;

import com.yunbiao.yunbiaolocal.layout.view.baseControls.view.CalendarTwoView;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.view.CountDownView;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.view.RMBrateView;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.view.TimerView;
import com.yunbiao.yunbiaolocal.layout.bean.Center;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.view.CalendarView;

public class BusinessBase {

    private static BusinessBase businessBase;

    public static BusinessBase getInstance() {
        if (businessBase == null) {
            businessBase = new BusinessBase();
        }
        return businessBase;
    }

    //基础控件
    public View runBaseControlsView(Context context, Center center, AbsoluteLayout.LayoutParams layoutParams) {
        Integer windowType = center.getWindowType();
        if (windowType == 1) {//天气
            CalendarView calendarView = new CalendarView(context);
            View calendarViewView = calendarView.getView();
            calendarViewView.setLayoutParams(layoutParams);
            return calendarViewView;
        } else if (windowType == 2) {//黑色背景日历
            CalendarTwoView calendarTwoView = new CalendarTwoView(context);
            View calendarTwoViewView = calendarTwoView.getView();
            calendarTwoViewView.setLayoutParams(layoutParams);
            return calendarTwoViewView;
        } else if (windowType == 3) {//汇率
            RMBrateView rmbRateView = new RMBrateView(context);
            View rmBrateViewView = rmbRateView.getView();
            rmBrateViewView.setLayoutParams(layoutParams);
            return rmBrateViewView;
        } else if (windowType == 4) {//倒计时
            CountDownView countDownView = new CountDownView(context, center, layoutParams);
            View countDownViewView = countDownView.getView();
            countDownViewView.setLayoutParams(layoutParams);
            return countDownViewView;
        } else if (windowType == 5) {//计时器
            TimerView timerView = new TimerView(context, center);
            View timerViewView = timerView.getView();
            timerViewView.setLayoutParams(layoutParams);
            return timerViewView;
        }
        return null;
    }
}
