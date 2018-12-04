package com.yunbiao.yunbiaolocal.view;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;
import com.yunbiao.yunbiaolocal.utils.TimerExecutor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/4.
 */

public class InsertPlayDialog extends Dialog implements MediaPlayer.OnInfoListener {

    private static InsertPlayDialog insertPlayDialog;
    private VideoView videoView;
    private ProgressBar pbInsertLoading;
    private TextView tvInsert;
    private String mContent;

    private DateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private int showType = DialogUtil.INSERT_TEXT;//播放类型默认文字

    public static synchronized InsertPlayDialog build(Context context, int insertType) {
        if (insertPlayDialog == null) {
            insertPlayDialog = new InsertPlayDialog(context, insertType);
        }
        return insertPlayDialog;
    }

    private InsertPlayDialog(@NonNull Context context, int insertType) {
        super(context, R.style.FullScreenDialog);
        showType = insertType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setCancelable(false);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.layout_insert_content, null);
        setContentView(rootView);

        videoView = rootView.findViewById(R.id.vv_insert);
        pbInsertLoading = rootView.findViewById(R.id.pb_insert_loading);
        tvInsert = rootView.findViewById(R.id.tv_insert);

        videoView.setZOrderOnTop(true);//解决背景色问题
//        videoView.setZOrderMediaOverlay(true);//解决遮挡问题
        videoView.setOnInfoListener(this);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                setLoading(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                videoView.start();
                setLoading(View.GONE);
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                rePlayVideo();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                showErrText("无法连接到服务器！");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                showErrText("直播源播放失败，未知错误！");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                showErrText("不支持的视频流格式！");
                break;
        }
        return true;
    }

    public void show(String content) {
        this.mContent = content;
        // TODO: 2018/12/4 解析内容并设置播放
        try {
            String begin = "2018120414:35";
            String end = "2018120414:37";
            Date beginTime = yyyyMMddHH_mm.parse(begin);
            Date endTime = yyyyMMddHH_mm.parse(end);

            TimerExecutor.getInstance().addInTimerQueue(beginTime, new TimerExecutor.OnTimeOutListener() {
                @Override
                public void execute() {
                    InsertPlayDialog.super.show();
                    setContent();
                }
            });

            TimerExecutor.getInstance().addInTimerQueue(endTime, new TimerExecutor.OnTimeOutListener() {
                @Override
                public void execute() {
                    insertPlayDialog.dismiss();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*
        设置播放内容
        必须在show之后调用
     */
    private void setContent() {
        switch (showType) {
            case DialogUtil.INSERT_TEXT:
                showTextContent(mContent);
                break;
            case DialogUtil.INSERT_LIVE:
            case DialogUtil.INSERT_VIDEO:
                if (mContent.startsWith("http://") && mContent.endsWith(".m3u8")) {
                    playVideo(mContent);
                } else {
                    showErrText("视频源地址无效！");
                }
                break;
        }
    }

    /*
        开始播放
     */
    private void playVideo(String content) {
        tvInsert.setVisibility(View.GONE);
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
        tvInsert.setText(err);
    }

    /*
        文字广告
     */
    private void showTextContent(String content) {
        videoView.setVisibility(View.GONE);
        pbInsertLoading.setVisibility(View.GONE);
        tvInsert.setVisibility(View.VISIBLE);
        tvInsert.setText(content);
    }

}
