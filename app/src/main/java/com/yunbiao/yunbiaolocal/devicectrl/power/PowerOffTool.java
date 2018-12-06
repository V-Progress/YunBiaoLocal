package com.yunbiao.yunbiaolocal.devicectrl.power;

import android.text.TextUtils;
import android.util.Log;

import com.xboot.stdcall.OnOffTool;
import com.yunbiao.yunbiaolocal.cache.FileCache;
import com.yunbiao.yunbiaolocal.common.ResConstants;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;


public class PowerOffTool {

    private static PowerOffTool instance;
    public final static String POWER_ON = "poerOn";
    public final static String POWER_OFF = "poerOff";

    public static synchronized PowerOffTool getInstance(){
        if(instance == null){
            instance = new PowerOffTool();
        }
        return instance;
    }

    public void getPowerOffTime(String uid){
        HashMap<String, String> paramMap = new HashMap();
        paramMap.put("uid", uid);
        NetUtil.getInstance().post(ResConstants.POWER_OFF_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                if (response.startsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }
                putParam(response);
            }
        });
    }

    private void putParam(String powerOffJson) {
        powerOffJson = powerOffJson.replaceAll("\\\\", "");
        if (powerOffJson.startsWith("\"")) {
            powerOffJson = powerOffJson.substring(1, powerOffJson.length() - 1);
        }
        JSONTokener jsonParser = new JSONTokener(powerOffJson);
        try {
            // 开机字符串
            String powerOn = "";
            // 关机字符串
            String powerOff = "";

            JSONArray person = (JSONArray) jsonParser.nextValue();
            for (int i = 0; i < person.length(); i++) {
                JSONObject jsonObject = (JSONObject) person.get(i);
                Integer status = jsonObject.getInt("status");
                Integer runType = jsonObject.getInt("runType");
                String runDate = jsonObject.getString("runDate");
                if (runDate.indexOf(":") != -1) {
                    runDate = runDate.substring(runDate.indexOf(":") + 1, runDate.length());//1,2,3,4,5,6,7,
                }

                String runTime = jsonObject.getString("runTime");
                if (runType == 0 && status == 0) {
                    powerOn = runDate + ";" + runTime;
                } else if (runType == 1 && status == 0) {
                    powerOff = runDate + ";" + runTime;
                }
            }

            // 1,2,3,4,5,6,7;08:00
            putPowerParam(POWER_ON, powerOn);
            putPowerParam(POWER_OFF, powerOff);

            setPowerRestartTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putPowerParam(String key, String value) {
        FileCache acache = FileCache.get(new File(ResConstants.RESOURSE_PATH + ResConstants.PROPERTY_CACHE_PATH));
        acache.put(key, value);
    }

    private void setPowerRestartTime() {
        Integer type = CommonUtils.getBroadType();
        Log.d("poweroff--板子", "setPowerRestartTime: " + type);
        switch (type.intValue()) {
            case 0:
                setPowerRunTime();
                break;
            case 1:
                setZHPoweroff();
                break;
            case 2:
                setHongShiDa();
                break;
            case 3:
            case 4:
                setYiSheng();
                break;
            case 5:
                setJYD();
                break;
        }
    }

    /**
     * 设置机器的开关机时间
     */
    public void setPowerRunTime() {
        Long[] powerOn = getPowerTime(POWER_ON);
        Long[] powerOff = getPowerTime(POWER_OFF);
        // 如果开关机时间没有设置，就进行网络获取
        if (powerOn != null && powerOff != null) {
            Long offh = powerOff[0] * 24 + powerOff[1];
            Long offm = powerOff[2];
            // 0 23 26
            // onh:0 onm:1
            // 0 23 25
            // offh:23 offm:25
            Long onh = powerOn[0] * 24 + powerOn[1];
            Long onm = powerOn[2];
            if ((onh * 60 + onm) > (offh * 60 + offm)) {
                long offset = (onh * 60 + onm) - (offh * 60 + offm);
                onm = offset % 60;
                onh = offset / 60;
            }
            OnOffTool.setEnabled(onh.byteValue(), onm.byteValue(), offh.byteValue(), offm.byteValue());
            Log.e("time::::", "onh:" + onh + "======onm:" + onm + "========offh:" + offh + "======offm:" + offm);
            executefailedPowerDown();
        } else {
            Log.e("time", "没有找到开关机时间");
            OnOffTool.setDisabled();
        }
    }
    /**
     * @return 1, 2, 3, 4, 5, 6, 7;08:00
     */
    public String getPowerParam(String key) {
        FileCache acache = FileCache.get(new File(ResConstants.RESOURSE_PATH + ResConstants.PROPERTY_CACHE_PATH));
        return acache.getAsString(key);
    }

    /*如果没有正常关机，中途启动了就再次关机*/
    private void executefailedPowerDown() {
        // 获取当天星期几
        Date currentDate = new Date();
        Integer weekDay = getWeekDay(currentDate);
        // 获取时间
        String poweroff = getPowerParam(POWER_OFF);
        if (!TextUtils.isEmpty(poweroff)) {
            // 判断是否存在运行策略
            String[] powerOnArray = poweroff.split(";");
            String runDate = powerOnArray[0];
            String runTime = powerOnArray[1];
            String[] timeDateArray = runTime.split(":");

            String currentTime = CommonUtils.getStringDate();
            String[] timePice = currentTime.split(":");
            int currentHour = Integer.valueOf(timePice[0]);
            int currentMinute = Integer.valueOf(timePice[1]);
            int closeHour = Integer.valueOf(timeDateArray[0]);
            int closeMinute = Integer.valueOf(timeDateArray[1]);

            if (runDate.indexOf(weekDay.toString()) != -1) {// 如果当天是运行策略中包含的周期
                int intervalTime = 6;
                if (currentHour > closeHour) {
                    intervalTime = 60 - closeMinute + currentMinute;
                } else if (currentHour == closeHour) {
                    intervalTime = Math.abs(currentMinute - closeMinute);
                }
                setPowerFailedDown(intervalTime);
            }
        }
    }

    /*//开关机失败。间隔时间小于5秒，重设开关机时间*/
    private void setPowerFailedDown(int intervalTime) {
        if (intervalTime < 5) {
            Long[] powerOn = getPowerTime(POWER_ON);
            if (powerOn != null) {
                Long onh = powerOn[0] * 24 + powerOn[1];
                Long onm = powerOn[2];
                if ((onh * 60 + onm) > 2) {
                    OnOffTool.setEnabled(onh.byteValue(), onm.byteValue(), (byte) 0, (byte) 2);
                }
            }
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


    /**
     * 中恒板子开关机接口
     * String msg="[{checkSwitch:true,type:0,settings:[
     * {switch:true,wakeupTime:\"10:05\",sleepTime:\"10:45\",week:[\"tuesday\",\"thursday\",\"saturday\"]},
     * {switch:true,wakeupTime:\"11:05\",sleepTime:\"11:45\",week:[\"tuesday\",\"thursday\",\"saturday\"]}
     * ]}]";
     */
    public void setZHPoweroff() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("checkSwitch", "true");
            jsonObject.put("type", 0);
            String powerOn = getPowerParam(POWER_ON);
            String powerOff = getPowerParam(POWER_OFF);
            if (!TextUtils.isEmpty(powerOn) && !TextUtils.isEmpty(powerOff)) {
                String[] powerOnStirng = powerOn.split(";");
                String[] powerOffStirng = powerOff.split(";");
                if (powerOnStirng.length == 2) {
                    String powerOnTime = powerOnStirng[1];
                    String powerOffTime = powerOffStirng[1];
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("switch", "true");
                    jsonObject1.put("wakeupTime", powerOnTime);
                    jsonObject1.put("sleepTime", powerOffTime);
                    JSONArray weekArray = getjsonArray(powerOnStirng[0]);
                    jsonObject1.put("week", weekArray);
                    jsonArray.put(jsonObject1);
                    jsonObject.put("settings", jsonArray);
                }
            } else {
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("settings", jsonArray);
            }

            ZHBroadControl.getZhBroadControl().setPowerOnOff(String.valueOf(jsonObject));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*中恒板子接口用*/
    private JSONArray getjsonArray(String powerOnStirng) {
        JSONArray jsonArray = new JSONArray();
        String[] weekNumbers = powerOnStirng.split(",");
        for (int i = 0; i < weekNumbers.length; i++) {
            switch (i) {
                case 1:
                    jsonArray.put("monday");
                    break;
                case 2:
                    jsonArray.put("tuesday");
                    break;
                case 3:
                    jsonArray.put("wednesday");
                    break;
                case 4:
                    jsonArray.put("thursday");
                    break;
                case 5:
                    jsonArray.put("friday");
                    break;
                case 6:
                    jsonArray.put("saturday");
                    break;
                case 7:
                    jsonArray.put("sunday");
                    break;
            }
        }
        return jsonArray;
    }

    /*设置深圳鸿世达科技板子 开关机*/
    private void setHongShiDa() {
        new HongShiDaBroadControl();
    }

    /*设置亿晟板子 开关机*/
    private void setYiSheng() {
        new YiShengBroadControl();
    }

    /*设置建益达板子 开关机*/
    private void setJYD() {
        new JYDBroadControl();
    }

}
