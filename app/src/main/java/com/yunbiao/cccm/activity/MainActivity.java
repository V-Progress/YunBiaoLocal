package com.yunbiao.cccm.activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.janev.easyijkplayer.EasyIJKPlayer;
import com.janev.easyijkplayer.IjkPlayListener;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.base.BaseActivity;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.listener.MainRefreshListener;
import com.yunbiao.cccm.net.resource.InsertTextManager;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.sd.HighVerSDController;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.sd.SDManager;
import com.yunbiao.cccm.utils.ConsoleUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.net.resource.DanmakuManager;
import com.yunbiao.cccm.local.LocalManager;
import com.yunbiao.cccm.utils.VideoProgressUtil;
import com.yunbiao.cccm.net.control.PowerOffTool;
import com.yunbiao.cccm.net.listener.OnXmppConnListener;
import com.yunbiao.cccm.xmpp.PnServerController;
import com.yunbiao.cccm.net.resource.InsertVideoManager;
import com.yunbiao.cccm.net.resource.ResourceManager;
import com.yunbiao.cccm.utils.DeleteResUtil;
import com.yunbiao.cccm.utils.DialogUtil;
import com.yunbiao.cccm.utils.TimerUtil;

import java.util.List;

import butterknife.BindView;
import master.flame.danmaku.ui.widget.DanmakuView;

// TODO: 2019/3/8
public class MainActivity extends BaseActivity implements MainRefreshListener, SDManager.CheckSDListener, IjkPlayListener {
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

    protected int setLayout() {
        return R.layout.activity_main;
    }

    protected void initView() {
        //先将插播fragment创建出来
        APP.setMainActivity(this);
//        VideoProgressUtil.instance().init(this);
        ConsoleUtil.instance().init(this);
        MainController.getInstance().registerActivity(this);

        ijkPlayer.initSoLib();//初始化SO库
        ijkPlayer.setNavigation(this);//调整底部栏
        ijkPlayer.enableController(true, false);//设置控制条
        ijkPlayer.enableErrorAlert(false);//关闭错误提示
        ijkPlayer.enableErrorDeleteUri(true);//开启错误删除
        ijkPlayer.setIjkPlayListener(this);//设置播放结束回调
    }

    protected void initData() {
        //开启软件守护服务
        startService(new Intent(this, MyProtectService.class));

        //连接XMPP
        PnServerController.startXMPP(MainActivity.this);

        PowerOffTool.getInstance().initPowerData();
        //检测SD卡
        SDManager.instance().init(this, this).checkSD();
    }

    @Override
    public void sdCanUsed(boolean isCanUsed) {
        //不论SD卡是否可用，如果当前是菜单界面，都关掉
        MenuActivity menuActivity = APP.getMenuActivity();
        if (menuActivity != null && menuActivity.isForeground()) {
            menuActivity.finish();
        }
        //SD卡不可用，停止一切操作并显示Alert
        if (!isCanUsed) {
            stop();
            stopInsert();
            stop();
            ResourceManager.getInstance().cancel();
            DialogUtil.getInstance().showError(MainActivity.this, "读取错误", "请插入SD卡\n并确保SD卡可正常使用",true);
            return;
        }

        //关闭Alert
        DialogUtil.getInstance().dismissError();
        if (CacheManager.SP.getMode() == 0) {
            priority_flag = CacheManager.SP.getLaterType() == 2;
            startGetRes();
        } else {
            LocalManager.getInstance().initData();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDManager.instance().onActivityResult(requestCode, resultCode, data);
    }

    //开始请求获取资源
    public void startGetRes() {
        TimerUtil.delayExecute(1000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                //初始化播放数据
                ResourceManager.getInstance().initNetData();

                //初始化广告插播，如果有未播完的广告则自动播放
                InsertVideoManager.getInstance().initData();

                //初始化字幕
                InsertTextManager.instance(MainActivity.this).initTXT();

                //删除过期文件
                DeleteResUtil.checkExpireFile();
            }
        });
    }

    //-------播放器控制----------------------------------------------------------------

    /*
     * 初始化播放数据
     */
//    @Override
    public void initPlayData() {
        if (HighVerSDController.instance().isSDCanUsed() || LowVerSDController.instance().isSDCanUsed()) {
            if (CacheManager.SP.getMode() == 0) {
                ResourceManager.getInstance().loadData();
            } else {
                LocalManager.getInstance().initData();
            }
        }
    }

    /*
    * 初始化插播数据
    * */
    public void initInsertData() {
        if (!HighVerSDController.instance().isSDCanUsed()) {
            return;
        }
        if (CacheManager.SP.getMode() == 0) {
            InsertVideoManager.getInstance().initData();
        } else {
            LocalManager.getInstance().initData();
        }
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
        if (CacheManager.SP.getMode() == 0 && (HighVerSDController.instance().isSDCanUsed()
                || LowVerSDController.instance().isSDCanUsed())) {
            //更新层次类型后再初始化一次播放资源
            startGetRes();
        }
    }

    //常规资源播放
    @Override
    public void startConfigPlay(List<String> videoList) {
        //如果标签为insert优先，且insert正在播放，则不开始播放普通资源
        if (!priority_flag && isInsertPlaying) {
            return;
        }
        isConfigPlaying = true;
        ijkPlayer.enableListLoop(true);
        play(videoList);
    }

    //常规资源停止
    @Override
    public void stopConfigPlay() {
        isConfigPlaying = false;
        //如果标签为insert优先，且insert正在播放，则不停止播放器
        if (!priority_flag && isInsertPlaying) {
            return;
        }
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
            ResourceManager.getInstance().loadData();
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

    public void removeView(View view) {
        flRoot.removeView(view);
    }

    public void addView(View view) {
        flRoot.addView(view);
    }

    /*===========页面控件相关=====================================================================
     * 初始化播放列表
     */

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                LogUtil.E("-------长按下键");
                ConsoleUtil.instance().showConsole();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                LogUtil.E("-------长按下键");
                ConsoleUtil.instance().hideConsole();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
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
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    event.startTracking();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                MenuActivity menuActivity = APP.getMenuActivity();
                if (menuActivity == null || !menuActivity.isForeground()) {
                    startActivity(new Intent(this, MenuActivity.class));
                }
                return false;
            case KeyEvent.KEYCODE_BACK:
                APP.exit();
                return false;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DanmakuManager.getInstance().configurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkPlayer.resume();
        DanmakuManager.getInstance().resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
