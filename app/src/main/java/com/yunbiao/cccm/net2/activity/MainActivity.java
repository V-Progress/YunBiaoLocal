package com.yunbiao.cccm.net2.activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.janev.easyijkplayer.EasyIJKPlayer;
import com.janev.easyijkplayer.IjkPlayListener;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.net2.event.DataLoadFinishedEvent;
import com.yunbiao.cccm.net2.listener.MainRefreshListener;
import com.yunbiao.cccm.net2.cache.CacheManager;
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
public class MainActivity extends BaseActivity implements MainRefreshListener, IjkPlayListener {
    /*视频播放----*/
    @BindView(R.id.ijk_player)
    EasyIJKPlayer ijkPlayer;

    @BindView(R.id.fl_root)
    FrameLayout flRoot;

    @BindView(R.id.danmaku_view)
    DanmakuView danmakuView;

    public boolean isInsertPlaying = false;//是否有insert正在播放
    public boolean isConfigPlaying = false;//是否有config正在播放
    private boolean priority_flag = false;//true:config优先，false:insert优先

    public static boolean isDataLoadFinished = false;

    protected int setLayout() {
        return R.layout.activity_main;
    }

    private ConsoleDialog consoleDialog;

    private static final String TAG = "MainActivity";

    protected void initView() {
        //先将插播fragment创建出来
        APP.setMainActivity(this);
        MainController.getInstance().registerActivity(this);

        ijkPlayer.initSoLib();//初始化SO库
        ijkPlayer.setNavigation(this);//调整底部栏
        ijkPlayer.enableController(false, false);//设置控制条
        ijkPlayer.enableErrorAlert(false);//关闭错误提示
        ijkPlayer.enableErrorDeleteUri(true);//开启错误删除
        ijkPlayer.setIjkPlayListener(this);//设置播放结束回调
        consoleDialog = new ConsoleDialog(this);
    }

    protected void initData() {
        startActivity(new Intent(this, MenuActivity.class));

        PowerOffTool.getInstance().initPowerData();
        priority_flag = CacheManager.SP.getLaterType() == 2;
        startGetRes();
    }

    //开始请求获取资源
    public void startGetRes() {
        SubtitleLoader.instance().init(MainActivity.this);
        //初始化广告插播，如果有未播完的广告则自动播放
        InsertLoader.getInstance().loadInsert();

        DataLoader.getInstance().get(new DataLoader.AutoLogListener() {
            @Override
            public void loadSingleComplete(final String date, boolean isToday) {
                super.loadSingleComplete(date, isToday);

                if (!isToday) {
                    return;
                }

                //下载今天的资源
                Downloader.getInstance().check(date,new Downloader.AutoLogDownListener() {
                    @Override
                    public void onReadyProgram(String date, boolean hasProgram) {
                        //有资源则加载，没有则等待下载
                        if (hasProgram) {
                            Log.e(TAG, "onReadyProgram: 111111111111");
                            ProgramLoader.getInstance().loadProgram(new ProgramLoader.AutoLogProgramListener());
                        }
                    }

                    @Override
                    public void onComplete(ItemBlock itemBlock) {
                        super.onComplete(itemBlock);
                        //下载完一个后就重新加载
                        Log.e(TAG, "onReadyProgram: 111111111111");
                        ProgramLoader.getInstance().loadProgram(new ProgramLoader.AutoLogProgramListener());
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

    /*
     * 初始化播放数据
     */
//    @Override
    public void initPlayData() {
        Log.e(TAG, "initPlayData: 22222222222222");
        ProgramLoader.getInstance().loadProgram(new ProgramLoader.AutoLogProgramListener());
    }

    /*
    * 初始化插播数据
    * */
    public void initInsertData() {
        InsertLoader.getInstance().loadInsert();
    }

    @Override
    public void clearPlayData() {
        isInsertPlaying = false;
        isConfigPlaying = false;
        priority_flag = false;
        stop();
    }

    //更新优先级标签
    @Override
    public void updateLayerType(boolean isConfigOnTop) {
        if (priority_flag == isConfigOnTop) {
            return;
        }
        priority_flag = isConfigOnTop;
        startGetRes();
    }

    //常规资源播放
    @Override
    public void startConfigPlay(List<String> videoList) {
        Log.e("123", "startConfigPlay: -----开始config");
        //如果标签为insert优先，且insert正在播放，则不开始播放普通资源
        if (!priority_flag && isInsertPlaying) {
            Log.e("123", "startConfigPlay: -----开始config失败");
            return;
        }
        Log.e("123", "startConfigPlay: -----开始config成功");
        isConfigPlaying = true;
        play(videoList);
    }

    //常规资源停止
    @Override
    public void stopConfigPlay() {
        isConfigPlaying = false;
        Log.e("123", "stopConfigPlay: -----停止config");
        //如果标签为insert优先，且insert正在播放，则不停止播放器
        if (!priority_flag && isInsertPlaying) {
            Log.e("123", "stopConfigPlay: -----停止失败");
            return;
        }
        Log.e("123", "stopConfigPlay: -----停止成功");
        stop();
        //普通资源结束后解析插播资源
        initInsertData();
    }

    //插播资源播放
    @Override
    public void startInsert(final boolean cycle, final List<String> videoList, final boolean isAdd) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (priority_flag && isConfigPlaying) {
                    return;
                }
                isInsertPlaying = true;
                ijkPlayer.enableListLoop(cycle);
                play(videoList);
            }
        }, 2000);
    }

    //插播资源停止
    @Override
    public void stopInsert() {
        isInsertPlaying = false;
        if (priority_flag && isConfigPlaying) {
            return;
        }
        stop();
        //插播资源结束后解析普通资源
        initPlayData();
    }

    @Override
    public void onListCompletion() {
        if (isInsertPlaying) {
            isInsertPlaying = false;
            initPlayData();
        }
    }

    //-----常规方法----------------------------------------------------------------
    private void stop() {
        if (ijkPlayer != null) {
            ijkPlayer.stop();
        }
    }

    private void play(final List<String> videoList) {
        ijkPlayer.setVideoList(videoList);
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
                ijkPlayer.fastForword();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                ijkPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER://按下中键
                ijkPlayer.toggle();
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
                MenuActivity menuActivity = APP.getMenuActivity();
                if (menuActivity == null || !menuActivity.isForeground()) {
                    startActivity(new Intent(this, MenuActivity.class));
                }
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
        ijkPlayer.resume();
        DanmakuManager.getInstance().resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SubtitleLoader.instance().onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (consoleDialog != null) {
            consoleDialog.dismiss();
        }
        ijkPlayer.pause();
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
        return ijkPlayer != null && ijkPlayer.isPlaying();
    }

    public Long getVideoCurrTime() {
        if (ijkPlayer != null && ijkPlayer.isPlaying()) {
            return ijkPlayer.getCurrentPosition();
        }
        return 0L;
    }

    public String getCurrPlayVideo() {
        return ijkPlayer.getCurrentVideo();
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
