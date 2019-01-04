package com.yunbiao.cccm.act;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.act.base.BaseActivity;
import com.yunbiao.cccm.br.USBBroadcastReceiver;
import com.yunbiao.cccm.devicectrl.PowerOffTool;
import com.yunbiao.cccm.download.ResourceManager;
import com.yunbiao.cccm.netcore.OnXmppConnListener;
import com.yunbiao.cccm.netcore.PnServerController;
import com.yunbiao.cccm.resolve.VideoDataResolver;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.SystemInfoUtil;
import com.yunbiao.cccm.view.MainVideoView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;

public class MainActivity extends BaseActivity implements MainRefreshListener {
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
    @BindView(R.id.ll_console)
    LinearLayout llConsole;
    @BindView(R.id.pb_update)
    ProgressBar pbUpdate;
    @BindView(R.id.ll_update_area)
    LinearLayout llUpdateArea;
    @BindView(R.id.fl_root)
    FrameLayout flRoot;

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private List<String> playLists;
    private static int videoIndex;//当前视频在列表中处于的位置
    private static int lineNumber = 0;
    private float playSpeed = 1.0f;
    private boolean isInsertPlaying = false;
    private boolean isCycle = true;

    protected int setLayout() {
        return R.layout.activity_main;
    }

    protected void initView() {
        //先将插播fragment创建出来
        APP.setMainActivity(this);
        MainController.getInstance().registerActivity(this);

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

    protected void initData() {
        //初始化播放器
        initPlayer();

        //初始化播放数据
//        initPlayData(true);// TODO: 2018/12/30 初始化本地数据

        //连接XMPP
        PnServerController.startXMPP(this);

        //初始化自动开关机数据
        PowerOffTool.getInstance().initPowerData();

        //初始化数据下载
        ResourceManager.getInstance().initResData();

        //初始化广告插播，如果有未播完的广告则自动播放
        ResourceManager.getInstance().initInsertData();
    }

    //-------播放器控制----------------------------------------------------------------
    @Override
    public void initPlayer() {
        MediaController mediaController = new MediaController(this);
        mediaController.setInstantSeeking(false);
        vtmVideo.setMediaController(mediaController);
        vtmVideo.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);//播放画质
        vtmVideo.setOnPreparedListener(preparedListener);//准备完毕监听
        vtmVideo.setOnErrorListener(errorListener);//播放错误监听
        vtmVideo.setOnInfoListener(infoListener);//播放信息监听
        vtmVideo.setOnCompletionListener(completionListener);
    }

    /*
     * 初始化播放数据
     */
    @Override
    public void initPlayData(boolean isRemote) {
        VideoDataResolver videoDataResolve = new VideoDataResolver();
        if(isRemote){
            videoDataResolve.resolvePlayLists();
        }else{
            videoDataResolve.resolveLocalResource();
        }

        if (videoDataResolve.getPlayList() == null || videoDataResolve.getPlayList().isEmpty()) {
            state.setVisibility(View.VISIBLE);
        }
    }

    //常规资源播放
    @Override
    public void startPlay(List<String> videoList) {
        if(isInsertPlaying){
            LogUtil.E("广告正在播放，不执行startPlay");
            return;
        }
        play(videoList);
    }

    //常规资源停止
    @Override
    public void stopPlay() {
        if(isInsertPlaying){
            LogUtil.E("广告正在播放，不执行stopPlay");
            return;
        }
        stop();
    }

    //插播资源播放
    @Override
    public void startInsert(final boolean isCycle, final List<String> videoList){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.isInsertPlaying = true;
                MainActivity.this.isCycle = isCycle;
                LogUtil.E("开始播放插播："+ videoList.toString());
                LogUtil.E("是否轮播："+MainActivity.this.isCycle);
                play(videoList);
            }
        },2000);
    }

    //插播资源停止
    @Override
    public void stopInsert(){
        isCycle = true;
        isInsertPlaying = false;
        LogUtil.E("停止播放插播");
        stop();
        initPlayData(true);
    }

    //------控制台-----------------------------------------------------------------
    /*
     * 打开控制台
     */
    @Override
    public void openConsole() {
        llConsole.setVisibility(View.VISIBLE);
    }

    /*
     * 关闭控制台
     */
    @Override
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

    @Override
    public void updateConsole(String msg) {
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

    //------进度条控制----------------------------------------------------------------
    @Override
    public void initProgress(final int max) {
        progress.setMax(max);
    }

    @Override
    public void updateProgress(final int pg) {
        progress.setProgress(pg);
    }

    //-----常规方法----------------------------------------------------------------
    private void stop(){
        if (vtmVideo == null) {
            return;
        }
        vtmVideo.stopPlayback();
        vtmVideo.setVisibility(View.GONE);
        state.setVisibility(View.VISIBLE);
    }

    private void play(final List<String> videoList){
        videoIndex = 0;
        playLists = videoList;

        state.setVisibility(View.INVISIBLE);
        if(vtmVideo.isPlaying()){
            vtmVideo.stopPlayback();
        }

        vtmVideo.setVideoPath(playLists.get(videoIndex));
        vtmVideo.setVisibility(View.VISIBLE);
        vtmVideo.start();
    }

    public void removeView(View view) {
        flRoot.removeView(view);
    }

    public void addView(View view) {
        flRoot.addView(view);
    }

    /*===========播放器监听关=====================================================================
     * 初始化播放器
     */
    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setPlaybackSpeed(1.0f);
        }
    };

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            vtmVideo.stopPlayback();
            vtmVideo.resume();
            vtmVideo.start();
            return false;
        }
    };

    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
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
    };

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            videoIndex++;
            if (videoIndex == playLists.size()) {
                videoIndex = 0;
                LogUtil.E("是否轮播");
                if(!isCycle){
                    LogUtil.E("不轮播");
                    stopInsert();
                    return;
                }
            }
            try {
                mp.setPlaybackSpeed(playSpeed);
                vtmVideo.stopPlayback();
                vtmVideo.setVideoPath(playLists.get(videoIndex));
                vtmVideo.start();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /*===========页面控件相关=====================================================================
     * 初始化播放列表
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, MenuActivity.class));
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            APP.exit();
            return false;
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
        super.onDestroy();
        unregisterReceiver(usbBroadcastReceiver);
        NetUtil.getInstance().stop();
        PnServerController.stopXMPP();
        APP.exit();
    }

    //下载更新的监听，外部静态调用
    public NetUtil.OnDownLoadListener downloadUpdateListener = new NetUtil.OnDownLoadListener() {
        @Override
        public void onStart(String fileName) {
            openConsole();
            updateConsole("开始下载更新...");
            initProgress(100);
        }

        @Override
        public void onProgress(int progress) {
            updateProgress(progress);
        }

        @Override
        public void onComplete(File response) {
            SystemInfoUtil.installApk(MainActivity.this, response);
        }

        @Override
        public void onFinish() {
            closeConsole();
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
                    closeConsole();
                    break;
                case "fail":
                    Toast.makeText(APP.getContext(), "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                    closeConsole();
                    break;
                default:
                    Toast.makeText(APP.getContext(), message, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    public boolean isVideoPlaying(){
        return vtmVideo != null && vtmVideo.isPlaying();
    }

    public Long getVideoCurrTime() {
        if (vtmVideo != null && vtmVideo.isPlaying()) {
            return vtmVideo.getCurrentPosition();
        }
        return 0L;
    }

    public String getCurrPlayVideo() {
        if (playLists != null && playLists.size() > 0) {
            return playLists.get(videoIndex);
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

    public OnXmppConnListener xmppConnListener = null;
}
