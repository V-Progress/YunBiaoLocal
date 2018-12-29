package com.yunbiao.cccm.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.act.MainActivity;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.utils.TimerExecutor;
import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/4.
 */

public class InsertPlayDialog extends Dialog implements MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {

    private static InsertPlayDialog insertPlayDialog;
    private VideoView videoView;
    private ProgressBar pbInsertLoading;

    private SimpleDateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    private View rootView;
    private MyScrollTextView msTvInsert;

    public static synchronized InsertPlayDialog build(Activity context) {
        if (insertPlayDialog == null) {
            insertPlayDialog = new InsertPlayDialog(context);
        }
        return insertPlayDialog;
    }

    private InsertPlayDialog(@NonNull Context context) {
        super(context, R.style.FullScreenDialog);
        initView();
        setCancelable(false);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        rootView = inflater.inflate(R.layout.dialog_insert_content, null);
        setContentView(rootView);

        videoView = rootView.findViewById(R.id.vv_insert);
        pbInsertLoading = rootView.findViewById(R.id.pb_insert_loading);
        msTvInsert = rootView.findViewById(R.id.mstv_insert);

        videoView.setZOrderOnTop(true);//解决VideoView被Dialog遮挡问题
        videoView.setOnInfoListener(this);
        videoView.setOnPreparedListener(this);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);
    }

    //监听直播源的播放情况
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始缓冲
                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                setLoading(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲结束
                videoView.start();
                setLoading(View.GONE);
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT://缓冲超时
                rePlayVideo();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED://无法连接服务器
                showErrText("无法连接到服务器！");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN://未知的直播源
                showErrText("直播源播放失败，未知错误！");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED://不支持的视频格式
                showErrText("不支持的视频流格式！");
                break;
        }
        return true;
    }

    //播放器准备完毕后的监听，设置为循环播放
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);//循环播放
    }

    //显示插入广告
    public void showInsert(final String content) {
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    InsertVideoModel insertVideoModel = new Gson().fromJson(content, InsertVideoModel.class);
//                    String playDate = insertVideoModel.getPlayDate();
//                    String playCurTime = insertVideoModel.getPlayCurTime();
//                    final Date[] dates = resolveTime(playDate, playCurTime);
//                    if (dates != null && dates.length > 0) {
//
//                        final String fileUrl = insertVideoModel.getFileurl();
//                        setVideo(dates[0], dates[1], fileUrl);
//                    } else {
//                        LogUtil.E("播放时间已过！");
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    //显示的同时暂停首页的视频
    @Override
    public void show() {
        super.show();
        pauseMainPlay();
    }

    //关闭的时候开启首页的视频
    @Override
    public void dismiss() {
        super.dismiss();
        resumeMainPlay();
    }

    /*
     * 设置播放视频
     * @param startDate
     * @param endDate
     * @param fileurl
     */
    private void setVideo(final Date startDate, final Date endDate, String fileurl) {
        if (fileurl.endsWith(".avi") || fileurl.endsWith(".mp4") || fileurl.endsWith(".3gp")) {
            NetUtil.getInstance().downloadFile(fileurl, new NetUtil.OnDownLoadListener() {
                @Override
                public void onStart(String fileName) {
                    LogUtil.E("开始下载视频");
                }

                @Override
                public void onProgress(int progress) {
                    LogUtil.E("正在下载-" + progress + "%");
                }

                @Override
                public void onComplete(final File response) {
                    TimerExecutor.getInstance().addInTimerQueue(startDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {
                            show();
                            playVideo(response.getAbsolutePath());
                        }
                    });
                    TimerExecutor.getInstance().addInTimerQueue(endDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {
                            dismiss();
                        }
                    });
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onError(Exception e) {
                    LogUtil.E("下载失败：" + e.getMessage());
                }
            });
        } else if ((fileurl.startsWith("http://") && fileurl.endsWith(".m3u8"))) {
            playVideo(fileurl);
        } else {
            showErrText("视频源地址无效！");
        }
    }

    /*
     * 广告开始的时候，暂停背景视频
     */
    private void pauseMainPlay() {
        MainActivity mainActivity = APP.getMainActivity();
        if (mainActivity != null) {
        }
    }

    /*
     * 广告结束的时候，开始背景视频
     */
    private void resumeMainPlay() {
        MainActivity mainActivity = APP.getMainActivity();
        if (mainActivity != null) {
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

    /*
        开始播放
     */
    private void playVideo(String content) {
        msTvInsert.setVisibility(View.GONE);
        pbInsertLoading.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(content);
        videoView.start();
    }

    /*
        重新开始
     */
    private void rePlayVideo() {
        videoView.stopPlayback();
        videoView.start();
    }

    /*
        设置加载条
     */
    private void setLoading(int show) {
        pbInsertLoading.setVisibility(show);
    }

    /*
        展示错误文字
     */
    private void showErrText(String err) {
        videoView.setVisibility(View.GONE);
        pbInsertLoading.setVisibility(View.GONE);

    }

    /*
        文字广告
     */
    private void showTextContent(InsertTextModel insertTextModel) {
        videoView.setVisibility(View.GONE);
        pbInsertLoading.setVisibility(View.GONE);
        msTvInsert.setVisibility(View.VISIBLE);
        msTvInsert.setText("");
        LogUtil.E(insertTextModel.getText());

        InsertTextModel.Content content = insertTextModel.getContent();
        msTvInsert.setTextSize(Integer.valueOf(content.getFontSize()));//字号
        msTvInsert.setTextColor(Color.parseColor(content.getFontColor()));//字体颜色
        msTvInsert.setScrollSpeed(Integer.valueOf(content.getPlaySpeed()));
        msTvInsert.setDirection(Integer.valueOf(content.getPlayType()));
        msTvInsert.setBackColor(Color.parseColor(content.getBackground()));//背景色
        msTvInsert.setText(insertTextModel.getText());//内容
    }

    public Bitmap screenShot() {
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        return Bitmap.createBitmap(rootView.getDrawingCache());
    }
}
