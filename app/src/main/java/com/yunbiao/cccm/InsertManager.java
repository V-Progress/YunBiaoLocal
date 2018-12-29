package com.yunbiao.cccm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.yunbiao.cccm.act.MainController;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.MediaPlayer;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertManager {
    private static InsertManager insertManager;
    private static Activity mActivity;
    private VideoView videoView;

    private SimpleDateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    public static final String KEY_INSERT_TXT = "insertTxt";
    public static final String KEY_INSERT_VIDEO = "insertVideo";

    private final int TYPE_VIDEO = 1;
    private final int TYPE_LIVE = 2;
    private final int TYPE_INPUT = 3;
    private Integer isCycle;
    private List<String> playList = new ArrayList<>();
    private Map<String, Timer> timerMap = new HashMap<>();
    int playIndex = 0;
    private MyScrollTextView scrollText;
    private InsertTextModel insertTxtModel;
    private final String CLEAR_TXT = "2";
    private InsertVideoModel insertVideoModel;

    public static InsertManager getInstance(Activity activity) {
        if (insertManager == null) {
            insertManager = new InsertManager();
        }
        mActivity = activity;
        return insertManager;
    }

    public void createVideo() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                videoView = new VideoView(mActivity);
                videoView.setLayoutParams(layoutParams);
                videoView.setOnCompletionListener(onCompletionListener);
            }
        });
    }

    android.media.MediaPlayer.OnCompletionListener onCompletionListener = new android.media.MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(android.media.MediaPlayer mp) {
            LogUtil.E("当前播放标签" + playIndex);
            playIndex++;
            LogUtil.E("当前播放标签" + playIndex);
            if (playIndex >= playList.size()) {
                LogUtil.E("playIndex >= playList.size()-1");
                playIndex = 0;
                if (isCycle <= -1) {
                    LogUtil.E("isCycle <= -1");
                    MainController.getInstance().removeView(videoView);
                    return;
                }
                LogUtil.E("playIndex = 0");
            }
            try {
                videoView.stopPlayback();
                videoView.setVideoPath(playList.get(playIndex));
                videoView.start();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /***
     * 显示滚动文字
     * @param insertTextModel
     */
    private void createScrTXT(final InsertTextModel insertTextModel) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    public void insertTXT(final InsertTextModel itm){
        if (itm == null) {
            return;
        }

        if(scrollText != null){
            ThreadUtil.getInstance().runInUIThread(new Runnable() {
                @Override
                public void run() {
                    APP.getMainActivity().removeView(scrollText);
                }
            });
        }

        if (TextUtils.equals(CLEAR_TXT, itm.getPlayType())) {
            insertTxtModel = null;
            return;
        }

        createScrTXT(itm);

        insertTxtModel = itm;
        //取出内部的数据
        String playDate1 = itm.getContent().getPlayDate();
        String playCurTime1 = itm.getContent().getPlayCurTime();

        try {
            final Date[] dates1 = resolveTime(playDate1, playCurTime1);
            if (dates1 != null && dates1.length > 0) {
                Timer startTimer = timerMap.get("txt_start");
                Timer endTimer = timerMap.get("txt_end");

                if (startTimer != null) {
                    startTimer.cancel();
                }

                if (endTimer != null) {
                    endTimer.cancel();
                }

                startTimer = new Timer();
                endTimer = new Timer();
                startTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                APP.getMainActivity().addView(scrollText);
                            }
                        });

//                        MainController.getInstance().addView(scrollText);
                    }
                }, dates1[0]);

                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                APP.getMainActivity().removeView(scrollText);
                            }
                        });

