package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.yunbiao.yunbiaolocal.netcore.PnServerController;
import com.yunbiao.yunbiaolocal.resolve.VideoDataResolver;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.SystemInfoUtil;
import com.yunbiao.yunbiaolocal.view.InsertPlayDialog;
import com.yunbiao.yunbiaolocal.view.MainVideoView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;

public class MainActivity extends BaseActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {
    @BindView(R.id.vtm_video)
    public MainVideoView vtmVideo;
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
    private float playSpeed = 1.0f;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        APP.setMainActivity(this);
//        ButterKnife.bind(this);
//
//        //初始化控件
//        initView();
//
//        //初始化播放数据
//        initPlayData();
//
//        //初始化播放器
//        initVTMPlayer();
//
//        //连接XMPP
//        PnServerController.startXMPP(this);
//
//        //初始化广告插播，如果有未播完的广告则自动播放
//        InsertPlayDialog.build(this).layoutInit();
//
//
//    }

    protected int setLayout(){
        APP.setMainActivity(this);
        return R.layout.activity_main;
    }

    protected void initView() {
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

    protected void initData(){
        //初始化播放数据
        initPlayData();

        //初始化播放器
        initVTMPlayer();

        //连接XMPP
        PnServerController.startXMPP(this);

        //初始化广告插播，如果有未播完的广告则自动播放
        InsertPlayDialog.build(this).init();
    }

    /*===========播放器控制相关=====================================================================
     * 初始化播放器
     */
    public void initVTMPlayer() {
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
        VideoDataResolver videoDataResolve = new VideoDataResolver();
        videoDataResolve.resolvePlayList();
        if (videoDataResolve.getPlayList() == null || videoDataResolve.getPlayList().isEmpty()) {
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

    /*===========播放器状态监听=====================================================================
     * vitamio准备好的回调
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, MenuActivity.class));
            vtmVideo.pause();
            vtmVideo.setVisibility(View.GONE);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!vtmVideo.isShown()) {
            vtmVideo.setVisibility(View.VISIBLE);
        }
        if (!vtmVideo.isPlaying()) {
            vtmVideo.resume();
            vtmVideo.start();
        }
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
        APP.exit();
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
            SystemInfoUtil.installApk(MainActivity.this, response);
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
                    if (llUpdateArea.isShown()) {
                        llUpdateArea.setVisibility(View.GONE);
                    }
                    break;
                case "fail":
                    Toast.makeText(APP.getContext(), "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                    if (llUpdateArea.isShown()) {
                        llUpdateArea.setVisibility(View.GONE);
                    }
                    break;
                default:
                    Toast.makeText(APP.getContext(), message, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    public Long getVideoCurrTime() {
        if(vtmVideo != null && vtmVideo.isPlaying()){
            return vtmVideo.getCurrentPosition();
        }
        return 0L;
    }

    public String getCurrPlayVideo() {
        if(playList != null && playList.length>0){
            return playList[videoIndex];
        }
        return "null";
    }

    /**
     * 判断某个界面是否在前台
     *
     * @return 是否在前台显示
     */
    public boolean isForeground() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getShortClassName().contains(this.getClass().getSimpleName())) { // 说明它已经启动了
                return true;
            }
        }
        return false;
    }
}
