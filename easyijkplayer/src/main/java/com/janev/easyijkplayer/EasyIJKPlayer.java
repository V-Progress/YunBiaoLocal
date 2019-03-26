package com.janev.easyijkplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by JaneV on 2019/3/14.
 */

public class EasyIJKPlayer extends FrameLayout implements IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, View.OnClickListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnSeekCompleteListener {

    private static final String TAG = "EasyIJKPlayer";

    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IjkMediaPlayer mMediaPlayer = null;
    private SurfaceView surfaceView;
    private Context mContext;

    //播放列表
    private List<String> playList = new ArrayList<>();

    //控制条
    private SeekBar timeline;
    //播放状态按钮
    private ImageButton ibPlayState;
    //开始更新进度条标识
    private final int START_UPDATE_PROGRESS = 0;
    //进度条更新
    private final int UPDATE_TIME = 1000;

    //一些标识
    private String currPlayUri;//当前正播放的Uri
    //列表循环
    private boolean listLoop = true;
    //控制条开启
    private boolean controllerEnable = false;
    //控制条长亮
    private boolean controllerLongShow = false;
    //错误时删除错误的URI
    private boolean errorDelUriEnable = true;
    //错误时提示
    private boolean errorAlertEnable = false;

    //当前状态
    private int mCurrentState = 0;
    /***
     * 返回状态
     */
    private final int STATE_RESUME = 1;
    /***
     * 暂停状态
     */
    private final int STATE_PAUSE = 2;
    /***
     * 停止状态
     */
    private final int STATE_STOP = 3;

    /***
     * 播放模式
     */
    private int mPlayMode = 0;
    /***
     * 单个播放模式
     */
    private final int MODE_SIMPLE = 0;
    /***
     * 列表模式
     */
    private final int MODE_LIST = 1;

    private IjkPlayListener mListener;

    //临时时间
    private int mControllerTime = 0;//用于控制条隐藏
    private ProgressBar bufferPb;//缓冲加载条
    private int seekToOffset = 3;//快进偏移量，单位：秒

    //当前播放指针
    private int playIndex = 0;
    //当前播放位置
    private long currPosition = 0;
    private View controllerView;
    private TextView tcpSpeed;
    private LinearLayout loadingView;
    private int reLoadLiveFlag = 0;

    public EasyIJKPlayer(@NonNull Context context) {
        super(context);
        initVideoView(context);

    }