//                        MainController.getInstance().removeView(scrollText);
                    }
                }, dates1[1]);

                timerMap.put("txt_start", startTimer);
                timerMap.put("txt_end", endTimer);
            } else {
                LogUtil.E("播放时间已过！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertVideo(InsertVideoModel ivm) {
        if (ivm == null) {
            return;
        }
        playList.clear();

        insertVideoModel = ivm;
        try {
            String ftpUrl;
            InsertVideoModel.InsertData dateJson = ivm.getDateJson();
            ftpUrl = dateJson.getHsdresourceUrl();

            List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();

            for (InsertVideoModel.Data data : insertArray) {
                Date[] dateArray = resolve(data.getStartTime(), data.getEndTime());
                Timer startTimer;
                Timer endTimer;
                switch (data.getPalyType()) {
                    case TYPE_VIDEO:
                        List<String> urlList = new ArrayList<>();
                        String content = data.getContent();
                        String[] playArray = content.split(",");
                        isCycle = data.getIsCycle();
                        for (String s : playArray) {
                            String[] split = s.split("/");
                            playList.add(ResourceConst.LOCAL_RES.RES_SAVE_PATH + "/" + split[split.length - 1]);
                            urlList.add(ftpUrl + s);
                        }

                        download(urlList, dateArray);
                        break;
                    case TYPE_LIVE://直播类
                        if(videoView != null){
                            APP.getMainActivity().removeView(videoView);
                        }

                        createVideo();

                        final String liveUrl = data.getContent();
                        startTimer = timerMap.get("live_start");
                        endTimer = timerMap.get("live_end");
                        if (startTimer != null) {
                            startTimer.cancel();
                        }

                        if (endTimer != null) {
                            endTimer.cancel();
                        }

                        startTimer = new Timer();
                        endTimer = new Timer();

                        startTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                APP.getMainActivity().addView(videoView);
                            }
                        }, dateArray[0]);

                        endTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                APP.getMainActivity().removeView(videoView);
                            }
                        }, dateArray[1]);

                        timerMap.put("live_start", startTimer);
                        timerMap.put("live_end", endTimer);
                        break;
                    case TYPE_INPUT:
                        startTimer = timerMap.get("hdmi_start");
                        endTimer = timerMap.get("hdmi_end");
                        if (startTimer != null) {
                            startTimer.cancel();
                        }

                        if (endTimer != null) {
                            endTimer.cancel();
                        }

                        startTimer = new Timer();
                        endTimer = new Timer();

                        startTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
//                                checkHDMI(true);
                            }
                        }, dateArray[0]);

                        endTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
//                                checkHDMI(false);
                            }
                        }, dateArray[1]);

                        timerMap.put("hdmi_start", startTimer);
                        timerMap.put("hdmi_end", endTimer);
                        break;
                }
            }

            for (Map.Entry<String, Timer> stringTimerEntry : timerMap.entrySet()) {
                LogUtil.E(stringTimerEntry.getKey() + "---" + stringTimerEntry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

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

    private Date[] resolve(String startStr, String endStr) throws ParseException {
        String endTime = correctTime(endStr);
        String startTime = correctTime(startStr);
        //获取当年月日
        Date currDateTime = new Date(System.currentTimeMillis());
        String currDateStr = yyyyMMdd.format(currDateTime);
        //转换成date格式
        Date start = yyyyMMddHH_mm.parse(currDateStr + startTime);
        Date end = yyyyMMddHH_mm.parse(currDateStr + endTime);

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

    /*=======下载监听================================================================*/
    public void download(List<String> urlList, final Date[] dateArray) {
        ResourceManager.getInstance().download(urlList, new ResourceManager.FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                LogUtil.E("共要下载" + totalNum);
            }

            @Override
            public void onFinish() {

                if(videoView != null){
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            APP.getMainActivity().removeView(videoView);
                        }
                    });
                }

                createVideo();

                LogUtil.E("下载结束");
                Timer startTimer = timerMap.get("video_start");
                Timer endTimer = timerMap.get("video_end");
                if (startTimer != null) {
                    startTimer.cancel();
                }

                if (endTimer != null) {
                    endTimer.cancel();
                }

                startTimer = new Timer();
                endTimer = new Timer();

                startTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.setVideoPath(playList.get(playIndex));
                                videoView.start();
                                APP.getMainActivity().pause();
                                APP.getMainActivity().addView(videoView);
                            }
                        });
                    }
                }, dateArray[0]);

                endTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                videoView.stopPlayback();
                                APP.getMainActivity().resume();
                                APP.getMainActivity().removeView(videoView);
                            }
                        });
                    }
                }, dateArray[1]);

                LogUtil.E("视频是否显示：" + videoView.isShown());
                LogUtil.E("视频是否播放：" + videoView.isPlaying());

                timerMap.put("video_start", startTimer);
                timerMap.put("video_end", endTimer);
            }

        });
    }

    public void onResume(){
        if(videoView != null){
            videoView.setVisibility(View.VISIBLE);
            videoView.resume();
            videoView.start();
        }
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
        String sTime = currDateStr + correctTime(times[0]);
        String eTime = currDateStr + correctTime(times[1]);
        LogUtil.E("开始时间-----" + sTime);
        LogUtil.E("结束时间-----" + eTime);
        //转换成date格式
        final Date beginTime = yyyyMMddHH_mm.parse(sTime);
        final Date endTime = yyyyMMddHH_mm.parse(eTime);

        LogUtil.E("开始毫秒：" + beginTime.getTime());
        LogUtil.E("结束毫秒：" + endTime.getTime());
        if (endTime.getTime() < yyyyMMddHH_mm.parse(yyyyMMddHH_mm.format(currDateTime)).getTime()) {
            return null;
        }

        return new Date[]{beginTime, endTime};
    }
}
