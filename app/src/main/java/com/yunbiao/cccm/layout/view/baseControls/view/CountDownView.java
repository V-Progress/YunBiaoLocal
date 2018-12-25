package com.yunbiao.cccm.layout.view.baseControls.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.layout.view.baseControls.bean.ControlsDetail;
import com.yunbiao.cccm.layout.bean.Center;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/28 0028.
 */
public class CountDownView {
    private final AbsoluteLayout.LayoutParams layoutParams;
    private Context context;
    private Center layoutInfo;

    public CountDownView(Context context, Center layoutInfo, AbsoluteLayout.LayoutParams layoutParams) {
        this.context = context;
        this.layoutInfo = layoutInfo;
        this.layoutParams = layoutParams;

        initView();
        //跟随布局大小，设置字体大小
        setView();
        //设置倒计时内容
        setText();
    }

    private TextView titleTextView, sloganTextView, count_one_TextView, count_two_TextView, count_three_TextView, count_day_TextView, day;
    private ImageView bgImageView;
    private TextView tv_timer_date;
    private TextView countOneTextView, countTwoTextView, countThreeTextView;
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

    private void setText() {
        ControlsDetail controlsDetail = getCountDown(layoutInfo);
        //后台传值标题、口号
        String slogan = controlsDetail.getSlogan();
        String title = controlsDetail.getTitle();
        String textColor = controlsDetail.getTextColor();
        String bgColor = controlsDetail.getBgColor();
        String timeColor = controlsDetail.getTimeColor();

        titleTextView.setText(title);
        sloganTextView.setText(slogan);
        day.setTextColor(Color.parseColor(textColor));
        titleTextView.setTextColor(Color.parseColor(textColor));
        sloganTextView.setTextColor(Color.parseColor(textColor));
        bgImageView.setBackgroundColor(Color.parseColor(bgColor));
        tv_timer_date.setTextColor(Color.parseColor(textColor));
        if (timeColor.startsWith("#")) {
            countOneTextView.setTextColor(Color.parseColor(timeColor));
            countTwoTextView.setTextColor(Color.parseColor(timeColor));
            countThreeTextView.setTextColor(Color.parseColor(timeColor));
        }

        //后台传值的时间，目标时间
        String endTimer = controlsDetail.getEndDate();
        tv_timer_date.setText("截止日期： " + endTimer);
        String splitEndTimer[] = endTimer.split("-");
        String startYear = splitEndTimer[0];
        String startMonth = splitEndTimer[1];
        String startDay = splitEndTimer[2];
        //获取当前时间
        Date date = new Date();
        Date currentDate = null;
        Date targetDate = null;
        int day = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//格式化
            currentDate = sdf.parse(sdf.format(date));
            targetDate = sdf.parse(startYear + startMonth + startDay);
            long currentTime = currentDate.getTime();
            long endTime = targetDate.getTime();
            long countDownDay = endTime - currentTime;
            day = (int) (countDownDay / 24 / 60 / 60 / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (day >= 0 && day <= 9) {
            count_one_TextView.setText("0");
            count_two_TextView.setText("0");
            count_three_TextView.setText(String.valueOf(day));
        } else if (day >= 10 && day <= 99) {
            count_one_TextView.setText("0");
            count_two_TextView.setText(String.valueOf(day / 10));
            count_three_TextView.setText(String.valueOf(day % 10));
        } else if (day >= 100 && day <= 999) {
            count_one_TextView.setText(String.valueOf(day / 100));
            count_two_TextView.setText(String.valueOf(day / 10 % 10));
            count_three_TextView.setText(String.valueOf(day % 10));
        } else if (day < 0) {
            count_one_TextView.setText("0");
            count_two_TextView.setText("0");
            count_three_TextView.setText("0");
        } else {
            Toast.makeText(APP.getContext(), "倒计时时间范围在999天以内", Toast.LENGTH_SHORT).show();
        }
    }


    //获得页面的大小，调整字体的大小
    private void setView() {
        int width = layoutParams.width;
        int height = layoutParams.height;


        int widthMultiple = 1920 / width;

        int title_size = 60 / widthMultiple;
        int slogan_size = 45 / widthMultiple;
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, title_size);
        count_day_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, title_size);
        sloganTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, slogan_size);
        tv_timer_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, slogan_size);

        int countMultiple = 1;
        double i = 1920 * 1.0 / 1080;
        double i1 = width * 1.0 / height;
        if (i > i1) {
            countMultiple = 1920 / width;
        } else if (i < i1) {
            countMultiple = 1920 / height;
        } else {
            countMultiple = 1920 / width;
        }

        int count_size = 300 / countMultiple;
        count_one_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, count_size);
        count_two_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, count_size);
        count_three_TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, count_size);
    }

    private View view;

    public View getView() {
        return view;
    }

    private View initView() {
        view = View.inflate(context, R.layout.countdown_layout, null);
        titleTextView = (TextView) view.findViewById(R.id.tv_countdown_title);
        sloganTextView = (TextView) view.findViewById(R.id.tv_countdown_slogan);
        count_one_TextView = (TextView) view.findViewById(R.id.tv_countdown_one);
        count_two_TextView = (TextView) view.findViewById(R.id.tv_countdown_two);
        count_three_TextView = (TextView) view.findViewById(R.id.tv_countdown_three);
        count_day_TextView = (TextView) view.findViewById(R.id.tv_countdown_day);
        bgImageView = (ImageView) view.findViewById(R.id.iv_countdown_bg);
        day = (TextView) view.findViewById(R.id.tv_countdown_day);
        tv_timer_date = (TextView) view.findViewById(R.id.tv_timer_date);
        countOneTextView = (TextView) view.findViewById(R.id.tv_countdown_one);
        countTwoTextView = (TextView) view.findViewById(R.id.tv_countdown_two);
        countThreeTextView = (TextView) view.findViewById(R.id.tv_countdown_three);

        return view;
    }


}
