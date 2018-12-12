package com.yunbiao.yunbiaolocal.layout.view.baseControls.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.bean.ControlsDetail;
import com.yunbiao.yunbiaolocal.layout.bean.Center;
import com.yunbiao.yunbiaolocal.utils.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/2 0002.
 */
public class TimerView {
    private View view;

    public View getView() {
        return view;
    }

    private Context context;
    private Center layoutInfo;

    public TimerView(Context context, Center layoutInfo) {

        this.context = context;
        this.layoutInfo = layoutInfo;

        initView();
        setView();
    }

    //获取时间显示
    private void setView() {
        //当前时间
        new TimeThread().start();
        //后台传值的时间
        ControlsDetail controlsDetail = getCountDown(layoutInfo);
        String title = controlsDetail.getTitle();
        titleTextView.setText(title);
        //设置颜色
        String bgColor = controlsDetail.getBgColor();
        String textColor = controlsDetail.getTextColor();
        bgImageView.setBackgroundColor(Color.parseColor(bgColor));
        TextView01.setTextColor(Color.parseColor(textColor));
        TextView02.setTextColor(Color.parseColor(textColor));
        TextView03.setTextColor(Color.parseColor(textColor));
        TextView04.setTextColor(Color.parseColor(textColor));
        TextView05.setTextColor(Color.parseColor(textColor));
        TextView06.setTextColor(Color.parseColor(textColor));
        TextView07.setTextColor(Color.parseColor(textColor));
        TextView08.setTextColor(Color.parseColor(textColor));
        TextView09.setTextColor(Color.parseColor(textColor));
        TextView10.setTextColor(Color.parseColor(textColor));
        TextView11.setTextColor(Color.parseColor(textColor));
        TextView12.setTextColor(Color.parseColor(textColor));
        TextView13.setTextColor(Color.parseColor(textColor));
        TextView14.setTextColor(Color.parseColor(textColor));


        String startTimer = controlsDetail.getStartDate();
        String splitStartTimer[] = startTimer.split("-");
        String startYear = splitStartTimer[0];
        String startMonth = splitStartTimer[1];
        String startDay = splitStartTimer[2];
        startYearTextView.setText(startYear);
        startMonthTextView.setText(startMonth);
        startDayTextView.setText(startDay);

        //当前时间-开始时间=运行天数
        Date date = new Date();//获取当前时间
        Date currentDate = null;
        Date startDate = null;
        int day = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//格式化
            currentDate = sdf.parse(sdf.format(date));
            startDate = sdf.parse(startYear + startMonth + startDay);
            long currentTime = currentDate.getTime();
            long startTime = startDate.getTime();
            long countDownDay = currentTime - startTime;
            day = (int) (countDownDay / 24 / 60 / 60 / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        countTextView.setText(String.valueOf(day));
    }

    private TextView currentYearTextView, currentMonthTextView, currentDayTextView;
    private TextView currentHourTextView, currentMinuteTextView, currentDateTextView;
    private TextView startYearTextView, startMonthTextView, startDayTextView;
    private TextView titleTextView, countTextView;
    private TextView TextView01, TextView02, TextView03, TextView04, TextView05, TextView06, TextView07, TextView08;
    private TextView TextView09, TextView10, TextView11, TextView12, TextView13,TextView14;
    private ImageView bgImageView;
    private static final int msgKey1 = 1;


    //初始化视图
    private View initView() {
        view = View.inflate(context, R.layout.timer_layout, null);
        currentYearTextView = (TextView) view.findViewById(R.id.tv_timer_end_year);
        currentMonthTextView = (TextView) view.findViewById(R.id.tv_timer_end_month);
        currentDayTextView = (TextView) view.findViewById(R.id.tv_timer_end_day);
        currentHourTextView = (TextView) view.findViewById(R.id.tv_timer_today_hour);
        currentMinuteTextView = (TextView) view.findViewById(R.id.tv_timer_today_minute);
        currentDateTextView = (TextView) view.findViewById(R.id.tv_timer_today_date);
        startYearTextView = (TextView) view.findViewById(R.id.tv_timer_start_year);
        startMonthTextView = (TextView) view.findViewById(R.id.tv_timer_start_month);
        startDayTextView = (TextView) view.findViewById(R.id.tv_timer_start_day);
        titleTextView = (TextView) view.findViewById(R.id.tv_timer_title);
        countTextView = (TextView) view.findViewById(R.id.tv_timer_all_day);

        bgImageView = (ImageView) view.findViewById(R.id.iv_timer_bg);
        TextView01 = (TextView) view.findViewById(R.id.tv_timer_position01);
        TextView02 = (TextView) view.findViewById(R.id.tv_timer_position02);
        TextView03 = (TextView) view.findViewById(R.id.tv_timer_position03);
        TextView04 = (TextView) view.findViewById(R.id.tv_timer_position04);
        TextView05 = (TextView) view.findViewById(R.id.tv_timer_position05);
        TextView06 = (TextView) view.findViewById(R.id.tv_timer_position06);
        TextView07 = (TextView) view.findViewById(R.id.tv_timer_position07);
        TextView08 = (TextView) view.findViewById(R.id.tv_timer_position08);
        TextView09 = (TextView) view.findViewById(R.id.tv_timer_position09);
        TextView10 = (TextView) view.findViewById(R.id.tv_timer_position10);
        TextView11 = (TextView) view.findViewById(R.id.tv_timer_position11);
        TextView12 = (TextView) view.findViewById(R.id.tv_timer_position12);
        TextView13 = (TextView) view.findViewById(R.id.tv_timer_position13);
        TextView14 = (TextView) view.findViewById(R.id.tv_timer_position14);

        return view;
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1 * 1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    //分段设置时间
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    Date currentDate = new Date();
                    currentYearTextView.setText(DateUtil.dateToStrByFormat(currentDate, "yyyy"));
                    currentMonthTextView.setText(DateUtil.dateToStrByFormat(currentDate, "MM"));
                    currentDayTextView.setText(DateUtil.dateToStrByFormat(currentDate, "dd"));
                    currentHourTextView.setText(DateUtil.dateToStrByFormat(currentDate, "HH"));
                    currentMinuteTextView.setText(DateUtil.dateToStrByFormat(currentDate, "mm"));
                    String date = DateUtil.dateToStrByFormat(currentDate, "E").substring(1, 2);
                    currentDateTextView.setText(date);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 获取倒计时信息
     */
    public static ControlsDetail getCountDown(Center layoutInfo) {
        ControlsDetail controlsDetail = new ControlsDetail();
        String endDate = layoutInfo.getControlsDetail().getEndDate();
        String slogan = layoutInfo.getControlsDetail().getSlogan();
        String title = layoutInfo.getControlsDetail().getTitle();
        String startDate = layoutInfo.getControlsDetail().getStartDate();
        String textColor = layoutInfo.getControlsDetail().getTextColor();
        String bgColor = layoutInfo.getControlsDetail().getBgColor();
        String timeColor = layoutInfo.getControlsDetail().getTimeColor();

        controlsDetail.setEndDate(endDate);
        controlsDetail.setSlogan(slogan);
        controlsDetail.setTitle(title);
        controlsDetail.setStartDate(startDate);
        controlsDetail.setTextColor(textColor);
        controlsDetail.setBgColor(bgColor);
        controlsDetail.setTimeColor(timeColor);

        return controlsDetail;
    }
}
