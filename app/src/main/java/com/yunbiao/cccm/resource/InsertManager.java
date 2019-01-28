package com.yunbiao.cccm.resource;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.control.actions.XBHActions;
import com.yunbiao.cccm.utils.CommonUtils;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.netcore.NetClient;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.view.MyScrollTextView;
import com.yunbiao.cccm.view.TipToast;
import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertManager implements TextToSpeech.OnInitListener {
    private static InsertManager insertManager;
    private static Activity mActivity;
    private Object downloadTag = getClass();
    private String TAG = this.getClass().getSimpleName();

    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    private final int TYPE_VIDEO = 1;
    private final int TYPE_LIVE = 2;
    private final int TYPE_INPUT = 3;
    private List<String> playList = new ArrayList<>();
    private MyScrollTextView scrollText;
    private final String CLEAR_TXT = "2"; //清除字幕
    private final Date todayDate;

    private TextToSpeech tts;

    List<Timer> timerList = new ArrayList<>();
    List<Timer> txtTimerList = new ArrayList<>();
    private BPDownloadUtil bpDownloadUtil;

    public static InsertManager getInstance(Activity activity) {
        mActivity = activity;
        if (insertManager == null) {
            insertManager = new InsertManager();
        }
        return insertManager;
    }

    public InsertManager() {
        tts = new TextToSpeech(mActivity, this);
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());
        initTXT();
    }

    /***
     * 初始化插播数据
     */
    public void initInsertData() {

        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtil.D(TAG, "开始请求插播资源");
                    String url = ResourceConst.REMOTE_RES.INSERT_CONTENT;
                    Map<String, String> params = new HashMap<>();
                    params.put("deviceNo", HeartBeatClient.getDeviceNo());
                    Response response = NetClient.getInstance().postSync(url, params);
                    if (response == null) {
                        throw new Exception("request Insert Data Error");
                    }
                    String jsonStr = response.body().string();
                    if (TextUtils.isEmpty(jsonStr)) {
                        throw new Exception("Json String is NULL : " + url);
                    }
                    LogUtil.D(TAG, "插播资源：" + jsonStr);
                    InsertVideoModel insertVideo = new Gson().fromJson(jsonStr, InsertVideoModel.class);
                    if (insertVideo == null) {
                        throw new Exception("Resolve ConfigResponse failed");
                    }

                    if (insertVideo.getResult() != 1) {
                        throw new Exception(insertVideo.getMessage());
                    }

                    insertPlay(insertVideo);
                } catch (Exception e) {
                    LogUtil.E(TAG, "处理插播资源出现异常：" + e.getMessage());
                }
            }
        });
    }

    /***
     * 插播字幕
     * ========================================================================================
     */
    public void initTXT() {
        InsertTextModel txtAds = CacheManager.FILE.getTXTAds();
        if (txtAds != null) {
            insertTXT(txtAds);
        }
    }

    public void insertTXT(final InsertTextModel itm) {
        if (itm == null) {
            return;
        }

        for (Timer timer : txtTimerList) {
            timer.cancel();
        }
        txtTimerList.clear();

        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                APP.getMainActivity().removeView(scrollText);
            }
        });

        if (TextUtils.equals(CLEAR_TXT, itm.getPlayType())) {
            CacheManager.FILE.putTXTAds(null);
            return;
        }

        MainController.getInstance().setHasInsert(true);
        CacheManager.FILE.putTXTAds(itm);

        //取出内部的数据
        String playDate = itm.getContent().getPlayDate();
        String playCurTime = itm.getContent().getPlayCurTime();
        String playTime = itm.getContent().getPlayTime();

        Date[] dates;
        //判断是播放时长还是播放时间段
        if (!TextUtils.isEmpty(playTime) && !TextUtils.equals("0", playTime)) {
            dates = resolveTimeLong(playDate, playTime);
        } else {
            dates = resolveTime(playDate, playCurTime);
        }

        if (dates == null) {
            return;
        }

        if (dates != null && dates.length > 0) {
            Timer startTimer = new Timer();
            Timer endTimer = new Timer();

            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            setTXT(itm);
                            APP.getMainActivity().addView(scrollText);
                        }
                    });
                }
            }, dates[0]);

            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            APP.getMainActivity().removeView(scrollText);
                        }
                    });
                }
            }, dates[1]);

            txtTimerList.add(startTimer);
            txtTimerList.add(endTimer);
        }
    }


    /***
     * 显示滚动文字
     * @param insertTextModel
     */
    private void setTXT(final InsertTextModel insertTextModel) {
        InsertTextModel.Content textDetail = insertTextModel.getContent();
        Integer fontSize = textDetail.getFontSize();
        String text = insertTextModel.getText();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (fontSize * 2.5));
        if (TextUtils.equals("0", textDetail.getLocation())) {//顶部
            layoutParams.gravity = Gravity.TOP;
        } else {
            layoutParams.gravity = Gravity.BOTTOM;
        }
        int backColor = Color.parseColor(textDetail.getBackground());
        scrollText = new MyScrollTextView(mActivity);

        /*String transparent = textDetail.getTransparent();
        if (TextUtils.equals("1", transparent)) {
            String background = textDetail.getBackground();
            String substring = background.substring(1);
            substring = "#77" + substring;
            scrollText.setAlpha(0.5f);

            LogUtil.E("当前背景色："+substring);
            backColor = Color.parseColor(substring);
            scrollText.setBackgroundColor(backColor);
        }*/

        scrollText.setLayoutParams(layoutParams);
        scrollText.setTextSize(fontSize);//字号
        scrollText.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色
        scrollText.setScrollSpeed(textDetail.getPlaySpeed());
        scrollText.setDirection(Integer.valueOf(textDetail.getPlayType()));
        scrollText.setBackColor(backColor);//背景色
        scrollText.setText(text);//内容

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            scrollText.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            scrollText.setDirection(0);
        }

        if (isSupportChinese) {
            if (TextUtils.equals("-1", textDetail.getSpeechCount())) {
                return;
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(mActivity, "暂不支持语音报读", Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * 插播类
     * ==================================================================================
     * @param ivm
     */
    public void insertPlay(InsertVideoModel ivm) {
        if (ivm == null) {
            return;
        }

        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();
        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();

        playList.clear();
        clearTimer();
        MainController.getInstance().stopInsert();

        if (insertArray == null || insertArray.size() <= 0) {
            MainController.getInstance().setHasInsert(false);
            return;
        }

        Date today = null;
        try {
            today = yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(todayDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (InsertVideoModel.Data data : insertArray) {
            if (data == null) {
                continue;
            }

            Integer playType = data.getPlayType();
            if (playType == null) {
                continue;
            }

            Date[] dateArray = resolve(data.getStartTime(), data.getEndTime());
            if (dateArray == null) {
                continue;
            }

            if (today.after(dateArray[1])) {
                continue;
            }

            //是否轮播
            boolean isCycle = data.getIsCycle() == 1;
            switch (playType) {
                case TYPE_VIDEO:
                    String content = data.getContent();
                    String[] playArray = content.split(",");

                    List<String> urlList = new ArrayList<>();
                    List<String> playList = new ArrayList<>();
                    //拼装播放列表
                    for (String s : playArray) {
                        String[] split = s.split("/");
                        playList.add(ResourceConst.LOCAL_RES.RES_SAVE_PATH + "/" + split[split.length - 1]);
                        urlList.add(ftpUrl + s);
                    }
                    download(isCycle, urlList, playList, dateArray);
                    break;
                case TYPE_LIVE://直播类
                    String liveUrl = data.getContent();
                    handleLive(isCycle, liveUrl, dateArray[0], dateArray[1]);
                    break;
                case TYPE_INPUT:
                    handleInput(dateArray[0], dateArray[1]);
                    break;
            }
        }
    }

    /*=======视频处理流程================================================================*/
    public void download(final boolean isCycle, final List<String> urlList, final List<String> playList, final Date[] dateArray) {
        if (bpDownloadUtil != null) {
            bpDownloadUtil.cancel();
        }
        bpDownloadUtil = new BPDownloadUtil(getClass(),new FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                MainController.getInstance().openLoading("插播资源下载");
            }

            @Override
            public void onStart(int currNum) {
                LogUtil.D(TAG, "开始下载");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "进度：" + progress);
            }

            @Override
            public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
                LogUtil.D(TAG, "下载出错：" + e.getMessage());
            }

            @Override
            public void onFinish() {
                MainController.getInstance().closeLoading();

                // TODO: 2019/1/9 关注此处可能出现的问题
                for (int i = 0; i < playList.size(); i++) {
                    File file = new File(playList.get(i));
                    if (!file.exists()) {
                        playList.remove(i);
                    }
                }

                if (playList.size() <= 0) {
                    return;
                }
                handleVideo(isCycle, playList, dateArray[0], dateArray[1]);
            }

        });
        bpDownloadUtil.breakPointDownload(urlList);
    }

    //处理视频
    private void handleVideo(final boolean isCycle, final List<String> playList, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, playList);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }

    //处理直播数据
    private void handleLive(final boolean isCycle, final String liveUrl, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add(liveUrl);
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, list);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }

    //处理插播信号源流程
    private void handleInput(Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E(TAG, "切换到HDMI信号");
                MainController.getInstance().setHasInsert(true);
                checkHDMI(true);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E(TAG, "结束HDMI信号");
                checkHDMI(false);
            }
        });
    }

    /***
     * 切换HDMI信号 todo 待测试
     * @param isHdmi
     */
    private void checkHDMI(boolean isHdmi) {
        Integer broadType = CommonUtils.getBroadType();
        if (broadType == 4) {//判断是不是小百合
            Intent intent = new Intent();
            intent.setAction(isHdmi ? XBHActions.CHANGE_TO_HDMI : XBHActions.CHANGE_TO_VGA);
            APP.getContext().sendBroadcast(intent);
        } else {
            TipToast.showLongToast(APP.getContext(), "本设备不支持该功能");
        }
    }

    /***定时执行开关任务
     * ==============================================================================
     */
    private void timeExecute(Date startTime, final TimerTask startTask, Date endTime, final TimerTask endTask) {
        Timer startTimer = new Timer();
        Timer endTimer = new Timer();

        startTimer.schedule(startTask, startTime);
        endTimer.schedule(endTask, endTime);

        timerList.add(startTimer);
        timerList.add(endTimer);
    }

    public void clearTimer() {
        if (timerList.size() > 0) {
            for (Timer timer : timerList) {
                timer.cancel();
            }
            timerList.clear();
        }
    }

    //解析播放时间，没有date的情况下默认为当天
    private Date[] resolve(String startStr, String endStr) {
        try {
            String endTime = correctTime(endStr) + ":00";
            String startTime = correctTime(startStr) + ":00";

            String currDateStr = yyyyMMdd.format(todayDate);
            //转换成date格式
            Date start = yyyyMMddHH_mm_ss.parse(currDateStr + startTime);
            Date end = yyyyMMddHH_mm_ss.parse(currDateStr + endTime);

            LogUtil.D(TAG, currDateStr + startTime);
            LogUtil.D(TAG, currDateStr + endTime);

            return new Date[]{start, end};
        } catch (Exception e) {
            LogUtil.E(TAG, "解析插播时间出错：" + e.getMessage());
            return null;
        }
    }

    //修正播放时间
    private String correctTime(String time) {
        String[] beginTimes = time.split(":");
        for (int i = 0; i < beginTimes.length; i++) {
            String temp = beginTimes[i];
            if (temp.length() <= 1) {
                temp = "0" + temp;
            }
            beginTimes[i] = temp;
        }
        return beginTimes[0] + ":" + beginTimes[1];
    }

    //解析播放时间
    private Date[] resolveTime(String playDate, String playTime) {
        try {
            if (!playDate.contains("-")) {
                throw new Exception("playDate formal error!");
            }
            if (!playTime.contains("-")) {
                throw new Exception("playTime formal error!");
            }
            //切割开始结束时间
            String[] dates = playDate.split("-");
            String[] times = playTime.split("-");
            //获取当年月日
            Date currDateTime = new Date(System.currentTimeMillis());
            String currDateStr = yyyyMMdd.format(currDateTime);
            //转换成date格式
            Date currDate = yyyyMMdd.parse(currDateStr);
            Date beginDate = yyyyMMdd.parse(dates[0]);
            Date endDate = yyyyMMdd.parse(dates[1]);
            //对比
            if (currDate.getTime() < beginDate.getTime() || currDate.getTime() > endDate.getTime()) {
                return null;
            }

            //修正时间字符串
            String sTime = currDateStr + correctTime(times[0]) + ":00";
            String eTime = currDateStr + correctTime(times[1]) + ":00";
            //转换成date格式
            final Date beginTime = yyyyMMddHH_mm_ss.parse(sTime);
            final Date endTime = yyyyMMddHH_mm_ss.parse(eTime);

            if (endTime.getTime() < yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(currDateTime)).getTime()) {
                return null;
            }

            return new Date[]{beginTime, endTime};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //解析播放时长
    private Date[] resolveTimeLong(String playDate, String playTime) {
        try {
            //当前时间
            long currTime = System.currentTimeMillis();
            //开始时间
            Date start = new Date(currTime);
            String todayStr = yyyyMMdd.format(start);
            Date today = yyyyMMdd.parse(todayStr);

            String[] split = playDate.split("-");
            Date startDate = yyyyMMdd.parse(split[0]);
            Date endDate = yyyyMMdd.parse(split[1]);
            if (!(today.getTime() >= startDate.getTime() && today.getTime() <= endDate.getTime())) {
                LogUtil.D(TAG, "不在插播字幕时间内");
                return null;
            }

            Date end = new Date(currTime + (Integer.valueOf(playTime) * 1000));
            return new Date[]{start, end};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isSupportChinese = false;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if(tts != null){
                int result = tts.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isSupportChinese = false;
                    return;
                }
                isSupportChinese = true;
            }
        }
    }
}
