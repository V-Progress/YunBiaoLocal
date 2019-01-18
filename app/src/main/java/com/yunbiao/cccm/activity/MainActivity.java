package com.yunbiao.cccm.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.netcore.NetClient;
import com.yunbiao.cccm.resource.InsertManager;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.base.BaseActivity;
import com.yunbiao.cccm.broadcast.USBBroadcastReceiver;
import com.yunbiao.cccm.control.PowerOffTool;
import com.yunbiao.cccm.resource.ResourceManager;
import com.yunbiao.cccm.netcore.OnXmppConnListener;
import com.yunbiao.cccm.netcore.PnServerController;
import com.yunbiao.cccm.resolve.VideoDataResolver;
import com.yunbiao.cccm.utils.DeleteResUtil;
import com.yunbiao.cccm.utils.DialogUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.SystemInfoUtil;
import com.yunbiao.cccm.utils.TimerUtil;
import com.yunbiao.cccm.utils.ToastUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import master.flame.danmaku.ui.widget.DanmakuView;
import rjsv.circularview.CircleView;

public class MainActivity extends BaseActivity implements MainRefreshListener {
    /*视频播放----*/
    @BindView(R.id.video_view)
    VideoView vv;
    @BindView(R.id.dm_view)
    DanmakuView dmView;

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
    @BindView(R.id.tv_speed_main)
    TextView tvSpeed;

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
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        registerReceiver(usbBroadcastReceiver, intentFilter);

//        DanmuUtil.getInstance(this).init(dmView);//初始化弹幕
    }

    protected void initData() {
        //初始化播放器
        initPlayer();
        //初始化播放数据
//      initPlayData(true);// TODO: 2018/12/30 初始化本地数据
        //连接XMPP
        PnServerController.startXMPP(MainActivity.this);

        TimerUtil.delayExecute(7000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                //初始化自动开关机数据
                PowerOffTool.getInstance().initPowerData();
            }
        });

        if(checkPermission()){
            startGetRes();
        }

        // TODO: 2019/1/17 暂时屏蔽SD卡检测模块
        //检测SD卡
        /*SDUtil.instance().init(this, new SDUtil.CheckSDListener() {
            @Override
            public void sdCanUsed(boolean isCanUsed) {
                if(isCanUsed){
                    DialogUtil.getInstance().dismissError();
                    startGetRes();
                }else{
                    ResourceManager.getInstance().cancel();
                    DialogUtil.getInstance().showError(MainActivity.this,"读取错误","请插入SD卡\n并确保SD卡可正常使用");
                }
            }
        }).checkSD();*/

    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获得SD权限的监听
        SDUtil.instance().onActivityResult(this,requestCode,resultCode,data);
    }*/

    //自定义REQUEST_CODE
    private int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;

    //检测权限
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                LogUtil.D("123", "正在申请权限");
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                return false;
            } else {
                LogUtil.D("123","已有权限");
                return true;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.D("123", "已取得权限");
                startGetRes();
            } else {
                LogUtil.D("123", "未取得权限");
                ToastUtil.showLong(this,"请允许权限");
            }
        }
    }

    //开始请求获取资源
    public void startGetRes() {
        TimerUtil.delayExecute(3000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                //初始化播放数据
                ResourceManager.getInstance().initResData();

                //初始化广告插播，如果有未播完的广告则自动播放
                InsertManager.getInstance(MainActivity.this).initInsertData();

                //删除过期文件
                DeleteResUtil.removeExpireFile();
            }
        });
    }

    //-------播放器控制----------------------------------------------------------------
    @Override
    public void initPlayer() {
        vv.setOnErrorListener(errorListener);//播放错误监听
        vv.setOnInfoListener(infoListener);//播放信息监听
        vv.setOnCompletionListener(completionListener);
        svConsoleMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                svConsoleMain.post(new Runnable() {
                    @Override
                    public void run() {
                        svConsoleMain.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    /*
     * 初始化播放数据
     */
    @Override
    public void initPlayData(boolean isRemote) {
        VideoDataResolver resolver = VideoDataResolver.getInstance();
        if (isRemote) {
            resolver.initPlayList();
        } else {
            resolver.resolveLocalResource();
        }

        if (resolver.getPlayList() == null || resolver.getPlayList().isEmpty()) {
            if (isInsertPlaying) {
                LogUtil.E("广告正在播放，不显示state");
                return;
            }
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
    }

    @Override
    public void initProgress(int parentMax) {
        tvNumMain.setText(0 + "/" + parentMax);
        progressParentMain.setMaximumValue(parentMax);
        progressChildMain.setMaximumValue(100);
    }

    @Override
    public void updateChildProgress(int pg) {
        progressChildMain.setProgressValue(pg);
        tvProgressMain.setText(pg + "%");
    }

    @Override
    public void updateParentProgress(int pg) {
        progressParentMain.setProgressValue(pg);

        String num = tvNumMain.getText().toString();
        if (TextUtils.isEmpty(num)) {
            return;
        }
        String[] split = num.split("/");
        if (split.length < 2) {
            return;
        }
        tvNumMain.setText(pg + "/" + split[1]);
    }

    @Override
    public void updateDownloadSpeed(String speed) {
        tvSpeed.setText(speed);
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
    }

    private void play(final List<String> videoList) {
        videoIndex = 0;
        playLists = videoList;

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

            DialogUtil.getInstance().showError(MainActivity.this
                    , "播放失败"
                    , "播放地址解析失败\n\n错误代码:" + what + " - " + extra + "\n\n本窗口于10秒后关闭"
                    , 10000
                    , new Runnable() {
                        @Override
                        public void run() {
                            stopInsert();
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
        NetClient.getInstance().stop();
        PnServerController.stopXMPP();
        APP.exit();
    }

    //下载更新的监听，外部静态调用
    public NetClient.OnDownLoadListener downloadUpdateListener = new NetClient.OnDownLoadListener() {
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
