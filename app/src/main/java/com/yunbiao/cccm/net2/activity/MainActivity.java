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
import com.yunbiao.cccm.xmpp.PnServerController;
import com.yunbiao.cccm.net2.InsertLoader;
import com.yunbiao.cccm.net2.utils.DeleteResUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import master.flame.danmaku.ui.widget.DanmakuView;

// TODO: 2019/3/8
public class MainActivity extends BaseActivity implements MainRefreshListener{
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

    private ConsoleDialog consoleDialog;

    private static final String TAG = "MainActivity";

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

        PowerOffTool.getInstance().initPowerData();

        startGetRes();
    }

    //开始请求获取资源
    public void startGetRes() {
        DataLoader.getInstance().get(new DataLoader.AutoLogListener() {
            @Override
            public void loadSingleComplete(final String date, boolean isToday) {
                super.loadSingleComplete(date, isToday);

                if (!isToday) {
                    return;
                }

                //初始化广告插播，如果有未播完的广告则自动播放
                InsertLoader.getInstance().loadInsert();

                Log.e(TAG, "loadSingleComplete: 11111111111111" );
                //下载今天的资源
                Downloader.getInstance().check(date,new Downloader.AutoLogDownListener() {
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
                        //下载结束后开始无限下载明天的数据
                        Downloader.getInstance().checkTomm(new Downloader.AutoLogDownListener() {
                            @Override
                            public void onFinished(String date) {
                                super.onFinished(date);
                                //明天的数据下载结束后开始循环往后检测
                                Downloader.getInstance().autoCheck();
                            }
                        });
                    }
                },true);
            }

            @Override
            public void failed(String date, boolean isToday, String type) {
                //检查失败的时候加载一次本地节目
                if(isToday){
                    ProgramLoader.getInstance().loadProgram();
                }
            }

            @Override
            public void loadFinished() {
                super.loadFinished();
                isDataLoadFinished = true;
                EventBus.getDefault().post(new DataLoadFinishedEvent());

            }
        });

        //检查过期文件
        DeleteResUtil.checkExpireFile();
    }

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
