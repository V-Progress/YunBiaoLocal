package com.yunbiao.cccm.activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.base.BaseActivity;
import com.yunbiao.cccm.net.listener.MainRefreshListener;
import com.yunbiao.cccm.net.resource.InsertTextManager;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.sdOperator.HighVerSDController;
import com.yunbiao.cccm.sdOperator.LowVerSDController;
import com.yunbiao.cccm.sdOperator.SDManager;
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
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.TimerUtil;

import java.util.List;

import butterknife.BindView;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends BaseActivity implements MainRefreshListener, SDManager.CheckSDListener {
    /*视频播放----*/
    @BindView(R.id.video_view)
    VideoView vv;

    @BindView(R.id.fl_root)
    FrameLayout flRoot;

    @BindView(R.id.danmaku_view)
    DanmakuView danmakuView;

    public boolean isInsertPlaying = false;//是否有insert正在播放
    public boolean isConfigPlaying = false;//是否有config正在播放
    private boolean isCycle = true;//是否轮播
    private boolean priority_flag = false;//true:config优先，false:insert优先

    private List<String> playLists;//当前播放列表
    private static int videoIndex;//当前视频在列表中处于的位置

    protected int setLayout() {
        return R.layout.activity_main;
    }

    protected void initView() {
        //先将插播fragment创建出来
        APP.setMainActivity(this);
        VideoProgressUtil.instance().init(this);
        ConsoleUtil.instance().init(this);
        MainController.getInstance().registerActivity(this);
    }

    protected void initData() {

        //开启软件守护服务
        startService(new Intent(this, MyProtectService.class));

        //初始化播放器
        initPlayer();
        //连接XMPP
        PnServerController.startXMPP(MainActivity.this);

        PowerOffTool.getInstance().initPowerData();
        //检测SD卡
        SDManager.instance().init(this, this).checkSD();
    }

    private void initPlayer() {
        vv.setOnPreparedListener(onPreparedListener);
        vv.setOnErrorListener(errorListener);//播放错误监听
        vv.setOnInfoListener(infoListener);//播放信息监听
        vv.setOnCompletionListener(completionListener);
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
            DialogUtil.getInstance().showError(MainActivity.this, "读取错误", "请插入SD卡\n并确保SD卡可正常使用");
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
    @Override
    public void initPlayData() {
        if(HighVerSDController.instance().isSDCanUsed() || LowVerSDController.instance().isSDCanUsed()){
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
        videoIndex = 0;
        stop();
        if(playLists != null){
            playLists.clear();
        }
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
        if (playLists != null) {
            playLists.clear();
        }
        stop();
        //普通资源结束后解析插播资源
        initInsertData();
    }

    //插播资源播放
    @Override
    public void startInsert(final boolean cycle, final List<String> videoList) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (priority_flag && isConfigPlaying) {
                    return;
                }
                isInsertPlaying = true;
                isCycle = cycle;
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
        if (playLists != null) {
            playLists.clear();
        }
        isCycle = true;
        stop();
        //插播资源结束后解析普通资源
        initPlayData();
    }

    //-----常规方法----------------------------------------------------------------
    private void stop() {
        if (vv == null) {
            return;
        }
        vv.stopPlayback();
        vv.setVisibility(View.GONE);
    }

    private void play(final List<String> videoList) {
        if(playLists == null || playLists.size()<=0 || (!playLists.equals(videoList))){
            playLists = videoList;
            videoIndex = 0;
        } else {
            videoIndex = tempIndex;
        }

        vv.stopPlayback();
        vv.setVideoURI(Uri.parse(playLists.get(videoIndex)));
        vv.setVisibility(View.VISIBLE);
        vv.start();
        vv.seekTo(currTime == 0 ? currTime : currTime - 1000);
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
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoHandler.removeMessages(0);
            videoHandler.sendEmptyMessage(0);

            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    vv.start();
                }
            });
        }
    };

    private Handler videoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 获得当前播放时间和当前视频的长度
            if(vv.isPlaying()){

                currTime = vv.getCurrentPosition();
                tempIndex = videoIndex;
                VideoProgressUtil.instance().updateProgress(vv.getDuration(),currTime);
            }
            videoHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    private int currTime = 0;
    private int tempIndex = 0;
    private int forwardOffsetNum = 2000;
    private int backOffsetNum = 5000;

    private void fastForward(){
        if (vv.canSeekForward()) {
            vv.pause();
            vv.seekTo(vv.getCurrentPosition()+ forwardOffsetNum);
            VideoProgressUtil.instance().showProgress(vv.getDuration(),vv.getCurrentPosition());
        }
    }

    private void fastBackward(){
        if (vv.canSeekBackward()) {
            vv.pause();
            vv.seekTo(vv.getCurrentPosition() - backOffsetNum);
            VideoProgressUtil.instance().showProgress(vv.getDuration(),vv.getCurrentPosition());
        }
    }

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtil.E("播放错误代码：" + what + "-----" + extra);

            DialogUtil.getInstance().showError(MainActivity.this
                    , "播放失败"
                    , "播放地址解析失败\n\n错误代码:" + what + " - " + extra + "\n\n本窗口于10秒后关闭"
                    , 10
                    , new Runnable() {
                        @Override
                        public void run() {
                            stopInsert();
                        }
                    });
            return true;
        }
    };
    //播放器信息监听
    private MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                ConsoleUtil.instance().openLoading("正在缓冲");
            } else {
                ConsoleUtil.instance().closeLoading();//关闭加载
            }
            return true;
        }
    };
    //播放完毕监听
    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            VideoProgressUtil.instance().cancel();
            videoIndex++;
            if (videoIndex > playLists.size() - 1) {
                videoIndex = 0;
                LogUtil.E("是否轮播");
                if (!isCycle) {
                    LogUtil.E("不轮播");
                    stopInsert();
                    return;
                }
            }

            vv.stopPlayback();
            vv.setVideoURI(Uri.parse(playLists.get(videoIndex)));
            vv.start();
        }
    };

    /*===========页面控件相关=====================================================================
     * 初始化播放列表
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                fastForward();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                fastBackward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if(vv.isPlaying()){
                    vv.pause();
                    VideoProgressUtil.instance().showPlayState(1);
                } else {
                    vv.start();
                    VideoProgressUtil.instance().showPlayState(0);
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                startActivity(new Intent(this, MenuActivity.class));
                break;
            case KeyEvent.KEYCODE_BACK:
                APP.exit();
                return false;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DanmakuManager.getInstance().configurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vv.start();
        vv.seekTo(currTime == 0 ? currTime : currTime - 1000);
        DanmakuManager.getInstance().resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DanmakuManager.getInstance().pause();
        vv.pause();
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
