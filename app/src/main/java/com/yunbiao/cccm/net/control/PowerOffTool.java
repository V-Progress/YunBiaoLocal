package com.yunbiao.cccm.net.control;

import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.common.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.net.control.actions.JYDActions;
import com.yunbiao.cccm.net.control.actions.XBHActions;
import com.yunbiao.cccm.net.control.power.JYDBroadControl;
import com.yunbiao.cccm.net.control.power.OnOffTool;
import com.yunbiao.cccm.net.control.power.PowerTimesBean;
import com.yunbiao.cccm.common.utils.CommonUtils;
import com.yunbiao.cccm.common.utils.DialogUtil;
import com.yunbiao.cccm.common.utils.LogUtil;
import com.yunbiao.cccm.net.netcore.NetClient;
import com.yunbiao.cccm.common.utils.ThreadUtil;
import com.yunbiao.cccm.common.utils.TimerUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/24.
 */

public class PowerOffTool {

    private String TAG = getClass().getSimpleName();

    private static PowerOffTool powerOffTool = null;

    private PowerOffTool() {
    }

    public static PowerOffTool getInstance() {
        if (powerOffTool == null) {
            synchronized (PowerOffTool.class) {
                if (powerOffTool == null) {
                    powerOffTool = new PowerOffTool();
                }
            }
        }
        return powerOffTool;
    }

    /**
     * 机器开机时候，初始化开关机数据
     */
    public void initPowerData() {
//        Long[] powerOn = getPowerTime(POWER_ON);
//        Long[] powerOff = getPowerTime(POWER_OFF);
//        // 如果开关机时间没有设置，就进行网络获取
//        if (powerOn != null && powerOff != null) {
//            setPowerRestartTime();
//        } else {
//            //开关机时间为空，则去网络下载
//            getPowerOffTime(HeartBeatClient.getDeviceNo());
//        }

//        String timesrundate = SpUtils.getString(APP.getContext(), SpUtils.TIMESRUNDATE, "");
//        String timesbeanlist = SpUtils.getString(APP.getContext(), SpUtils.TIMESBEANLIST, "");
//        // 如果开关机时间没有设置，就进行网络获取
//        if (!TextUtils.isEmpty(timesrundate) && !TextUtils.isEmpty(timesbeanlist)) {
//            selectPowerOnOff();
//        } else {
        //开关机时间为空，则去网络下载
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                getPowerOffTime(HeartBeatClient.getDeviceNo());
            }
        });
