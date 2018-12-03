package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.Const;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.br.EventMessage;
import com.yunbiao.yunbiaolocal.br.USBBroadcastReceiver;
import com.yunbiao.yunbiaolocal.io.Video;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.vtm_video)
    VideoView vtmVideo;
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

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private static String[] playList;//播放列表
    private static int videoIndex;//当前视频在列表中处于的位置
    private static int lineNumber = 0;
    public AudioManager audioManager = null;//音频
    private float playSpeed = 1.0f;
    private MediaPlayer mP;

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(EventMessage event) {
        switch (event.getControlType()) {
            //控制台
            case Const.CONTROL_EVENT.OPEN_CONSOLE:
                Log.e("123", "收到消息");
                openConsole();
                updateConsole(event.getConsoleMsg());
                break;
            case Const.CONTROL_EVENT.UPDATE_CONSOLE:
                updateConsole(event.getConsoleMsg());
                break;
            case Const.CONTROL_EVENT.CLOSE_CONSOLE:
                closeConsole();
                break;
            //进度条
            case Const.CONTROL_EVENT.INIT_PROGRESS:
                progress.setMax(Integer.parseInt(event.getConsoleMsg()));
                break;
            case Const.CONTROL_EVENT.UPDATE_PROGRESS:
                Log.e("123", "当前进度：" + event.getConsoleMsg());
                progress.setProgress(Integer.parseInt(event.getConsoleMsg()));
                break;
            //播放器
            case Const.CONTROL_EVENT.INIT_PLAYER:
                initPlayData();
                closeConsole();
                lineNumber = 0;
                break;
            case Const.CONTROL_EVENT.VIDEO_PLAY:
                vtmPlay(event.getConsoleMsg());
                break;
            case Const.CONTROL_EVENT.VIDEO_STOP:
                Log.d("log", "停止播放");
                vtmStop();
                break;
        }
    }


    boolean isLocalRes = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        APP.setMainActivity(this);

        //初始化控件
        initView();
        //初始化播放器
        initVTMPlayer();

        Button btnVtm = findViewById(R.id.btn_vtm);
        btnVtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isLocalRes) {
//                    vtmStop();
//                    vtmVideo.setVideoPath("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8");
//                    vtmVideo.start();
//                } else {
//                    vtmStop();
//                    vtmVideo.setVideoPath(playList[0]);
//                    vtmVideo.start();
//                }
//                isLocalRes = !isLocalRes;

                DialogUtil.getInstance(MainActivity.this).showInsertDialog(DialogUtil.INSERT_LIVE,"http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8");
            }
        });
    }

    /*===========播放器相关=====================================================================
     * 初始化播放器
     */
    public void initVTMPlayer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Video video = new Video();
                video.setPlayList();

                MediaController mediaController = new MediaController(MainActivity.this);
                mediaController.setInstantSeeking(false);
                vtmVideo.setMediaController(mediaController);
                vtmVideo.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
                vtmVideo.setOnPreparedListener(MainActivity.this);//准备完毕监听
                vtmVideo.setOnErrorListener(MainActivity.this);//播放错误监听
                vtmVideo.setOnInfoListener(MainActivity.this);//播放信息监听
            }
        });
    }

    /*
     * 初始化播放数据
     */
    public void initPlayData() {
        Video video = new Video();
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
            Log.e("123", "11111111111111");
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

        // 安卓音频初始化
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                llConsole.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
     * 关闭控制台
     */
    public void closeConsole() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    /*
     * 更新控制台显示
     */
    public void updateConsole(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });

    }

    public void initProgress(final int max){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMax(max);
            }
        });

    }

    public void updateProgress(final String pg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(Integer.parseInt(pg));
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            vtmVideo.pause();
            vtmVideo.setVisibility(View.GONE);

            DialogUtil.getInstance(this).showPlayListDialog(
                    Video.playList == null
                            ? new ArrayList<String>()
                            : Video.playList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vtmVideo.setVisibility(View.VISIBLE);
                            vtmVideo.start();
                        }
                    });
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vtmVideo.resume();
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
