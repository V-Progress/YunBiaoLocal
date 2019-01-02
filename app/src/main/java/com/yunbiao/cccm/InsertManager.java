package com.yunbiao.cccm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

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
    private MediaPlayer mediaPlayer;
    private int mPlayType;
    private final Date todayDate;

    public static InsertManager getInstance(Activity activity) {
        mActivity = activity;
        if (insertManager == null) {
            insertManager = new InsertManager();
        }
        return insertManager;
    }

    public InsertManager() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoView = new VideoView(mActivity);
        videoView.setLayoutParams(layoutParams);
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.setOnPreparedListener(onPreparedListener);
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());

    }

    /***
     * 在返回界面的时候调用这个方法，如果还有需要播放的数据会自动进行播放
     */
    public void onResume() {
        if (videoView != null) {
            videoView.setVisibility(View.VISIBLE);
            videoView.resume();
            videoView.start();
        }
    }

    //准备好监听
    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer = mp;
        }
    };

    //视频播放完成的监听
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtil.E("当前播放标签" + playIndex);
            if (mPlayType == TYPE_LIVE) {
                return;
            }

            //如果index大于播放列表的size
            if (playIndex >= playList.size()) {
                playIndex = 0;//将playIndex置为0

                if (isCycle <= -1) {//如果是不重复
                    closeVideo();//关闭后充值
                    return;
                }
            }

            playIndex++;
            videoView.stopPlayback();
            videoView.setVideoPath(playList.get(playIndex));
            videoView.start();
        }
    };

    /***
     * 插播字幕
     * ========================================================================================
     * @param itm
     */
    public void insertTXT(final InsertTextModel itm) {
        if (itm == null) {
            return;
        }

        if (scrollText != null) {
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
        String playDate = itm.getContent().getPlayDate();
        String playCurTime = itm.getContent().getPlayCurTime();

        try {
            final Date[] dates = resolveTime(playDate, playCurTime);
            if (dates != null && dates.length > 0) {
                timeExecute("txt", dates[0], new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                APP.getMainActivity().addView(scrollText);
                            }
                        });
                    }
                }, dates[1], new TimerTask() {
                    @Override
                    public void run() {
                        ThreadUtil.getInstance().runInUIThread(new Runnable() {
                            @Override
                            public void run() {
                                APP.getMainActivity().removeView(scrollText);
                            }
                        });
                    }
                });
            } else {
                LogUtil.E("播放时间已过！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    /***
     * 插播视频
     * ==================================================================================
     * @param ivm
     */
    public void insertVideo(InsertVideoModel ivm) throws ParseException {
        if (ivm == null) {
            return;
        }

        playList.clear();
        insertVideoModel = ivm;
        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();
        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();

        Date today = null;
        try {
            today = yyyyMMddHH_mm.parse(yyyyMMddHH_mm.format(todayDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (InsertVideoModel.Data data : insertArray) {
            if (data == null) {
                continue;
            }
            LogUtil.E("插播数据：" + data.toString());

            Integer playType = data.getPlayType();
            if (playType == null) {
                continue;
            }

            Date[] dateArray = resolve(data.getStartTime(), data.getEndTime());
            if (today.after(dateArray[1])) {
                LogUtil.E("播放时间已过，不添加任务！");
                continue;
            }

            switch (playType) {
                case TYPE_VIDEO:
                    LogUtil.E("插播类型：视频");
                    String content = data.getContent();
                    String[] playArray = content.split(",");
                    isCycle = data.getIsCycle();

                    List<String> urlList = new ArrayList<>();
                    //拼装播放列表
                    for (String s : playArray) {
                        String[] split = s.split("/");
                        playList.add(ResourceConst.LOCAL_RES.RES_SAVE_PATH + "/" + split[split.length - 1]);
                        urlList.add(ftpUrl + s);
                    }
                    LogUtil.E("播放列表：" + playList.toString());
//                    handleVideo(dateArray[0], dateArray[1]); // TODO: 2018/12/30 测试
                    download(urlList, dateArray);
                    break;
                case TYPE_LIVE://直播类
                    LogUtil.E("插播类型：直播源");
                    String liveUrl = data.getContent();
                    handleLive(liveUrl, dateArray[0], dateArray[1]);
                    break;
                case TYPE_INPUT:
                    LogUtil.E("插播类型：输入流");
                    handleInput(dateArray[0], dateArray[1]);
                    break;
            }
        }
    }

    /*=======视频处理流程================================================================*/
    public void download(List<String> urlList, final Date[] dateArray) {
        ResourceManager.getInstance().download(urlList, new ResourceManager.FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                LogUtil.E("共要下载" + totalNum);
            }

            @Override
            public void onFinish() {
                handleVideo(dateArray[0], dateArray[1]);
            }

        });
    }

    //处理视频
    private void handleVideo(Date startTime, Date endTime) {
        timeExecute("video", startTime, new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.getInstance().runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayType = TYPE_VIDEO;
                        openVideo(playList.get(playIndex));
                    }
                });
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.getInstance().runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeVideo();
                    }
                });
            }
        });
    }

    //处理直播数据
    private void handleLive(final String liveUrl, Date startTime, Date endTime) {
        timeExecute("live", startTime, new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.getInstance().runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayType = TYPE_LIVE;
                        openVideo(liveUrl);
                    }
                });
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                ThreadUtil.getInstance().runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.E("结束直播源");
                        closeVideo();
                    }
                });
            }
        });
    }

    //处理插播信号源流程
    private void handleInput(Date startTime, Date endTime) {
        timeExecute("hdmi", startTime, new TimerTask() {
            @Override
            public void run() {
//                checkHDMI(true);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
//                checkHDMI(false);
            }
        });
    }

    //打开视频
    private void openVideo(String playPath) {
        if (videoView != null && videoView.isShown()) {
            APP.getMainActivity().removeView(videoView);
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        APP.getMainActivity().pause();
        APP.getMainActivity().addView(videoView);

        LogUtil.E("当前播放地址：" + playPath);
        videoView.setVideoPath(playPath);
        videoView.start();
    }

    //关闭视频
    private void closeVideo() {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        APP.getMainActivity().removeView(videoView);
        APP.getMainActivity().resume();
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
    private void timeExecute(String prefix, Date startTime, TimerTask startTask, Date endTime, TimerTask endTask) {
        String key_start = prefix + "_start";
        String key_end = prefix + "_end";
        Timer startTimer = timerMap.get(key_start);
        Timer endTimer = timerMap.get(key_end);
        if (startTimer != null) {
            startTimer.cancel();
        }

        if (endTimer != null) {
            endTimer.cancel();
        }

        startTimer = new Timer();
        endTimer = new Timer();

        startTimer.schedule(startTask, startTime);
        endTimer.schedule(endTask, endTime);

        timerMap.put(key_start, startTimer);
        timerMap.put(key_end, endTimer);
    }

    //解析播放时间，没有date的情况下默认为当天
    private Date[] resolve(String startStr, String endStr) throws ParseException {
        String endTime = correctTime(endStr);
        String startTime = correctTime(startStr);

        String currDateStr = yyyyMMdd.format(todayDate);
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
