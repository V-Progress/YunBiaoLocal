package com.yunbiao.yunbiaolocal.layout.view.baseControls.util;

import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;


import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.yunbiao.yunbiaolocal.utils.DateUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/7/21 0021.
 */
public class CalendarThread extends Thread {
    public TextView weatherTextView, numTextView;
    public ImageView weatherImageView, weatherBgImageView;
    public String city;

    public CalendarThread(TextView weatherTextView, ImageView weatherImageView, ImageView weatherBgImageView, TextView
            numTextView, String city) {
        this.weatherTextView = weatherTextView;
        this.weatherImageView = weatherImageView;
        this.weatherBgImageView = weatherBgImageView;
        this.numTextView = numTextView;
        this.city = city;
    }

    public void run() {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("city", city);

        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.WEATHER_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String result, int id) {
//天气信息
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] weatherArray = result.split("\\|");
                if (weatherArray.length == 3) {
                    String weather = weatherArray[0];
                    weatherTextView.setText(weather);
                    if (weather.contains("阴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_cloud);
                        weatherBgImageView.setImageResource(R.mipmap.bg_fog);
                    } else if (weather.contains("雨")) {
                        weatherImageView.setImageResource(R.mipmap.cal_rain);
                        weatherBgImageView.setImageResource(R.mipmap.bg_rain);
                    } else if (weather.contains("雪")) {
                        weatherImageView.setImageResource(R.mipmap.cal_snow);
                        weatherBgImageView.setImageResource(R.mipmap.bg_snow);
                    } else if (weather.contains("晴")) {
                        weatherImageView.setImageResource(R.mipmap.cal_sun);
                        weatherBgImageView.setImageResource(R.mipmap.bg_sunny);
                    } else if (weather.contains("霾")) {
                        weatherImageView.setImageResource(R.mipmap.cal_mai);
                        weatherBgImageView.setImageResource(R.mipmap.bg_fog);
                    } else if (weather.contains("雷")) {
                        weatherImageView.setImageResource(R.mipmap.cal_thundershower);
                        weatherBgImageView.setImageResource(R.mipmap.bg_thundershower);
                    }
                }
            }
        });

        HashMap<String, String> carMap = new HashMap<String, String>();
        paramMap.put("deviceId", HeartBeatClient.getDeviceNo());
        paramMap.put("city", city);
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.CARRUN_URL, carMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String result, int id) {
//车辆限号
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] carNumArray = result.split(",");
                Date currentDate = new Date();
                String date = DateUtil.dateToStrByFormat(currentDate, "E");
                if (carNumArray.length == 7) {
                    switch (date) {
                        case "周一":
                            numTextView.setText("今日限行: " + carNumArray[0]);
                            break;
                        case "周二":
                            numTextView.setText("今日限行: " + carNumArray[1]);
                            break;
                        case "周三":
                            numTextView.setText("今日限行: " + carNumArray[2]);
                            break;
                        case "周四":
                            numTextView.setText("今日限行: " + carNumArray[3]);
                            break;
                        case "周五":
                            numTextView.setText("今日限行: " + carNumArray[4]);
                            break;
                        case "周六":
                            numTextView.setText("今日限行: " + carNumArray[5]);
                            break;
                        case "周日":
                            numTextView.setText("今日限行: " + carNumArray[6]);
                            break;
                    }
                } else {
                    numTextView.setText("");
                }
            }
        });
        //两个小时刷新一次
        new Handler().postDelayed(this, 1000 * 60 * 120);
    }
}
