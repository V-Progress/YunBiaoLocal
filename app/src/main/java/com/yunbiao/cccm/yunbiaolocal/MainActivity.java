package com.yunbiao.cccm.yunbiaolocal;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.yunbiaolocal.io.Video;
import com.yunbiao.cccm.yunbiaolocal.sd.SDUtil;
import com.yunbiao.cccm.yunbiaolocal.utils.LogUtil;
import com.yunbiao.cccm.yunbiaolocal.utils.ProgressUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements SDUtil.CheckSDListener {
    private static final String TAG = "MainActivity";

    private TextView permission;
    private static TextView state;
    private static TextView console;
    private static ProgressBar progress;
    private static EasyIJKPlayer ijkPlayer;

    private static String[] tempVideos;
    private AlertDialog alertDialog;

    private static int lineNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        com.yunbiao.cccm.APP.addActivity(this);
        APP.setMainActivity(this);
        ProgressUtil.instance().init(this);

        initView();

        try {//是否授权
            long expire = new SimpleDateFormat("yyyyMMdd").parse("20250101").getTime();
            if (expire < System.currentTimeMillis()) {
                permission.setVisibility(View.VISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SDUtil.init(this, this).checkSD();
    }

    private void initView(){
        permission = findViewById(R.id.permission);
        state = findViewById(R.id.state);
        console = findViewById(R.id.console);
        progress = findViewById(R.id.progress);

        ijkPlayer = findViewById(R.id.ijk_player);
        ijkPlayer.initSoLib();
        ijkPlayer.enableController(true,false);
        ijkPlayer.enableListLoop(true);
        ijkPlayer.setNavigation(this);
    }

    @Override
    public void sdCanUsed(boolean isCanUsed) {
        LogUtil.E("sdCanUsed:"+isCanUsed);
        if (isCanUsed) {//可用
            dissMissAlert();
            setPlay();
        } else {//不可用
            sendMessage("10", null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlert("SD卡不可用\n请检测是否插入SD卡  或  SD卡是否可读写");
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDUtil.instance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkPlayer.resume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkPlayer.pause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkPlayer.release();
        com.yunbiao.cccm.APP.removeActivity(this);
    }

    //========控制区================================================================
    private static void setPlay() {
        Video video = new Video();
        video.initPlayList();
//        if (video.timerList == null || video.timerList.isEmpty())
//            state.setVisibility(View.VISIBLE);

    }


    private static void play(String videoString) {
        String[] split = videoString.split(",");
        if (split == null || split.length <=0 || Arrays.equals(tempVideos, split)) {
            return;
        }
        tempVideos = split;
        state.setVisibility(View.INVISIBLE);
        ijkPlayer.setVideoList(Arrays.asList(tempVideos));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                ijkPlayer.fastForword();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                ijkPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                ijkPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_MENU:
                startActivity(new Intent(this,PlayListActivity.class));
                break;
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                } else {
                    com.yunbiao.cccm.APP.exit();
                }
                return false;
        }

        return super.onKeyDown(keyCode, event);
    }
    private long firstTime = 0;


    public static void sendMessage(String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        Message message = new Message();
        message.what = Integer.parseInt(key);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String value = msg.getData().getString(String.valueOf(msg.what));
            switch (msg.what) {
                case (1):
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
                case (3):
                    progress.setMax(Integer.parseInt(value));
                    progress.setVisibility(View.VISIBLE);
                    break;
                case (4):
                    progress.setProgress(progress.getProgress() + 1);
                    break;
                case (0):
                    setPlay();
                    progress.setVisibility(View.INVISIBLE);
                    progress.setProgress(0);
                    console.setVisibility(View.INVISIBLE);
                    console.setText(null);
                    lineNumber = 0;
                    break;
                case (11):
                    play(value);
                    break;
                case (10):
                    Log.d("log", "停止播放");
                    ijkPlayer.stop();//停止播放
            }
        }
    };

    private void showAlert(final String message) {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle("警告");
        alertBuilder.setMessage(message);
        alertDialog = alertBuilder.create();
        if (!MainActivity.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void dissMissAlert() {
        if ((!this.isFinishing()) && alertDialog != null) {
            alertDialog.dismiss();
        }
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
