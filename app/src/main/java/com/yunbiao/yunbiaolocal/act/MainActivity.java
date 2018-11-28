package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.br.USBBroadcastReceiver;
import com.yunbiao.yunbiaolocal.io.Video;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.ZipUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TextView permission;
    private static TextView state;
    private static TextView console;
    private static ProgressBar progress;
    private static VideoView videoView;
    private LinearLayout mLinearLayout;
    private ListView mPlaylist;
    private TextView mTimer;
    private VideoView mPreview;

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private static String[] video;//播放列表
    private static int videoIndex;
    private AlertDialog mAlertDialog;
    private String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private static int lineNumber = 0;

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String value = msg.getData().getString(String.valueOf(msg.what));
            switch (msg.what) {
                case (1)://打开控制台
                    console.setVisibility(View.VISIBLE);
                    break;
                case (2):
                    String text = console.getText().toString();
                    if (lineNumber < 5)
                        lineNumber++;
                    else
                        text = text.substring(text.indexOf("\n") + 1);
                    if (lineNumber > 1)
                        text += "\n";
                    console.setText(text + value);
                    break;
                case (3)://设置进度以及最大显示
                    progress.setMax(Integer.parseInt(value));
                    progress.setVisibility(View.VISIBLE);
                    break;
                case (4)://进度增加
                    progress.setProgress(progress.getProgress() + 1);
                    break;
                case (0)://初始化播放器
                    initPlayer();
                    progress.setVisibility(View.INVISIBLE);
                    progress.setProgress(0);
                    console.setVisibility(View.INVISIBLE);
                    console.setText(null);
                    lineNumber = 0;
                    break;
                case (11)://开始播放
                    play(value);
                    break;
                case (10)://停止播放
                    Log.d("log", "停止播放");
                    videoView.stopPlayback();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();





//        NetUtil.getInstance().downLoadFile(onDownLoadListener);

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

    private boolean haveNewRes(){
        boolean isHave = false;
        //检测网络
        NetUtil.getInstance().requestNet("", new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                //解析响应

            }
        });
        return isHave;
    }

    NetUtil.OnDownLoadListener onDownLoadListener = new NetUtil.OnDownLoadListener() {
        @Override
        public void onStart(String fileName) {
            Log.e("123","下载开始");
        }

        @Override
        public void onDownloading(String progress) {
            Log.e("123",progress);
        }

        @Override
        public void onComplete(File response) {
            try {
                ZipUtil.UnZipFolder("","");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFinish() {
            Log.e("123","下载结束");
        }

        @Override
        public void onError(Exception e) {
            Log.e("123","下载错误");
        }
    };

    private void initView(){
        permission = findViewById(R.id.permission);
        state = findViewById(R.id.state);
        console = findViewById(R.id.console);
        progress = findViewById(R.id.progress);
        videoView = findViewById(R.id.video);
        mLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.alert_dialog, null);
        mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        mTimer = mLinearLayout.findViewById(R.id.timer);
        mPreview = mLinearLayout.findViewById(R.id.preview);
        findViewById(R.id.pb_download_progress);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbBroadcastReceiver);
        NetUtil.getInstance().stop();
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
        if (mPlaylist==null){
            Log.e(TAG, "mPlaylist为null " );
            return;
        }
        if (arrayAdapter==null){
            Log.e(TAG, "arrayAdapter为null " );
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
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setLayout(-1,-1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下菜单键显示播放列表
//        if (keyCode == KeyEvent.KEYCODE_MENU) {
//            videoView.pause();
//            if (mAlertDialog == null)
//                creatPlayList();
//            mAlertDialog.show();
//            videoView.setVisibility(View.INVISIBLE);
//        }

        startActivity(new Intent(this,Main2Activity.class));
        return super.onKeyDown(keyCode, event);
    }

    private static void initPlayer() {
        Video video = new Video();
        video.setPlayList();
        if (video.timerList == null || video.timerList.isEmpty())
            state.setVisibility(View.VISIBLE);
    }

    private static void play(String videoString) {
        Log.d("log", "开始播放");
        state.setVisibility(View.INVISIBLE);
        videoView.stopPlayback();//停止播放
        video = videoString.split(",");
        videoIndex = 0;
        videoView.setVideoPath(video[0]);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoIndex++;
                if (videoIndex == video.length)
                    videoIndex = 0;
                try {
                    mp.reset();
                    mp.setDataSource(video[videoIndex]);
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void initProgress(boolean tag){
        if (tag){//为true时代表网络下载，需要自动滚动效果

        }else{//为false时为U盘拷贝，需要节点效果

        }
    }


    public static void sendMessage(String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        Message message = new Message();
        message.what = Integer.parseInt(key);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
