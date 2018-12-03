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

    Handler closeConsoleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            console.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.INVISIBLE);
            progress.setProgress(0);
            progress.setMax(0);
            console.setText("");
        }
    };


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


        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDownloading){
                    startDownload(downloadUrl);
                }else{
                    downloadTask.pauseDownload();
                }
            }
        });
    }

    public static String downloadUrl = "https://qd.myapp.com/myapp/qqteam/pcqq/QQ9.0.8.exe";
    /**
     * 开始下载
     * @param url
     */
    public void  startDownload(String url) {
        if (downloadTask == null) {
            downloadUrl = url;
            downloadTask = new DownloadTask(new DownloadListener() {
                @Override
                public void onProgress(int progress) {
                    Log.e("123","正在下载..."+progress);
                }

                @Override
                public void onSuccess() {
                    Log.e("123","下载完成...onSuccess");
                }

                @Override
                public void onFailed() {
                    Log.e("123","下载失败...onFailed");
                }

                @Override
                public void onPaused() {
                    Log.e("123","下载暂停...onPaused");
                }

                @Override
                public void onCanceled() {
                    Log.e("123","下载取消...onCanceled");
                }
            });
            //启动下载任务
            downloadTask.execute(downloadUrl);
        }
    }

    private void openConsole() {
        console.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
    }

    private void closeConsole() {
        closeConsoleHandler.sendEmptyMessageDelayed(0, 2000);
    }

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

    private void initView() {

        mLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.alert_dialog, null);
        mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        mTimer = mLinearLayout.findViewById(R.id.timer);
        mPreview = mLinearLayout.findViewById(R.id.preview);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbBroadcastReceiver);
        NetUtil.getInstance().stop();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    private void initPlayer() {
        Video video = new Video();
        video.setPlayList();
        if (video.timerList == null || video.timerList.isEmpty())
            state.setVisibility(View.VISIBLE);
    }

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

    private void download(){
            NetUtil.OnDownLoadListener onDownLoadListener = new NetUtil.OnDownLoadListener() {
                @Override
                public void onStart(String fileName) {
                    Log.e("123", "下载开始");
                    llProgressArea.setVisibility(View.VISIBLE);
                    tvDownloadState.setText("开始下载");
                }

                @Override
                public void onDownloading(String progress) {
                    tvDownloadState.setText("正在下载"+progress);
                    Log.e("123", progress);
                }

                @Override
                public void onComplete(File response) {
                    tvDownloadState.setText("下载完成");
//                    try {
//                        ZipUtil.UnZipFolder("", "");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onFinish() {
                    Log.e("123", "下载结束");
                    llProgressArea.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("123", "下载错误");
                    download();
                }
            };
        try {
            NetUtil.getInstance().downLoadFile(onDownLoadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //
//    private boolean haveNewRes() {
//        boolean isHave = false;
//        //检测网络
//        NetUtil.getInstance().requestNet("", new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                //解析响应
//
//            }
//        });
//        return isHave;
//    }
//
//    NetUtil.OnDownLoadListener onDownLoadListener = new NetUtil.OnDownLoadListener() {
//        @Override
//        public void onStart(String fileName) {
//            Log.e("123", "下载开始");
//        }
//
//        @Override
//        public void onDownloading(String progress) {
//            Log.e("123", progress);
//        }
//
//        @Override
//        public void onComplete(File response) {
//            try {
//                ZipUtil.UnZipFolder("", "");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onFinish() {
//            Log.e("123", "下载结束");
//        }
//
//        @Override
//        public void onError(Exception e) {
//            Log.e("123", "下载错误");
//        }
//    };

//        TimerExecutor.getInstance().addInQueue("2018-11-30 10:05:00", new TimerExecutor.OnTimeOutListener() {
//            @Override
//            public void timeOut() {
//                Log.e("123", "开始执行了1111111111111");
//            }
//        });
//
//        TimerExecutor.getInstance().addInQueue("2018-11-30 10:10:00", new TimerExecutor.OnTimeOutListener() {
//            @Override
//            public void timeOut() {
//                Log.e("123", "开始执行了22222222222222");
//            }
//        });
//
//        TimerExecutor.getInstance().addInQueue("2018-11-30 10:10:15", new TimerExecutor.OnTimeOutListener() {
//            @Override
//            public void timeOut() {
//                Log.e("123", "开始执行了3333333333333333");
//            }
//        });
//
//        TimerExecutor.getInstance().addInQueue("2018-11-30 10:10:30", new TimerExecutor.OnTimeOutListener() {
//            @Override
//            public void timeOut() {
//                Log.e("123", "开始执行了44444444444444444");
//            }
//        });

}