    public EasyIJKPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);

    }

    public EasyIJKPlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    /**
     * 初始化IjkMediaPlayer加载so库
     */
    public void initSoLib(){
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNavigation(@NonNull Activity activity){
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    /***
     * 初始化区========================================================================================
     */
    //初始化
    private void initVideoView(Context context) {
        mContext = context;
        createSurfaceView();
        createController();
        //获取焦点，不知道有没有必要~。~
        setFocusable(true);
        setOnClickListener(this);
        setBackgroundColor(Color.BLACK);
    }

    /**
     * 新建一个surfaceview
     */
    @SuppressLint("NewApi")
    private void createSurfaceView() {
        //生成一个新的surface view
        surfaceView = new SurfaceView(mContext);
        this.addView(surfaceView);
        surfaceView.getHolder().addCallback(new LmnSurfaceCallback());
        surfaceView.setZOrderOnTop(true);
        surfaceView.setSecure(true);
        surfaceView.setZOrderMediaOverlay(true);
    }

    private void createController() {
        controllerView = View.inflate(mContext, R.layout.layout_video_progress, null);
        timeline = controllerView.findViewById(R.id.timeline);
        ibPlayState = controllerView.findViewById(R.id.ib_play_state);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = 30;
        this.addView(controllerView, params);

        controllerView.setVisibility(View.GONE);
        timeline.setOnSeekBarChangeListener(onSeekBarChangeListener);
        ibPlayState.setOnClickListener(this);

        loadingView = new LinearLayout(mContext);
        loadingView.setOrientation(LinearLayout.VERTICAL);
        loadingView.setGravity(Gravity.CENTER);
        bufferPb = new ProgressBar(mContext);
        tcpSpeed = new TextView(mContext);
        loadingView.addView(bufferPb);
        loadingView.addView(tcpSpeed);
        LayoutParams pbParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbParams.gravity = Gravity.CENTER;
        this.addView(loadingView, pbParams);
        loadingView.setVisibility(View.GONE);
    }

    /***
     * 资源设置区========================================================================================
     */
    public List<String> getPlayList(){
        return playList;
    }

    /***
     * 设置播放列表
     * @param videoList
     */
    public void setVideoList(List<String> videoList/*,boolean isAdd*/){
        if(videoList == null || videoList.size()<=0){
            return;
        }
        mPlayMode = MODE_LIST;//设置播放状态
        playList.clear();//清除列表
        playList.addAll(videoList);//添加内容
        Log.e(TAG, "setVideoList: "+playList.toString());
        loadUri();

//        if((!isInitPlay) || (!isAdd)){
//            isInitPlay = true;
//            playList.clear();//清除列表
//            playList.addAll(videoList);//添加内容
//            Log.e(TAG, "setVideoList: "+playList.toString());
//            loadUri();
//        }
//
//        for (String s : videoList) {
//            Log.e(TAG, "setVideoList: " + s);
//            if(!playList.contains(s)){
//                Log.e(TAG, "setVideoList: playList中包含此内容");
//                playList.add(s);
//            }
//        }
    }

    /***
     * 设置单个播放Uri
     * @param uri
     */
    public void setVideoUri(String uri) {
        mPlayMode = MODE_SIMPLE;
        currPlayUri = uri;
        loadUri();
    }

    private void loadUri() {
        release();

        Log.e(TAG, "loadUri: 指针："+playIndex );
        if(mPlayMode == MODE_LIST){
            if(playList == null || playList.size()<=0){
                return;
            }

            if(playIndex >= playList.size()){
                playIndex = 0 ;
            }

            currPlayUri = playList.get(playIndex);
        }
        if(TextUtils.isEmpty(currPlayUri)){
            return;
        }

        Log.e(TAG, "loadUri: "+currPlayUri);
        //每次都要重新创建IMediaPlayer
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setDisplay(surfaceView.getHolder());
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setLogEnabled(false);
        //跳帧处理，保证音画同步
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"framedrop",5);
        //最大FPS
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"max-fps",30);
        //seekTo设置优化(会导致视频开启过慢，拖动进度条后视频会延迟播放)
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);

        setLiveOption();

        Uri videoUri = Uri.parse(currPlayUri);
        try {
            mMediaPlayer.setDataSource(mContext,videoUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                changeVideoSize();
                iMediaPlayer.start();
                if(mCurrentState == STATE_RESUME){
                    iMediaPlayer.seekTo(currPosition);
                    timeline.setMax((int) iMediaPlayer.getDuration());
                    timeline.setProgress((int) currPosition);
                }
            }
        });

        showProgress();
        loadingView.setVisibility(View.GONE);
    }

    public String getCurrentVideo() {
        return currPlayUri;
    }


    /***
     * 监听区========================================================================================
     */
    //surfaceView的监听器
    private class LmnSurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.e(TAG, "surfaceView已创建完成");
            if(TextUtils.isEmpty(currPlayUri)){
                return;
            }
            if(mCurrentState == STATE_RESUME){
                loadUri();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e(TAG, "surfaceView已更改");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e(TAG, "surfaceView已销毁");
            if(mCurrentState == STATE_PAUSE){
                currPosition = getCurrentPosition();
            }
            release();
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        Log.e(TAG, "onInfo: "+what + "-----"+extra);
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            loadingView.setVisibility(View.VISIBLE);
            isLoadLong = true;
            Log.e(TAG, "onInfo: 开始加载-----" );
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            loadingView.setVisibility(View.GONE);
            isLoadLong = false;
            reLoadLiveFlag = 0;
            Log.e(TAG, "onInfo: 加载成功-----" );
        }
        return true;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        complete();
    }


    private void complete(){
        release();
        currPosition = 0;

        Log.e(TAG, "onListCompletion: 播放完毕");
        //如果当前是单个播放模式，则播放完毕后释放
        if(mPlayMode == MODE_SIMPLE){
            Log.e(TAG, "onListCompletion: 单个播放");
            return;
        }

        playIndex++;
        Log.e(TAG, "onListCompletion: 列表播放");
        //如果是列表播放，并且指针等于列表尺寸的话
        if(playIndex >= playList.size()){
            Log.e(TAG, "onListCompletion: 指针大于等于列表尺寸");
            playIndex = 0;//重置指针
            Log.e(TAG, "onListCompletion: 重置指针");
            if(!listLoop){//如果非列表循环
                Log.e(TAG, "onListCompletion: 非列表循环：结束");
                if(mListener != null){
                    mListener.onListCompletion();
                }
                return;
            }
        }
        //如果是列表播放，则增加指针继续播放
        Log.e(TAG, "onListCompletion: 列表循环");
        loadUri();
    }
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG, "播放失败");
        if(errorAlertEnable){
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("播放错误");
            builder.setMessage("错误代码：("+i+","+i1+")");
            builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    error();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
        error();
        return false;
    }
    private void error(){
        release();
        currPosition = 0;

        //如果播放状态是单个播放
        if(mPlayMode == MODE_SIMPLE){
            Log.e(TAG, "error: 单个播放，结束");
            return;
        }

        //如果是列表播放,并且开启错误删除的话则删除当前错误uri
        Log.e(TAG, "error: 列表播放");
        if(errorDelUriEnable){
            String remove = playList.remove(playIndex);//错误后删除错误列表
            Log.e(TAG, "error: 删除错误uri："+remove);
        }
        playIndex++;
        if(playList.size()<=0){
            if(mListener != null){
                mListener.onListCompletion();
            }
        }
        //测试判断指针如果等于列表尺寸或大于
        if(playIndex >= playList.size()){
            playIndex = 0;//重置指针
            if(!listLoop){//如果未开列表循环
                return;//结束
            }
        }
        loadUri();
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        if(mCurrentState == STATE_PAUSE){
            return;
        }
        start();
    }

    /***
     * 控制区========================================================================================
     */
    public void setIjkPlayListener(IjkPlayListener ijkPlayListener){
        this.mListener = ijkPlayListener;
    }
    /***
     * 开启控制条
     * @param enable default = false，default = false
     */
    public void enableController(boolean enable, boolean isLongShow) {
        controllerEnable = enable;
        controllerLongShow = isLongShow;
    }

    public void enableErrorAlert(boolean enable){
        this.errorAlertEnable = enable;
    }

    /***
     * 开启列表循环
     * @param enable default = true
     */
    public void enableListLoop(boolean enable) {
        this.listLoop = enable;
    }

    /***
     * 错误时删除该Uri
     * @param enable default = true;
     */
    public void enableErrorDeleteUri(boolean enable) {
        errorDelUriEnable = enable;
    }

    /***
     * 设置快进快退的偏移量
     * @param seconds 秒  default = 3
     */
    public void setSeekToOffset(int seconds) {
        seekToOffset = seconds;
    }

    public void toggle() {
        controllerView.setVisibility(View.VISIBLE);
        mControllerTime = 0;
        if (isPlaying()) {
            ibPlayState.setImageResource(R.mipmap.play);
            if(mCurrentState == STATE_STOP){
                return;
            }
            pause();
        } else {
            ibPlayState.setImageResource(R.mipmap.pause);

            //如果是停止状态，则把状态置为Resume
            if(mCurrentState == STATE_STOP){
                mCurrentState = STATE_RESUME;
                loadUri();//重新加载
                return;
            }
            mCurrentState = STATE_RESUME;
            Log.e(TAG, "toggle: 当前状态："+mCurrentState );
            start();
        }
    }

    /**
     * 下面封装了一下控制视频的方法
     */
    public void fastForword() {
        if (mMediaPlayer != null && (mMediaPlayer.getDuration() > 0)) {
            long forword = mMediaPlayer.getCurrentPosition() + (seekToOffset * 1000);
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(forword);
            timeline.setProgress((int) forword);
            controllerView.setVisibility(View.VISIBLE);
        }
    }

    public void fastBackward() {
        if (mMediaPlayer != null && (mMediaPlayer.getDuration() > 0)) {
            long fastBackword = mMediaPlayer.getCurrentPosition() - (seekToOffset * 1000);
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(fastBackword);
            timeline.setProgress((int) fastBackword);
            controllerView.setVisibility(View.VISIBLE);
        }
    }

    /***
     * 开始
     */
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /***
     * 恢复（应当是恢复缓存的进度和Uri的播放）
     */
    public void resume() {
        mCurrentState = STATE_RESUME;
    }

    /***
     * 暂停
     */
    public void pause() {
        mCurrentState = STATE_PAUSE;
        currPosition = getCurrentPosition();
        if(mMediaPlayer != null){
            mMediaPlayer.pause();
        }
    }

    /***
     * 停止
     */
    public void stop() {
        mCurrentState = STATE_STOP;
        playList.clear();
        currPlayUri = "";
        if (mMediaPlayer != null) {
            toggle();
            currPosition = 0;
            surfaceView.setLayoutParams(new LayoutParams(0,0));
            mMediaPlayer.stop();
            timeline.setProgress(0);
            timeline.setMax(0);
            release();
        }
    }

    /***
     * 释放资源
     */
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /***
     * 状态区========================================================================================
     */
    /***
     * 是否在播放
     * @return
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /***
     * 获取总时长
     * @return
     */
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /***
     * 获取当前进度
     * @return
     */
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public void seekTo(long l) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(l);
        }
    }

    /***
     * 自动区========================================================================================
     */
    private void changeVideoSize() {
        try {
            double videoWidth = mMediaPlayer.getVideoWidth();
            double videoHeight = mMediaPlayer.getVideoHeight();
            Log.e(TAG, "视频尺寸——" + videoWidth + "---" + videoHeight);

            ViewGroup parent = (ViewGroup) surfaceView.getParent();
            double surfaceWidth = Double.valueOf(parent.getWidth());
            double surfaceHeight = Double.valueOf(parent.getHeight());
            Log.e(TAG, "父控件尺寸——" + surfaceWidth + "---" + surfaceHeight);

            //根据视频尺寸去计算-&gt;视频可以在sufaceView中放大的最大倍数。
            double prop = 1;
            if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                //竖屏模式下按视频宽度计算放大倍数值
                prop = surfaceWidth / videoWidth;
            } else {
                if(videoWidth > surfaceWidth){//如果是小窗口播放
                    prop = surfaceWidth / videoWidth;
                } else {
                    //横屏模式下按视频高度计算放大倍数值
                    prop = surfaceHeight / videoHeight;
                }
            }

            videoWidth = videoWidth * prop;
            videoHeight = videoHeight * prop;
            Log.e(TAG, "计算出的尺寸——" + videoWidth + "---" + videoHeight);

            //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
            surfaceView.setLayoutParams(new LayoutParams((int) videoWidth, (int) videoHeight, Gravity.CENTER));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showProgress() {
        progressHandler.removeMessages(START_UPDATE_PROGRESS);
        progressHandler.sendEmptyMessage(START_UPDATE_PROGRESS);
        if (controllerEnable) {
            controllerView.setVisibility(View.VISIBLE);
            mControllerTime = 0;
        } else {
            controllerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ib_play_state){
            toggle();
            return;
        }
        if (controllerEnable) {
            controllerView.setVisibility(View.VISIBLE);
            mControllerTime = 0;
        }
    }

    private boolean isLoadLong = false;

    private Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (controllerEnable && !controllerLongShow) {//如果不是长显的话
                if (mControllerTime >= 5) {//开始计算标识
                    mControllerTime = 0;
                    controllerView.setVisibility(View.GONE);
                }
                mControllerTime++;
            }

            if (mMediaPlayer == null) {
                return;
            }

            if(isLoadLong){
                if(reLoadLiveFlag >= 30){
                    Log.e(TAG, "onInfo: 时间到，重新加载" );
                    reLoadLiveFlag = 0;
                    loadUri();
                }
                reLoadLiveFlag++;
                Log.e(TAG, "onInfo: 等待"+reLoadLiveFlag+"秒-----" );
            }

            long tcpSpeed = mMediaPlayer.getTcpSpeed();
            if(tcpSpeed != 0){
                tcpSpeed = tcpSpeed / 1024;
            }
            EasyIJKPlayer.this.tcpSpeed.setText(""+tcpSpeed+"k/s");
            long currentPosition = mMediaPlayer.getCurrentPosition();
            long duration = mMediaPlayer.getDuration();
            timeline.setMax((int) duration);
            timeline.setProgress((int) currentPosition);
            sendEmptyMessageDelayed(START_UPDATE_PROGRESS, UPDATE_TIME);
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mControllerTime = 0;
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void setLiveOption(){
        //开启变调
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"soundtouch",0);
//        //播放前最大探测时间
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzemaxduration",100L);
//        //播放前探测时间1，达到首屏秒开
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzeduration",1);
//        //播放前的探测Size，默认是1M, 改小一点会出画面更快
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"probesize",1024*10);
//        //每处理一个packet之后刷新io上下文
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"flush_packets",1L);
//        //播放重连次数
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",5);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 512 * 1024);//设置缓冲区为100KB，目前我看来，多缓冲了4秒
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 5000);// 视频的话，设置100帧即开始播放
//        //设置最大缓冲尺寸kb
////        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"max-buffer-size",1024 * 1024);
//        //预读的 packet 超过 MIN_FRAMES 个，那么 ffplay 就会停止预读(取值2-50000)
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 50000);
//
        //  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制PLAYER, "framedrop", 1L);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "p台打印 FFP_MSG_BUFFERING_START
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_rotocol_whitelist", "crypto,file,http,https,tcp,tls,udp");
    }
}
