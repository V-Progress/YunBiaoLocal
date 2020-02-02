package com.yunbiao.cccm.net2;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.cccm.net2.common.HeartBeatClient;
import com.yunbiao.cccm.net2.common.ResourceConst;
import com.yunbiao.cccm.net2.model.VideoDataModel;
import com.yunbiao.cccm.net2.db.ConfigResponse;
import com.yunbiao.cccm.net2.db.Daily;
import com.yunbiao.cccm.net2.db.DaoManager;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.db.TimeSlot;
import com.yunbiao.cccm.net2.utils.DateUtil;
import com.yunbiao.cccm.net2.utils.NetUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Response;

/**
 * Created by Administrator on 2019/11/18.
 */

public class DataLoader {
    private Date todayDate;
    private Date tommDate;
    private int REQ_DAY_NUM = 7;
    private final long dayMilliSec = 24 * 60 * 60 * 1000;
    private List<String> reqDatelist = new ArrayList<>();

    private DataListener dataListener;

    private static final String TAG = "DataLoader";

    private static DataLoader request = new DataLoader();
    private final ExecutorService executorService;
    private String today_str;
    private static boolean isTodayLoadComplete = false;

    public static DataLoader getInstance() {
        return request;
    }

    public static boolean isCurrDataComplete(){
        return isTodayLoadComplete;
    }

    private DataLoader() {
        executorService = Executors.newSingleThreadExecutor();
        todayDate = DateUtil.getTodayDate();
        today_str = DateUtil.getToday_str();
        tommDate = DateUtil.getTommDate(DateUtil.getTodayStr());

        for (int i = 0; i < REQ_DAY_NUM; i++) {
            String date = DateUtil.yyyy_MM_dd_Format(new Date(todayDate.getTime() + (dayMilliSec * i)));
            reqDatelist.add(date);
        }
    }

    public void get() {
        get(dataListener);
    }

    public void get(DataListener listener) {
        dataListener = listener;
        startLoading();
    }

    //删除当前请求日期之前留存的数据
    private void deleteOldDataByDate(String date){
        d("删除已存的数据：" + date);
        Daily daily = DaoManager.get().queryByDate(date);
        if(daily == null){
            d("无可删除");
            return;
        }
        for (TimeSlot timeSlot : daily.getTimeSlots()) {
            for (ItemBlock itemBlock : timeSlot.getItemBlocks()) {
                DaoManager.get().delete(itemBlock);
            }
            DaoManager.get().delete(timeSlot);
        }
        DaoManager.get().delete(daily);
    }

    private void startLoading() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                startLoad();

