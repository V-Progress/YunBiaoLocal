package com.yunbiao.cccm.net2.activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.janev.easyijkplayer.EasyPlayer;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.net2.InsertPlayer;
import com.yunbiao.cccm.net2.SystemVersion;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.net2.event.DataLoadFinishedEvent;
import com.yunbiao.cccm.net2.listener.MainRefreshListener;
import com.yunbiao.cccm.net2.ConsoleDialog;
import com.yunbiao.cccm.net2.DataLoader;
import com.yunbiao.cccm.net2.Downloader;
import com.yunbiao.cccm.net2.ProgramLoader;
import com.yunbiao.cccm.net2.SubtitleLoader;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.utils.NetUtil;
import com.yunbiao.cccm.net2.DanmakuManager;
import com.yunbiao.cccm.net2.control.PowerOffTool;
import com.yunbiao.cccm.net2.listener.OnXmppConnListener;
import com.yunbiao.cccm.net2.utils.network.NetStateChangeObserver;
import com.yunbiao.cccm.net2.utils.network.NetStateChangeReceiver;
import com.yunbiao.cccm.net2.utils.network.NetworkType;
import com.yunbiao.cccm.net2.utils.network.NetworkUtil;
import com.yunbiao.cccm.xmpp.PnServerController;
import com.yunbiao.cccm.net2.InsertLoader;
import com.yunbiao.cccm.net2.utils.DeleteResUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import master.flame.danmaku.ui.widget.DanmakuView;

// TODO: 2019/3/8
public class MainActivity extends BaseActivity implements MainRefreshListener{
    private static final String TAG = "MainActivity";
    private ConsoleDialog consoleDialog;
    public static boolean isDataLoadFinished;
    @BindView(R.id.ep)
    EasyPlayer easyPlayer;

    @BindView(R.id.fl_root)
    FrameLayout flRoot;

    @BindView(R.id.danmaku_view)
    DanmakuView danmakuView;

    protected int setLayout() {
        return R.layout.activity_main;
    }

    protected void initView() {
        //先将插播fragment创建出来
        APP.setMainActivity(this);
        MainController.getInstance().registerActivity(this);

        consoleDialog = new ConsoleDialog(this);

        SubtitleLoader.instance().init(MainActivity.this);

        InsertPlayer.getInstance().init(this);
        InsertPlayer.getInstance().setInsertCallback(new InsertPlayer.InsertCallback() {
            @Override
            public void onShow() {
                easyPlayer.pause();
            }

            @Override
            public void onHide() {
                easyPlayer.resume();
            }
        });
    }

    protected void initData() {
        startActivity(new Intent(this, MenuActivity.class));

        NetStateChangeReceiver.registerReceiver(this);

        PowerOffTool.getInstance().initPowerData();

        Log.e(TAG, "initData: 加载节目");
        ProgramLoader.getInstance().loadProgram( new Runnable() {
            @Override
            public void run() {
                boolean networkAvalible = NetworkUtil.isNetworkAvalible(MainActivity.this);
                Log.e(TAG, "run: 加载完成后检测网络状态：" + networkAvalible);

                if(!networkAvalible){
                    ConsoleDialog.addTextLog("网络不可用，等待网络");
                    return;
                }
                startGetRes();
            }
        });
    }

    private NetStateChangeObserver netStateChangeObserver = new NetStateChangeObserver() {
        @Override
        public void onNetDisconnected() {
            ConsoleDialog.addTextLog("网络已断开");
        }

        @Override
        public void onNetConnected(NetworkType networkType) {
            ConsoleDialog.addTextLog("网络已连接");
            boolean networkAvalible = NetworkUtil.isNetworkAvalible(MainActivity.this);
            if(networkAvalible){
                if(!DataLoader.isCurrDataComplete()){//如果当前日期的数据已加载完毕
                    startGetRes();
                } else {

                }
            } else {
                ConsoleDialog.addTextLog("无法连接网络，请检查");
            }
        }
    };

    //开始请求获取资源
    public void startGetRes() {
        DataLoader.getInstance().get(autoLogListener);

        //检查过期文件
        DeleteResUtil.checkExpireFile();
    }

    //今天有节目的情况，onFinish的时候不加载明天数据
    private boolean isTodayHasProgram = false;

    /***
     * 获取节目列表的回调
     */
    private DataLoader.AutoLogListener autoLogListener = new DataLoader.AutoLogListener() {
        @Override
        public void loadSingleComplete(final String date, boolean isToday, boolean hasProgram) {
            super.loadSingleComplete(date, isToday, hasProgram);
            if (!isToday) {
                return;
            }

            isTodayHasProgram = hasProgram;

            Log.e(TAG, "loadSingleComplete: 11111111111111" );
            //下载今天的资源
            Downloader.getInstance().check(date,autoLogDownListener);
        }

        @Override
        public void failed(String date, boolean isToday, String type) {
            //检查失败的时候加载一次本地节目
            if(isToday){
//                ProgramLoader.getInstance().loadProgram();
            }
        }

        @Override
        public void loadFinished() {
            super.loadFinished();
            isDataLoadFinished = true;
            EventBus.getDefault().post(new DataLoadFinishedEvent());

            //今天没有节目，结束的时候加载全部
            if(!isTodayHasProgram){
                Log.e(TAG, "loadFinished: 节目全部加载完毕，且今天没有节目，代表下载结束的地方不会检查，所以此处检查");
                //下载结束后开始无限下载明天的数据
                Downloader.getInstance().checkTomm(new Downloader.AutoLogDownListener() {
                    @Override
                    public void onFinished(String date) {
                        super.onFinished(date);
                        //明天的数据下载结束后开始循环往后检测
                        Downloader.getInstance().autoCheck();
                    }
                });
            } else {
                Log.e(TAG, "loadFinished: 节目全部加载完毕，且今天有节目，代表下载结束的地方会检查，所以此处不检查" );
            }
        }
    };

