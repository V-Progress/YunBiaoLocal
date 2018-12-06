package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.br.USBBroadcastReceiver;
import com.yunbiao.yunbiaolocal.io.VideoDataResolver;
import com.yunbiao.yunbiaolocal.netcore.PnServerController;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.SystemInfoUtil;
import com.yunbiao.yunbiaolocal.view.InsertPlayDialog;
import com.yunbiao.yunbiaolocal.view.MainVideoView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.vtm_video)
    MainVideoView vtmVideo;
    @BindView(R.id.permission)
    TextView permission;
    @BindView(R.id.state)
    TextView state;
    @BindView(R.id.console)
    TextView console;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.pb_video)
    ProgressBar pbVideo;
    @BindView(R.id.pb_download)
    ProgressBar pbDownload;
    @BindView(R.id.tv_download_state)
    TextView tvDownloadState;
    @BindView(R.id.ll_progress_area)
    LinearLayout llProgressArea;
    @BindView(R.id.ll_console)
    LinearLayout llConsole;
    @BindView(R.id.pb_update)
    ProgressBar pbUpdate;
    @BindView(R.id.ll_update_area)
    LinearLayout llUpdateArea;

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private static String[] playList;//播放列表
    private static int videoIndex;//当前视频在列表中处于的位置
    private static int lineNumber = 0;
    public AudioManager audioManager = null;//音频
    private float playSpeed = 1.0f;
    private MediaPlayer mP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        APP.setMainActivity(this);
        ButterKnife.bind(this);

        //初始化控件
        initView();

        //初始化播放器
        initVTMPlayer();

        //连接XMPP
        PnServerController.startXMPP(this);

        //初始化广告插播，如果有未播完的广告则自动播放
        InsertPlayDialog.build(this).init();
    }

    /*===========播放器相关=====================================================================
     * 初始化播放器
     */
    public void initVTMPlayer() {
        initPlayData();

        MediaController mediaController = new MediaController(MainActivity.this);
        mediaController.setInstantSeeking(false);
        vtmVideo.setMediaController(mediaController);
        vtmVideo.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//播放画质
        vtmVideo.setOnPreparedListener(this);//准备完毕监听
        vtmVideo.setOnErrorListener(this);//播放错误监听
        vtmVideo.setOnInfoListener(this);//播放信息监听
    }

    /*
     * 初始化播放数据
     */
    public void initPlayData() {
        VideoDataResolver video = new VideoDataResolver();
        video.setPlayList();
        if (video.timerList == null || video.timerList.isEmpty()) {
            state.setVisibility(View.VISIBLE);
        }
    }

    /*
     * 开始播放
     */
    public void vtmPlay(String videoString) {
        Log.d("log", "开始播放");
        vtmVideo.setVisibility(View.VISIBLE);
        state.setVisibility(View.INVISIBLE);
        vtmVideo.stopPlayback();
        playList = videoString.split(",");
        videoIndex = 0;
        vtmVideo.setVideoPath(playList[0]);
        vtmVideo.setOnCompletionListener(this);
        vtmVideo.start();

    }

    /*
     * 停止播放
     */
    public void vtmStop() {
        if (vtmVideo == null) {
            return;
        }
        vtmVideo.stopPlayback();
        vtmVideo.setVisibility(View.GONE);
        state.setVisibility(View.VISIBLE);
    }

    /*
     * vitamio准备好的回调
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mP = mp;
        mp.setPlaybackSpeed(1.0f);
    }

    /*
     * 当前视频播放完毕的监听
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        videoIndex++;
        if (videoIndex == playList.length) {
            videoIndex = 0;
        }
        try {
            mp.setPlaybackSpeed(playSpeed);
            vtmVideo.stopPlayback();
            vtmVideo.setVideoPath(playList[videoIndex]);
            vtmVideo.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*
     * 播放暂停的监听
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            pbVideo.setVisibility(View.VISIBLE);
            pbVideo.setInterpolator(new AccelerateDecelerateInterpolator());
        } else {
            pbVideo.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    /*
     * vitamio播放错误回调
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        vtmVideo.stopPlayback();
        vtmVideo.resume();
        vtmVideo.start();
        return false;
    }

    /*===========页面控件相关=====================================================================
     * 初始化播放列表
     */
    private void initView() {

        //USB广播监听
        usbBroadcastReceiver = new USBBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(usbBroadcastReceiver, intentFilter);
        //是否授权
        try {
            long expire = new SimpleDateFormat("yyyyMMdd").parse("20250101").getTime();
            if (expire < System.currentTimeMillis()) {
                permission.setVisibility(View.VISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*
     * 打开控制台
     */
    public void openConsole() {
        llConsole.setVisibility(View.VISIBLE);
    }

    /*
     * 关闭控制台
     */
    public void closeConsole() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                llConsole.setVisibility(View.GONE);
                progress.setProgress(0);
                progress.setMax(0);
                console.setText("");
            }
        }, 3000);
    }

    /*
     * 更新控制台显示
     */
    public void updateConsole(final String msg) {
        String text = console.getText().toString();
        if (lineNumber < 5) {
            lineNumber++;
        } else {
            text = text.substring(text.indexOf("\n") + 1);
        }

        if (lineNumber > 1) {
            text += "\n";
        }
        console.setText(text + msg);
    }

    public void initProgress(final int max) {
        progress.setMax(max);
    }

    public void updateProgress(final int pg) {
        progress.setProgress(pg);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            vtmVideo.pause();
            vtmVideo.setVisibility(View.GONE);

            DialogUtil.getInstance(this).showPlayListDialog(
                    VideoDataResolver.playList == null
                            ? new ArrayList<String>()
                            : VideoDataResolver.playList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vtmVideo.setVisibility(View.VISIBLE);
                            vtmVideo.start();
                        }
                    });
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogUtil.getInstance(this).showTestController();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vtmVideo.resume();
        vtmVideo.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vtmVideo.pause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbBroadcastReceiver);
        NetUtil.getInstance().stop();
        PnServerController.stopXMPP();
        super.onDestroy();

    }

    //下载更新的监听，外部静态调用
    public NetUtil.OnDownLoadListener downloadUpdateListener = new NetUtil.OnDownLoadListener() {
        @Override
        public void onStart(String fileName) {
            llUpdateArea.setVisibility(View.VISIBLE);
            pbUpdate.setMax(100);
        }

        @Override
        public void onDownloading(int progress) {
            pbUpdate.setProgress(progress);
        }

        @Override
        public void onComplete(File response) {
            SystemInfoUtil.installApk(MainActivity.this,response);
        }

        @Override
        public void onFinish() {
            llUpdateArea.setVisibility(View.GONE);
        }

        @Override
        public void onError(Exception e) {
            String message = e.getMessage();
            if (TextUtils.isEmpty(message)) {
                return;
            }

            switch (message) {
                case "1":
                    Toast.makeText(APP.getContext(), "当前版本为最新版本", Toast.LENGTH_SHORT).show();
                    if(llUpdateArea.isShown()){
                        llUpdateArea.setVisibility(View.GONE);
                    }
                    break;
                case "fail":
                    Toast.makeText(APP.getContext(), "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                    if(llUpdateArea.isShown()){
                        llUpdateArea.setVisibility(View.GONE);
                    }
                    break;
                default:
                    Toast.makeText(APP.getContext(), message, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

}