//        }
    }

    StringCallback stringCallback;

    /**
     * 获取开关机时间
     *
     * @param uid 设备id
     */
    public void getPowerOffTime(final String uid) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("uid", uid);

            Response response = NetClient.getInstance().postSync(ResourceConst.REMOTE_RES.POWER_OFF_URL, params);

            if (response == null) {
                LogUtil.E("获取定时开关机失败");
                return;
            }

            String responseStr = response.body().string().replaceAll("\\\\", "");
            if (responseStr.startsWith("\"")) {
                responseStr = responseStr.substring(1, responseStr.length() - 1);
            }

            String runDate = "", listStr = "";
            if (!TextUtils.isEmpty(responseStr) && !response.equals("null")) {
                List<PowerTimesBean> timesBeanList = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(responseStr);
                runDate = jsonObject.getString("runDate");
                JSONArray runTimes = jsonObject.getJSONArray("runTimes");
                int length = runTimes.length();
                for (int i = 0; i < length; i++) {
                    JSONObject timesObject = (JSONObject) runTimes.get(i);
                    String closeTime = timesObject.getString("closeTime");
                    String openTime = timesObject.getString("openTime");

                    PowerTimesBean timesBean = new PowerTimesBean();
                    timesBean.setCloseTime(closeTime);
                    timesBean.setOpenTime(openTime);
                    timesBeanList.add(timesBean);
                }
                listStr = new Gson().toJson(timesBeanList);
            }
            CacheManager.SP.put(TIMESRUNDATE, runDate);
            CacheManager.SP.put(TIMESBEANLIST, listStr);
            selectPowerOnOff();
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*stringCallback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "获取定时开关机失败: " + e.getMessage());

                NetClient.getInstance().post(ResourceConst.REMOTE_RES.POWER_OFF_URL, params, stringCallback);
            }

            @Override
            public void onResponse(String response, int id) {
                response = response.replaceAll("\\\\", "");
                if (response.startsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }

                try {
                    String runDate = "", listStr = "";
                    if (!TextUtils.isEmpty(response) && !response.equals("null")) {
                        List<PowerTimesBean> timesBeanList = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject(response);
                        runDate = jsonObject.getString("runDate");
                        JSONArray runTimes = jsonObject.getJSONArray("runTimes");
                        int length = runTimes.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject timesObject = (JSONObject) runTimes.get(i);
                            String closeTime = timesObject.getString("closeTime");
                            String openTime = timesObject.getString("openTime");

                            PowerTimesBean timesBean = new PowerTimesBean();
                            timesBean.setCloseTime(closeTime);
                            timesBean.setOpenTime(openTime);
                            timesBeanList.add(timesBean);
                        }
                        listStr = new Gson().toJson(timesBeanList);
                    }
//                    else {
//                        runDate = "1,2,3,4,5,6,7";
//                        listStr = "[{\"closeTime\":\"14:00\",\"openTime\":\"17:10\"},"
//                                + "{\"closeTime\":\"20:40\",\"openTime\":\"9:30\"}]";
//                    }
                    CacheManager.SP.put(TIMESRUNDATE, runDate);
                    CacheManager.SP.put(TIMESBEANLIST, listStr);
                    selectPowerOnOff();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        NetClient.getInstance().post(ResourceConst.REMOTE_RES.POWER_OFF_URL, params, stringCallback);*/
    }

    /**
     * 在开关机时间集合中 选择符合当前策略的时间段
     * 并执行开关机策略
     */
    private void selectPowerOnOff() {
        String powerDate = CacheManager.SP.get(TIMESRUNDATE, "");
        String powerTimes = CacheManager.SP.get(TIMESBEANLIST, "");
        LogUtil.D(TAG, "powerDate: " + powerDate);
        LogUtil.D(TAG, "powerTimes: " + powerTimes);
        if (!TextUtils.isEmpty(powerDate) && !TextUtils.isEmpty(powerTimes)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                String current = df.format(new Date());
                long currentTime = df.parse(current).getTime();
                JSONArray jsonArray = new JSONArray(powerTimes);
                int length = jsonArray.length();
                long[] select = new long[length];
                for (int i = 0; i < length; i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    String close = jsonObject.getString("closeTime");
                    long closeTime = df.parse(close).getTime();
                    select[i] = closeTime - currentTime;
                }

                //数组中选一个大于0并且最小的
                int position = -1;
                for (int i = 0; i < length; i++) {
                    if (select[i] > 0) {
                        if (i == 0) {
                            position = 0;
                        } else {
                            if (position == -1) {
                                position = i;
                            } else {
                                if (select[position] > select[i]) {
                                    position = i;
                                }
                            }
                        }
                    }
                }
                if (position == -1) {
                    position = 0;
                }
                JSONObject useObject = (JSONObject) jsonArray.get(position);
                String powerClose = useObject.getString("closeTime");
                String poserOpen = useObject.getString("openTime");
                // 1,2,3,4,5,6,7;08:00
                String powerOff = powerDate + ";" + powerClose;
                String powerOn = powerDate + ";" + poserOpen;
                LogUtil.D(TAG, "powerOff: " + powerOff);
                LogUtil.D(TAG, "powerOn: " + powerOn);
                putPowerParam(POWER_OFF, powerOff);
                putPowerParam(POWER_ON, powerOn);

                setPowerRestartTime();
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPowerRestartTime() {
        Integer type = CommonUtils.getBroadType();
        switch (type) {
            case 0:
                setPowerRunTime();
                break;
            case 1:
                setA90();
                break;
        }
    }

    /*设置建益达板子 开关机*/
    private void setA90() {
        new JYDBroadControl();
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
            LogUtil.D(TAG, "time:::: onh:" + onh + "======onm:" + onm + "========offh:" + offh + "======offm:" + offm);
            executefailedPowerDown();
        } else {
            LogUtil.E(TAG, "time " + "没有找到开关机时间");
            OnOffTool.setDisabled();
        }
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
     * @return 1, 2, 3, 4, 5, 6, 7;08:00
     */
    public String getPowerParam(String key) {
        return CacheManager.SP.get(key, "");
    }

    /**
     * 获取下一个开关机时间
     *
     * @param powerType
     * @return
     */
    public Long[] getPowerTime(String powerType) {
        Long[] runTimeLong = null;
        // 获取当天星期几
        Date currentDate = new Date();
        Integer weekDay = getWeekDay(currentDate);
        // 获取时间
        String powerOn = getPowerParam(powerType);
        if (!TextUtils.isEmpty(powerOn)) {
            // 判断是否存在运行策略
            String[] powerOnArray = powerOn.split(";");
            String runDate = powerOnArray[0];
            String runTime = powerOnArray[1];
            String[] timeDateArray = runTime.split(":");
            Date onDate = new Date();
            onDate.setHours(Integer.parseInt(timeDateArray[0]));
            onDate.setMinutes(Integer.parseInt(timeDateArray[1]));
            boolean isEq = false;
            if (onDate.getHours() == currentDate.getHours() && onDate.getMinutes() == currentDate.getMinutes()) {
                currentDate.setMinutes(currentDate.getMinutes() + 3);
                isEq = true;
            }
            if (runDate.indexOf(weekDay.toString()) != -1) {// 如果当天是运行策略中包含的周期
                if (onDate.getTime() > currentDate.getTime()) {
                    // 如果开机时间大于当前时间，就需要设置
                    Long betwLong = onDate.getTime() - currentDate.getTime();
                    runTimeLong = formatDuring(betwLong);
                } else {
                    // 推迟到运行周期中的下一天的这个时间
                    Integer betweenDay = getBetweenDay(runDate, weekDay);
                    if (powerType.equals(POWER_ON) && isEq) {
                        onDate.setMinutes(onDate.getMinutes() + 3);
                    }
                    onDate.setDate(onDate.getDate() + betweenDay);
                    runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
                }
            } else {
                // 如果运行策略中没有这一天
                Integer nextDay = getNextDay(runDate, weekDay);
                onDate.setDate(onDate.getDate() + nextDay);
                if (powerType.equals(POWER_ON) && isEq) {
                    onDate.setMinutes(onDate.getMinutes() + 3);
                }
                runTimeLong = formatDuring(onDate.getTime() - currentDate.getTime());
            }
        }
        return runTimeLong;
    }

    public Integer getNextDay(String runDate, Integer currentWeekDay) {
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

    public Integer getBetweenDay(String runDate, Integer currentWeekDay) {
        Integer between = 0;
        String[] runDateArray = runDate.split(",");
        for (int i = 0; i < runDateArray.length; i++) {
            Integer runDateA = Integer.parseInt(runDateArray[i]);
            if (runDateA == currentWeekDay) {
                if (i == runDateArray.length - 1) {
                    between = Integer.parseInt(runDateArray[0]) + (7 - currentWeekDay);
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
    public Long[] formatDuring(Long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return new Long[]{days, hours, minutes, seconds};
    }

    public Integer getWeekDay(Date date) {
        SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
        String weekDay = dateFm.format(date);
        Integer week = 0;
        switch (weekDay) {
            case "星期一":
            case "Monday": //Wednesday
                week = 1;
                break;
            case "星期二":
            case "Tuesday":
                week = 2;
                break;
            case "星期三":
            case "Wednesday":
                week = 3;
                break;
            case "星期四":
            case "Thursday":
                week = 4;
                break;
            case "星期五":
            case "Friday":
                week = 5;
                break;
            case "星期六":
            case "Saturday":
                week = 6;
                break;
            case "星期日":
            case "Sunday":
                week = 7;
                break;
        }
        return week;
    }

    public final static String POWER_ON = "poerOn";
    public final static String POWER_OFF = "poerOff";

    public static final String TIMESRUNDATE = "times_run_date";//开关机时间日期
    public static final String TIMESBEANLIST = "times_bean_list";//开关机时间集合

    private void putPowerParam(String key, String value) {
        CacheManager.SP.put(key, value);
    }

    /**
     * 机器关机
     */
    public void shutdown() {
        DialogUtil.getInstance().showProgressDialog(APP.getMainActivity(), "关机", "3秒后设备将关机");
        TimerUtil.delayExecute(3000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                Integer broadType = CommonUtils.getBroadType();
                Intent intent = new Intent();
                if (broadType == 4) {
                    intent.setAction(XBHActions.ACTION_SHUTDOWN);
                    APP.getContext().sendBroadcast(intent);
                } else if (broadType == 5) {
                    intent.setAction(JYDActions.ACTION_SHUTDOWN);
                    APP.getContext().sendBroadcast(intent);
                } else {
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("su");
                        DataOutputStream out = new DataOutputStream(process.getOutputStream());
                        out.writeBytes("reboot -p\n");
                        out.writeBytes("exit\n");
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void reboot() {
        DialogUtil.getInstance().showProgressDialog(APP.getMainActivity(), "重启", "3秒后设备将重启");
        TimerUtil.delayExecute(3000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                Integer broadType = CommonUtils.getBroadType();
                Intent intent = new Intent();
                switch (broadType) {
                    case 0:
                    case 2:
                    case 3:
                        execSuCmd("reboot");
                        break;
                    case 1:
                        intent.setAction("com.zhsd.setting.syscmd");
                        intent.putExtra("cmd", "reboot");
                        APP.getContext().sendBroadcast(intent);
                        break;
                    case 4:
                        intent.setAction(XBHActions.ACTION_REBOOT);
                        APP.getContext().sendBroadcast(intent);
                        break;
                    case 5:
                        intent.setAction(JYDActions.ACTION_REBOOT);
                        APP.getContext().sendBroadcast(intent);
                        break;
                }
            }
        });

    }

    public void execSuCmd(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String out = new String(buffer);
            LogUtil.D("tag", out + aa);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
