package com.yunbiao.cccm.layout.view.baseControls.util;

import android.os.Handler;
import android.widget.TextView;

import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.utils.NetUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/7/25 0025.
 */
public class CalendarTwoThread extends Thread {
    private TextView temperatureTextView, pm25TextView;
    public String city;

    public CalendarTwoThread(TextView temperatureTextView, TextView pm25TextView, String city) {
        this.temperatureTextView = temperatureTextView;
        this.pm25TextView = pm25TextView;
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
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                if (result.endsWith("\"")) {
                    result = result.substring(0, result.length() - 2);
                }
                String[] weatherArray = result.split("\\|");
                if (weatherArray.length == 3) {
                    String weatherTemperature = weatherArray[1];
                    if (weatherTemperature.contains("~")) {
                        weatherTemperature = weatherTemperature.replace("~", "/");
                        weatherTemperature = weatherTemperature.replaceAll(" ", "");
                    }
                    temperatureTextView.setText(weatherTemperature);

                    //0~50，一级，优，绿色；  51~100，二级，良，黄色； 101~150，三级，轻度污染，橙色； 151~200，四级，中度污染 ，红色； 201~300，五级，重度污染 ，紫色；
                    // >300，六级，严重污染， 褐红色。
                    Integer pm25 = Integer.parseInt(weatherArray[2]);
                    if (pm25 != -1) {
                        if (pm25 >= 0 && pm25 <= 50) {
                            pm25TextView.setText("优");
                        } else if (pm25 >= 51 && pm25 <= 100) {
                            pm25TextView.setText("良");
                        } else if (pm25 >= 101 && pm25 <= 150) {
                            pm25TextView.setText("轻度");
                        } else if (pm25 >= 151 && pm25 <= 200) {
                            pm25TextView.setText("中度");
                        } else if (pm25 >= 201 && pm25 <= 300) {
                            pm25TextView.setText("重度 ");
                        } else if (pm25 > 300) {
                            pm25TextView.setText("严重");
                        }
                    } else {
                        pm25TextView.setText("");
                    }
                }
            }
        });
        //两个小时刷新一次
        new Handler().postDelayed(this, 1000 * 60 * 120);
    }
}
