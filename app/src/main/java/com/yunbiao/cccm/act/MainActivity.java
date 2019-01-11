package com.yunbiao.cccm.act;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.download.InsertManager;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.act.base.BaseActivity;
import com.yunbiao.cccm.br.USBBroadcastReceiver;
import com.yunbiao.cccm.devicectrl.PowerOffTool;
import com.yunbiao.cccm.download.ResourceManager;
import com.yunbiao.cccm.netcore.OnXmppConnListener;
import com.yunbiao.cccm.netcore.PnServerController;
import com.yunbiao.cccm.resolve.VideoDataResolver;
import com.yunbiao.cccm.utils.DeleteResUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.SystemInfoUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.utils.TimerExecutor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import rjsv.circularview.CircleView;

public class MainActivity extends BaseActivity implements MainRefreshListener {
    /*视频播放----*/
    @BindView(R.id.video_view)
    VideoView vv;
    @BindView(R.id.permission)
    TextView permission;
    @BindView(R.id.state)
    TextView state;

    /*加载框-----*/
    @BindView(R.id.fl_loading_main)
    LinearLayout llLoadingMain;
    @BindView(R.id.pb_loading_main)
    ProgressBar pbLoadingMain;
    @BindView(R.id.tv_loading_main)
    TextView tvLoadingMain;

    /*更新下载进度条---*/
    @BindView(R.id.ll_update_area)
    LinearLayout llUpdateArea;
    @BindView(R.id.fl_root)
    FrameLayout flRoot;
    @BindView(R.id.pb_update)
    ProgressBar pbUpdate;

    /*资源下载进度条---*/
    @BindView(R.id.tv_console_main)
    TextView tvConsoleMain;
    @BindView(R.id.sv_console_main)
    ScrollView svConsoleMain;
    @BindView(R.id.progress_child_main)
    CircleView progressChildMain;
    @BindView(R.id.progress_parent_main)
    CircleView progressParentMain;
    @BindView(R.id.tv_num_main)
    TextView tvNumMain;
    @BindView(R.id.tv_progress_main)
    TextView tvProgressMain;
    @BindView(R.id.ll_console_main)
    LinearLayout llConsoleMain;

    private USBBroadcastReceiver usbBroadcastReceiver;//USB监听广播
    private List<String> playLists;
    private static int videoIndex;//当前视频在列表中处于的位置
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //初始化播放器
                initPlayer();

                //初始化播放数据
                //initPlayData(true);// TODO: 2018/12/30 初始化本地数据

                //连接XMPP
                PnServerController.startXMPP(MainActivity.this);

                //初始化自动开关机数据
                PowerOffTool.getInstance().initPowerData();

                //初始化播放数据
                ResourceManager.getInstance().initResData();

