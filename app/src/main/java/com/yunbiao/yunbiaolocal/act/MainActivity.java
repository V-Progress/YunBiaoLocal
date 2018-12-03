package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yunbiao.yunbiaolocal.Const;
import com.yunbiao.yunbiaolocal.br.EventMessage;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.br.USBBroadcastReceiver;
import com.yunbiao.yunbiaolocal.io.Video;
import com.yunbiao.yunbiaolocal.netcore.DownloadListener;
import com.yunbiao.yunbiaolocal.netcore.DownloadTask;
import com.yunbiao.yunbiaolocal.utils.NetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.video)
    VideoView video;
    @BindView(R.id.permission)
    TextView permission;
    @BindView(R.id.state)
    TextView state;
    @BindView(R.id.console)
    TextView console;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.pb_download)
    ProgressBar pbDownload;
    @BindView(R.id.tv_download_state)
    TextView tvDownloadState;
    @BindView(R.id.ll_progress_area)
    LinearLayout llProgressArea;

    private LinearLayout mLinearLayout;
    private ListView mPlaylist;
    private TextView mTimer;
    private VideoView mPreview;

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private static String[] playList;//播放列表
    private static int videoIndex;
    private AlertDialog mAlertDialog;
    private String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private static int lineNumber = 0;
    public AudioManager audioManager = null;//音频
    private DownloadTask downloadTask;
    boolean isDownloading = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage event) {
        switch (event.getControlType()) {
            case Const.CONTROL_EVENT.OPEN_CONSOLE:
                openConsole();
                break;
            case Const.CONTROL_EVENT.UPDATE_CONSOLE:
                updateConsole(event.getConsoleMsg());
                break;
            case Const.CONTROL_EVENT.INIT_PROGRESS:
                progress.setMax(Integer.parseInt(event.getConsoleMsg()));
                break;
            case Const.CONTROL_EVENT.UPDATE_PROGRESS:
                Log.e("123", "当前进度：" + event.getConsoleMsg());
                progress.setProgress(Integer.parseInt(event.getConsoleMsg()));
                break;
            case Const.CONTROL_EVENT.INIT_PLAYER:
                initPlayer();
                closeConsole();
                lineNumber = 0;
                break;
            case Const.CONTROL_EVENT.VIDEO_PLAY:
                play(event.getConsoleMsg());
                break;
            case Const.CONTROL_EVENT.VIDEO_STOP:
                Log.d("log", "停止播放");
                video.stopPlayback();
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initView();

        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(date);
        Log.e("123", "今天日期" + format);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化
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
        //设置播放器
        initPlayer();
    }

    private void initView() {
        mLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.alert_dialog, null);
        mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        mTimer = mLinearLayout.findViewById(R.id.timer);
        mPreview = mLinearLayout.findViewById(R.id.preview);
    }


    /***
     * 打开控制台
     */
    private void openConsole() {
        console.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
    }

    /***
     * 关闭控制台
     */
    private void closeConsole() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                console.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.INVISIBLE);
                progress.setProgress(0);
                progress.setMax(0);
                console.setText("");
            }
        },2000);

    }

    /***
     * 更新控制台显示
     */
    private void updateConsole(String msg) {
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

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView textView = (TextView) view;
            String text = textView.getText().toString();
            if (!text.matches("^.+\\.\\S+$"))
                return;
            if (mPreview.isPlaying())
                mPreview.stopPlayback();
            String path = Video.previewMap.get(yyyyMMdd + text.substring(3));
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(MainActivity.this, "没有视频", Toast.LENGTH_SHORT).show();
                return;
            }
            mPreview.setVideoPath(path);
            mPreview.start();
        }
    };

    //创建播放列表dialog
    private void creatPlayList() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Video.playList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) convertView;
                String text = getItem(position);
                textView.setText(text);
                if (text.contains(".")) {
                    textView.setSingleLine();
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                } else
                    textView.setBackgroundColor(Color.parseColor("#333333"));
                return convertView;
            }
        };
        if (mPlaylist == null) {
            Log.e(TAG, "mPlaylist为null ");
            return;
        }
        if (arrayAdapter == null) {
            Log.e(TAG, "arrayAdapter为null ");
            return;
        }
        mPlaylist.setAdapter(arrayAdapter);
        mPlaylist.setOnItemClickListener(onItemClickListener);
        mTimer.setText(Video.timer);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(mLinearLayout);
        builder.setTitle("播放列表");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPreview.isPlaying())
                    mPreview.stopPlayback();
                video.setVisibility(View.VISIBLE);
                video.start();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setLayout(-1, -1);
    }

    /***
     * 初始化播放器
     */
    private void initPlayer() {
        Video video = new Video();
        video.setPlayList();
        if (video.timerList == null || video.timerList.isEmpty())
            state.setVisibility(View.VISIBLE);
    }

    /***
     * 开始播放
     * @param videoString
     */
    private void play(String videoString) {
        Log.d("log", "开始播放");
        state.setVisibility(View.INVISIBLE);
        video.stopPlayback();//停止播放
        playList = videoString.split(",");
        videoIndex = 0;
        video.setVideoPath(playList[0]);
        video.start();
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoIndex++;
                if (videoIndex == playList.length)
                    videoIndex = 0;
                try {
                    mp.reset();
                    mp.setDataSource(playList[videoIndex]);
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            video.pause();
            if (mAlertDialog == null)
                creatPlayList();
            mAlertDialog.show();
            video.setVisibility(View.INVISIBLE);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbBroadcastReceiver);
        NetUtil.getInstance().stop();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