                for (String date : reqDatelist) {
                    startSingleLoading(date);

                    Map<String, String> map = new HashMap<>();
                    map.put("deviceNo", HeartBeatClient.getDeviceNo());
                    map.put("playDate", date);
                    //开始请求，5次重试
                    Response response = request(ResourceConst.REMOTE_RES.GET_RESOURCE, map, 5);

                    if (response == null) {
                        loadFailed(date);
                        d("三次重试完毕：结束");
                        continue;
                    }

                    String responseString = null;
                    try {
                        responseString = response.body().string();
                    } catch (IOException e) {
                        d("请求config，解析响应异常：" + e.getMessage());
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(responseString)) {
                        resolveFailed(date);
                        d("请求config，响应为空");
                        continue;
                    }

                    d(responseString);

                    ConfigResponse configResponse = new Gson().fromJson(responseString, ConfigResponse.class);

                    if (configResponse.getResult() == -1) {
                        loadSingleComplete(date,false);
                        continue;
                    }

                    //ftp下载地址
                    String ftpServiceUrl = configResponse.getDataJson().getFtpServiceUrl();

                    String configUrl = configResponse.getDataJson().getConfigUrl();
                    Response configResp = get(configUrl, 5);
                    if (configResp == null) {
                        d("加载config失败");
                        loadConfigFailed(date);
                        continue;
                    }

                    String configString = null;
                    try {
                        configString = configResp.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (TextUtils.isEmpty(configString)) {
                        d("下载config，响应为空");
                        resolveConfigFailed(date);
                        continue;
                    }

                    VideoDataModel videoDataModel = null;
                    try {
                        videoDataModel = new XMLParse().parseVideoModel(configString);
                    } catch (Exception e) {
                        resolveConfigFailed(date, e);
                        e.printStackTrace();
                        continue;
                    }

                    if (videoDataModel == null) {
                        resolveConfigFailed(date);
                        continue;
                    }

                    //请求并解析成功后删除已存的当日数据
                    deleteOldDataByDate(date);

                    isTodayLoadComplete = true;

                    List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
                    VideoDataModel.Config config = videoDataModel.getConfig();
                    String playurl = config.getPlayurl();

                    List<VideoDataModel.Play.Rule> currRules = null;
                    String currDate = null;

                    for (VideoDataModel.Play play : playlist) {
                        String playday = play.getPlayday();
                        String s = DateUtil.yyyy_MM_dd_Format(playday);
                        if (TextUtils.equals(date, s)) {
                            currDate = s;
                            currRules = play.getRules();
                        }
                    }

                    boolean hasProgram = currRules != null && currRules.size() > 0;

                    //判断有节目时才处理
                    if(hasProgram){
                        //设置日期
                        Daily daily = new Daily();
                        daily.setDate(currDate);
                        DaoManager.get().addOrUpdate(daily);

                        for (VideoDataModel.Play.Rule currRule : currRules) {
                            TimeSlot timeSlot = new TimeSlot();
                            timeSlot.setParentDate(daily.getDate());
                            timeSlot.setDateTime(daily.getDate() + currRule.getDate());

                            String[] split1 = currRule.getDate().split("-");
                            timeSlot.setStart(split1[0]);
                            timeSlot.setEnd(split1[1]);

                            DaoManager.get().addOrUpdate(timeSlot);

                            String[] split = currRule.getRes().split(",");
                            for (String resName : split) {
                                resName = resName.trim().replaceAll("%20", "").replaceAll("\n", "");
                                if (TextUtils.isEmpty(resName)) {
                                    continue;
                                }

                                ItemBlock itemBlock = new ItemBlock();
                                itemBlock.setDateTime(timeSlot.getDateTime());
                                itemBlock.setUrl(resolveUrl(ftpServiceUrl, playurl, resName));
                                itemBlock.setName(resName);
                                itemBlock.setUnique(timeSlot.getDateTime() + resName);
                                DaoManager.get().addOrUpdate(itemBlock);
                            }

                        }
                    }

                    loadSingleComplete(date,hasProgram);
                }

                loadFinished();
            }
        });
    }

    private Response request(String url, Map map, int retryNum) {
        d("地址：" + url);
        d("参数：" + map.toString());

        Response response = null;
        for (int i = 0; i < retryNum; i++) {
            d("请求第" + i + "次");
            Response resp = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
            if (resp != null && resp.code() == 200) {
                response = resp;
                break;
            }
        }
        return response;
    }

    private Response get(String url, int retryNum) {
        d("地址：" + url);
        Response response = null;
        for (int i = 0; i < retryNum; i++) {
            d("请求第" + i + "次");
            response = NetUtil.getInstance().getSync(url);
            if (response != null) {
                break;
            }
        }
        return response;
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    private void d(String log) {
        Log.d(TAG, log);
//        d2Log(log);
    }

    private void d2Log(String log) {
        LogHandler.log2Console(log);
    }

    public static String resolveUrl(String ftpUrl, String playUrl, String resName) {
        return ftpUrl + playUrl + "/" + resName;
    }

    private void loadFailToRetry(String date, int time) {
        if (dataListener != null) {
            dataListener.loadDataFailToRetry(date, time);
        }
    }

    private void loadFailed(String date) {
        if (dataListener != null) {
            dataListener.loadDataFiled(date);
        }
    }

    private void resolveFailed(String date) {
        if (dataListener != null) {
            dataListener.resolveDataFailed(date);
        }
    }

    private void loadConfigFailToRetry(String date, int time) {
        if (dataListener != null) {
            dataListener.loadConfigFailToRetry(date, time);
        }
    }

    private void loadConfigFailed(String date) {
        if (dataListener != null) {
            dataListener.loadConfigFailed(date);
        }
    }

    private void resolveConfigFailed(String date) {
        if (dataListener != null) {
            dataListener.resolveConfigFailed(date);
        }
    }

    private void resolveConfigFailed(String date, Exception e) {
        if (dataListener != null) {
            dataListener.resolveConfigFailed(date, e);
        }
    }

    private void loadSingleComplete(String date, boolean hasProgram) {
        if (dataListener != null) {
            dataListener.loadSingleComplete(date, TextUtils.equals(today_str, date),hasProgram);
        }
    }

    private void loadFinished() {
        if (dataListener != null) {
            dataListener.loadFinished();
        }
    }

    private void startLoad() {
        if (dataListener != null) {
            dataListener.startLoad();
        }
    }

    private void startSingleLoading(String date) {
        if (dataListener != null) {
            dataListener.startSingleLoading(date);
        }
    }

    public static class AutoLogListener implements DataListener {

        private final String today_str;

        public AutoLogListener() {
            today_str = DateUtil.getToday_str();
        }

        public void failed(String date, boolean isToday , String type) {

        }

        @Override
        public void startLoad() {
        }

        @Override
        public void startSingleLoading(String date) {
            ConsoleDialog.addTextLog("开始加载：" + date);
        }

        /**
         * 加载失败去重试
         *
         * @param date 当前加载的日期
         * @param time 失败的次数
         */
        public void loadDataFailToRetry(String date, int time) {
            ConsoleDialog.addTextLog("加载数据失败"/*： （日期：" + date*/ + "，重试第 " + time + "次");
        }

        /***
         * 加载失败
         * @param date 当前日期
         */
        public void loadDataFiled(String date) {
            ConsoleDialog.addTextLog("加载数据失败"/*： （日期：" + date*/);
            failed(date,TextUtils.equals(today_str, date),"loadDataFiled");
        }

        /**
         * 解析失败
         *
         * @param date
         */
        public void resolveDataFailed(String date) {
            ConsoleDialog.addTextLog("解析数据失败"/*： （日期：" + date*/);
            failed(date,TextUtils.equals(today_str, date),"resolveDataFailed");
        }

        /***
         * 下载config失败去重试
         * @param date
         * @param time
         */
        public void loadConfigFailToRetry(String date, int time) {
            ConsoleDialog.addTextLog("加载Config失败"/*： （日期：" + date */ + "，重试第 " + time + "次");
        }

        /***
         * 下载config失败
         * @param date
         */
        public void loadConfigFailed(String date) {
            ConsoleDialog.addTextLog("加载Config失败"/*： （日期：" + date*/);
            failed(date,TextUtils.equals(today_str, date),"loadConfigFailed");
        }

        /***
         * 解析config失败
         * @param date
         */
        public void resolveConfigFailed(String date) {
            ConsoleDialog.addTextLog("解析Config失败"/*： （日期：" + date*/);
            failed(date,TextUtils.equals(today_str, date),"resolveConfigFailed");
        }

        /***
         * 解析config失败
         * @param date
         */
        public void resolveConfigFailed(String date, Exception e) {
            ConsoleDialog.addTextLog("解析Config失败"/*： （日期：" + date + "，"*/ + (e == null ? "NULL" : e.getMessage()));
            failed(date,TextUtils.equals(today_str, date),"resolveConfigFailed");
        }

        @Override
        public void loadSingleComplete(String date, boolean isToday, boolean hasProgram) {
            if(hasProgram){
                ConsoleDialog.addTextLog("加载成功"/*： （日期：" + date + "，" + isToday*/);
            } else {
                ConsoleDialog.addTextLog("暂无节目安排"/*： （日期：" + date + "，" + isToday*/);
            }
        }

        @Override
        public void loadFinished() {
            ConsoleDialog.addTextLog("加载结束-------");
        }
    }

    public interface DataListener {
        /***
         * 整体流程开始
         */
        void startLoad();

        /***
         * 单个开始
         * @param date
         */
        void startSingleLoading(String date);

        /**
         * 加载失败去重试
         *
         * @param date 当前加载的日期
         * @param time 失败的次数
         */
        void loadDataFailToRetry(String date, int time);

        /***
         * 加载失败
         * @param date 当前日期
         */
        void loadDataFiled(String date);

        /**
         * 解析失败
         *
         * @param date
         */
        void resolveDataFailed(String date);

        /***
         * 下载config失败去重试
         * @param date
         * @param time
         */
        void loadConfigFailToRetry(String date, int time);

        /***
         * 下载config失败
         * @param date
         */
        void loadConfigFailed(String date);

        /***
         * 解析config失败
         * @param date
         */
        void resolveConfigFailed(String date);

        /***
         * 解析config失败
         * @param date
         */
        void resolveConfigFailed(String date, Exception e);

        /***
         * 当前日期无节目
         * @param date
         */
//        void noProgram(String date, boolean isToday);

        /***
         * 单个加载完成
         * @param date
         * @param hasProgram
         */
        void loadSingleComplete(String date, boolean isToday, boolean hasProgram);

        /***
         * 全部加载完成
         */
        void loadFinished();
    }

}