                //初始化广告插播，如果有未播完的广告则自动播放
                InsertManager.getInstance(MainActivity.this).initInsertData();
            }
        }, 3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DeleteResUtil.removeExpireFile();
            }
        }, 10000);
    }

    //-------播放器控制----------------------------------------------------------------
    @Override
    public void initPlayer() {
//        vv.setOnPreparedListener(preparedListener);//准备完毕监听
        vv.setOnErrorListener(errorListener);//播放错误监听
        vv.setOnInfoListener(infoListener);//播放信息监听
        vv.setOnCompletionListener(completionListener);
    }

    /*
     * 初始化播放数据
     */
    @Override
    public void initPlayData(boolean isRemote) {
        VideoDataResolver videoDataResolve = new VideoDataResolver();
        if (isRemote) {
            videoDataResolve.resolvePlayLists();
        } else {
            videoDataResolve.resolveLocalResource();
        }

        if (videoDataResolve.getPlayList() == null || videoDataResolve.getPlayList().isEmpty()) {
            if (isInsertPlaying) {
                LogUtil.E("广告正在播放，不显示state");
                return;
            }
            state.setVisibility(View.VISIBLE);
        }
    }

    //常规资源播放
    @Override
    public void startPlay(List<String> videoList) {
        if (isInsertPlaying) {
            LogUtil.E("广告正在播放，不执行startPlay");
            return;
        }
        play(videoList);
    }

    //常规资源停止
    @Override
    public void stopPlay() {
        if (isInsertPlaying) {
            LogUtil.E("广告正在播放，不执行stopPlay");
            return;
        }
        if (playLists != null) {
            playLists.clear();
        }
        stop();
    }

    //插播资源播放
    @Override
    public void startInsert(final boolean isCycle, final List<String> videoList) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.isInsertPlaying = true;
                MainActivity.this.isCycle = isCycle;
                LogUtil.E("开始播放插播：" + videoList.toString());
                LogUtil.E("是否轮播：" + MainActivity.this.isCycle);
                play(videoList);
            }
        }, 2000);
    }

    //插播资源停止
    @Override
    public void stopInsert() {
        isCycle = true;
        isInsertPlaying = false;
        LogUtil.E("停止播放插播");
        stop();
        initPlayData(true);
    }



    //------进度条控制----------------------------------------------------------------
    /*
    * 打开控制台
    */
    @Override
    public void openConsole() {
        llConsoleMain.setVisibility(View.VISIBLE);
    }

    /*
     * 关闭控制台
     */
    @Override
    public void closeConsole() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                llConsoleMain.setVisibility(View.GONE);
                progressParentMain.setMaximumValue(0);
                tvConsoleMain.setText("");
            }
        }, 3000);
    }

    @Override
    public void updateConsole(String msg) {
        String lastStr = tvConsoleMain.getText().toString();
        tvConsoleMain.setText(lastStr + "\n" + msg);
        svConsoleMain.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
    }

    @Override
    public void initProgress(int parentMax) {
        tvNumMain.setText(0+"/"+parentMax);
        progressParentMain.setMaximumValue(parentMax);
        progressChildMain.setMaximumValue(100);
    }

    @Override
    public void updateChildProgress(int pg) {
        progressChildMain.setProgressValue(pg);
        tvProgressMain.setText(pg+"%");
    }

    @Override
    public void updateParentProgress(int pg) {
        progressParentMain.setProgressValue(pg);

        String num = tvNumMain.getText().toString();
        if(TextUtils.isEmpty(num)){
            return;
        }
        String[] split = num.split("/");
        if(split.length < 2){
            return;
        }
        tvNumMain.setText( pg + "/" +split[1]);
    }

    @Override
    public void openLoading(String loadingMsg) {
        tvLoadingMain.setText(loadingMsg);
        pbLoadingMain.setInterpolator(new AccelerateDecelerateInterpolator());
        llLoadingMain.setVisibility(View.VISIBLE);
    }

    @Override
    public void closeLoading() {
        llLoadingMain.setVisibility(View.GONE);
    }

    //-----常规方法----------------------------------------------------------------

    /***
     * 获取可播放内容的总数
     */
    public int getPlayCount() {
        int count = 0;
        if (playLists != null && playLists.size() > 0) {
            count = playLists.size();
        }
        return count;
    }

    private void stop() {
        if (vv == null) {
            return;
        }
        vv.stopPlayback();
        vv.setVisibility(View.GONE);
        state.setVisibility(View.VISIBLE);
    }

    private void play(final List<String> videoList) {
        videoIndex = 0;
        playLists = videoList;

        state.setVisibility(View.INVISIBLE);
        if (vv.isPlaying()) {
            vv.stopPlayback();
        }

        vv.setVideoPath(playLists.get(videoIndex));
        vv.setVisibility(View.VISIBLE);
        vv.start();
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
    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtil.E("当前错误代码：" + what + "-----" + extra);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("播放失败");
            alertDialog.setMessage("播放地址解析失败\n\n错误代码:" + what + " - " + extra + "\n\n本窗口于10秒后关闭");
            alertDialog.show();

            TimerExecutor.getInstance().delayExecute(10000, new TimerExecutor.OnTimeOutListener() {
                @Override
                public void execute() {
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                            stopInsert();
                        }
                    });
                }
            });
            return true;
        }
    };

    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                openLoading("正在缓冲");//打开加载
            } else {
                closeLoading();//关闭加载
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
                if (!isCycle) {
                    LogUtil.E("不轮播");
                    stopInsert();
                    return;
                }
            }
            try {
                vv.stopPlayback();
                vv.setVideoPath(playLists.get(videoIndex));
                vv.start();
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
        vv.resume();
        vv.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vv.pause();
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
//            updateProgress(progress, progressStr); //// TODO: 2019/1/9
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

    public boolean isVideoPlaying() {
        return vv != null && vv.isPlaying();
    }

    public Long getVideoCurrTime() {
        if (vv != null && vv.isPlaying()) {
            return Long.valueOf(vv.getCurrentPosition());
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