    /***
     * 自动下载的回调
     */
    private Downloader.AutoLogDownListener autoLogDownListener = new Downloader.AutoLogDownListener() {
        @Override
        public void onReadyProgram(String date, boolean hasProgram) {
            Log.e(TAG, "onReadyProgram: " + date);
            //有资源则加载，没有则等待下载
            if (hasProgram) {
                Log.e(TAG, "onReadyProgram: 111111111111");
                ProgramLoader.getInstance().loadProgram();
            }
        }

        @Override
        public void onComplete(ItemBlock itemBlock) {
            super.onComplete(itemBlock);
            //下载完一个后就重新加载
            Log.e(TAG, "onReadyProgram: 111111111111");
            ProgramLoader.getInstance().loadProgram();
        }

        @Override
        public void onFinished(String date) {
            super.onFinished(date);

            //今天的节目先下载完 且 全部节目后加载完的情况，判断今天是否有节目，有的话，代表加载结束时没有开始下载
            if(isTodayHasProgram){
                Log.e(TAG, "onFinished: 今天有节目，代表加载结束的地方不会开始检查，从此处开始检查" );
                //下载结束后开始无限下载明天的数据
                Downloader.getInstance().checkTomm(new Downloader.AutoLogDownListener() {
                    @Override
                    public void onFinished(String date) {
                        super.onFinished(date);
                        //明天的数据下载结束后开始循环往后检测
                        Downloader.getInstance().autoCheck();
                    }
                });
            } else {
                Log.e(TAG, "onFinished: 今天没有节目，代表加载结束的地方会开始检查，此处不检查");
            }
        }
    };

    //-------播放器控制----------------------------------------------------------------

    //更新优先级标签
    @Override
    public void updateLayerType(boolean isConfigOnTop) {
        SystemVersion.setIsInsertFirst(!isConfigOnTop);

        if(isDataLoadFinished){
            ProgramLoader.getInstance().loadProgram();
            InsertLoader.getInstance().loadInsert();
        }
    }

    //常规资源播放
    @Override
    public void startConfigPlay(List<String> videoList) {
        easyPlayer.setVisibility(View.VISIBLE);
        easyPlayer.setCycle(true);
        easyPlayer.setVideos(videoList);
    }

    //常规资源停止
    @Override
    public void stopConfigPlay() {
        easyPlayer.stop();
        easyPlayer.setVisibility(View.GONE);
    }

    /*===========页面控件相关=====================================================================
     * 初始化播放列表
     */

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                easyPlayer.fastForward();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                easyPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER://按下中键
                easyPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.e(TAG, "onKeyDown: 打开控制台");
                consoleDialog.show();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.e(TAG, "onKeyDown: 关闭控制台");
                consoleDialog.dismiss();
                break;
            case KeyEvent.KEYCODE_MENU:
                startActivity(new Intent(this, MenuActivity.class));
                return false;
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                } else {
                    APP.exit();
                }
                return false;
        }
        return true;
    }

    private long firstTime = 0;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DanmakuManager.getInstance().configurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SubtitleLoader.instance().onResume();
        InsertPlayer.getInstance().resume();
        easyPlayer.resume();
        DanmakuManager.getInstance().resume();
        NetStateChangeReceiver.registerObserver(netStateChangeObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SubtitleLoader.instance().onStop();
        InsertPlayer.getInstance().pause();
        if (consoleDialog != null) {
            consoleDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        easyPlayer.pause();
        DanmakuManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetStateChangeReceiver.unRegisterReceiver(this);
        DanmakuManager.getInstance().destroy();
        NetUtil.getInstance().stop();
        PnServerController.stopXMPP();
        APP.exit();
    }

    public boolean isVideoPlaying() {
        return easyPlayer.isPlaying();
    }

    public Long getVideoCurrTime() {
        return easyPlayer.getCurrentPosition();
    }

    public String getCurrPlayVideo() {
        return easyPlayer.getCurrentPath();
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


    /*// TODO: 2019/1/27 弹幕测试
    if(!Const.SYSTEM_CONFIG.IS_PRO){
        DanmakuManager.getInstance().init(this, danmakuView, new DanmakuManager.DanmakuReadyListener() {
            @Override
            public void Ready() {
                DanmakuManager.getInstance().generateSomeDanmaku();
            }
        });
    }*/
}
