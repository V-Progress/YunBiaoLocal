package com.yunbiao.cccm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.devicectrl.actions.XBHActions;
import com.yunbiao.cccm.download.ResourceManager;
import com.yunbiao.cccm.utils.CommonUtils;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.view.MyScrollTextView;
import com.yunbiao.cccm.view.TipToast;
import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertManager {
    private static InsertManager insertManager;
    private static Activity mActivity;

    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    private final int TYPE_VIDEO = 1;
    private final int TYPE_LIVE = 2;
    private final int TYPE_INPUT = 3;
    private List<String> playList = new ArrayList<>();
    private MyScrollTextView scrollText;
    private final String CLEAR_TXT = "2"; //清除字幕
    private final Date todayDate;

    List<Timer> timerList = new ArrayList<>();
    List<Timer> txtTimerList = new ArrayList<>();

    public static InsertManager getInstance(Activity activity) {
        mActivity = activity;
        if (insertManager == null) {
            insertManager = new InsertManager();
        }
        return insertManager;
    }

    public InsertManager() {
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());
        initTXT();
    }

    public void initTXT() {
        InsertTextModel txtAds = CacheManager.FILE.getTXTAds();
        if (txtAds != null) {
            insertTXT(txtAds);
        }
    }

    /***
     * 插播字幕
     * ========================================================================================
     * @param itm
     */
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

        CacheManager.FILE.putTXTAds(itm);

        //取出内部的数据
        String playDate = itm.getContent().getPlayDate();
        String playCurTime = itm.getContent().getPlayCurTime();

        try {
            final Date[] dates = resolveTime(playDate, playCurTime);
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
        } catch (Exception e) {
            e.printStackTrace();
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

        scrollText = new MyScrollTextView(mActivity);
        scrollText.setLayoutParams(layoutParams);
        scrollText.setTextSize(fontSize);//字号
        scrollText.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色
        scrollText.setScrollSpeed(textDetail.getPlaySpeed());
        scrollText.setDirection(Integer.valueOf(textDetail.getPlayType()));
        scrollText.setBackColor(Color.parseColor(textDetail.getBackground()));//背景色
        scrollText.setText(text);//内容

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            scrollText.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            scrollText.setDirection(0);
        }
    }

    /***
     * 插播类
     * ==================================================================================
     * @param ivm
     */
    public void insertPlay(InsertVideoModel ivm) throws ParseException {
        if (ivm == null) {
            return;
        }

        playList.clear();
        clearTimer();

        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();
        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();

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
            if (today.equals(dateArray[1]) || today.after(dateArray[1])) {
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
                    download(isCycle,urlList, playList,dateArray);
                    break;
                case TYPE_LIVE://直播类
                    String liveUrl = data.getContent();
                    handleLive(isCycle,liveUrl, dateArray[0], dateArray[1]);
                    break;
                case TYPE_INPUT:
                    handleInput(dateArray[0], dateArray[1]);
                    break;
            }
        }
    }

    /*=======视频处理流程================================================================*/
    public void download(final boolean isCycle, List<String> urlList, final List<String> playList, final Date[] dateArray) {
        ResourceManager.getInstance().download(urlList, new ResourceManager.FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
            }

            @Override
            public void onFinish() {
                handleVideo(isCycle,playList, dateArray[0], dateArray[1]);
            }

        });
    }

    //处理视频
    private void handleVideo(final boolean isCycle, final List<String> playList, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().startInsert(isCycle,playList);
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
                List<String> list = new ArrayList<String>();
                list.add(liveUrl);
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
                LogUtil.E("切换到HDMI信号");
//                checkHDMI(true);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E("结束HDMI信号");
//                checkHDMI(true);
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
            TipToast.showLongToast(APP.getContext(), "暂不支持该功能");
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
    private Date[] resolve(String startStr, String endStr) throws ParseException {
        String endTime = correctTime(endStr)+":00";
        String startTime = correctTime(startStr)+":00";

        String currDateStr = yyyyMMdd.format(todayDate);
        //转换成date格式
        Date start = yyyyMMddHH_mm_ss.parse(currDateStr + startTime);
        Date end = yyyyMMddHH_mm_ss.parse(currDateStr + endTime);

        LogUtil.E(currDateStr + startTime);
        LogUtil.E(currDateStr + endTime);

        return new Date[]{start, end};
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
    private Date[] resolveTime(String playDate, String playTime) throws Exception {
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
        String sTime = currDateStr + correctTime(times[0])+":00";
        String eTime = currDateStr + correctTime(times[1])+":00";
        //转换成date格式
        final Date beginTime = yyyyMMddHH_mm_ss.parse(sTime);
        final Date endTime = yyyyMMddHH_mm_ss.parse(eTime);

        if (endTime.getTime() < yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(currDateTime)).getTime()) {
            return null;
        }

        return new Date[]{beginTime, endTime};
    }
}
