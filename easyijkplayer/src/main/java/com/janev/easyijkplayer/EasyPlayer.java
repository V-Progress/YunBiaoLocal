package com.janev.easyijkplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Administrator on 2019/11/25.
 */

public class EasyPlayer extends FrameLayout {
    private boolean canUsedHardCodec;
    private Context mContext;
    private List<String> playList = new ArrayList<>();
    private VideoView hdVideoView;
    private IjkVideoView sdVideoView;
    private Queue<String> playQueue = new LinkedList<>();
    private boolean isCycle = true;
    private String currentPath = "";

    public EasyPlayer(@NonNull Context context) {
        this(context, null);
    }

    public EasyPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyPlayer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        canUsedHardCodec = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;

        View view = null;
        if (canUsedHardCodec) {
            hdVideoView = createVideoView(mContext);
            view = hdVideoView;
        } else {
            sdVideoView = createIjkVideo(mContext);
            view = sdVideoView;
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(view,layoutParams);

    }

    private VideoView createVideoView(Context context) {
//        TextureVideoView hdVideoView = new TextureVideoView(context);
        VideoView videoView = new VideoView(context);
        new HCPlayerListener(videoView);
        return videoView;
    }

    private IjkVideoView createIjkVideo(Context context) {
        IjkVideoView ijkVideoView = new IjkVideoView(context);
        new IjkPlayerListen(ijkVideoView);
        return ijkVideoView;
    }

    public void setVideos(List<String> list) {
        stop();
        currentPath = "";
        playList.clear();
        playQueue.clear();
        if (list == null) {
            return;
        }
        playList.addAll(list);
        playQueue.addAll(list);
        play();
    }

    public void seekTo(long position) {
        if(canUsedHardCodec){
            hdVideoView.seekTo((int) position);
        } else {
            sdVideoView.seekTo((int) position);
        }
    }

    public interface PlayCallback{
        void playComplete();
    }

    private PlayCallback playCallback;

    public void setPlayCallback(PlayCallback playCallback) {
        this.playCallback = playCallback;
    }

    public void setCycle(boolean cycle) {
        isCycle = cycle;
    }

    public void play() {
        if(playQueue == null || playQueue.size() <= 0){
            if(playCallback != null){
                playCallback.playComplete();
            }
            return;
        }
        String playPath = playQueue.poll();
        currentPath = playPath;
        if(isCycle){
            playQueue.offer(playPath);
        }
        if (canUsedHardCodec) {
            hdVideoView.setVideoPath(playPath);
            hdVideoView.start();
        } else {
            sdVideoView.setVideoPath(playPath);
            sdVideoView.start();
        }
    }

    public void stop() {
        currentPath = "";
        if (canUsedHardCodec) {
            hdVideoView.stopPlayback();
        } else {
            sdVideoView.stopPlayback();
        }
    }

    public void pause() {
        if (canUsedHardCodec) {
            hdVideoView.pause();
        } else {
            sdVideoView.pause();
        }
    }

    public void resume() {
        if (canUsedHardCodec) {
            hdVideoView.resume();
            hdVideoView.start();
        } else {
            sdVideoView.resume();
            sdVideoView.start();
        }
    }

    private int seekToOffset = 3;//快进偏移量，单位：秒
    public void fastForward(){
        if(canUsedHardCodec){
            int forward = hdVideoView.getCurrentPosition() + (seekToOffset * 1000);
            hdVideoView.seekTo(forward);
        } else {
            long forward = sdVideoView.getCurrentPosition() + (seekToOffset * 1000);
            sdVideoView.seekTo((int) forward);
        }
    }

    public void fastBackward(){
        if(canUsedHardCodec){
            int backward = hdVideoView.getCurrentPosition() - (seekToOffset * 1000);
            hdVideoView.seekTo(backward);
        } else {
            long backward = sdVideoView.getCurrentPosition() - (seekToOffset * 1000);
            sdVideoView.seekTo((int) backward);
        }
    }

    public void toggle(){
        if(canUsedHardCodec){
            if (hdVideoView.isPlaying()) {
                hdVideoView.pause();
            } else {
                hdVideoView.start();
            }
        } else {
            if (sdVideoView.isPlaying()) {
                sdVideoView.pause();
            } else {
                sdVideoView.start();
            }
        }
    }

    public boolean isPlaying(){
        return canUsedHardCodec ? hdVideoView.isPlaying() : sdVideoView.isPlaying();
    }

    public long getCurrentPosition(){
        return canUsedHardCodec ? hdVideoView.getCurrentPosition() : sdVideoView.getCurrentPosition();
    }

    public String getCurrentPath(){
        return currentPath;
    }

    class HCPlayerListener implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

        public HCPlayerListener(VideoView videoView) {
//            hdVideoView.setOnPreparedListener(this);
            videoView.setOnCompletionListener(this);
            videoView.setOnErrorListener(this);
            videoView.setOnInfoListener(this);
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            play();
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            play();
            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }

    }

    class IjkPlayerListen implements IMediaPlayer.OnErrorListener,
            IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener {

        public IjkPlayerListen(IjkVideoView ijkVideoView) {
//            sdVideoView.setOnPreparedListener(this);
            ijkVideoView.setOnCompletionListener(this);
            ijkVideoView.setOnErrorListener(this);
            ijkVideoView.setOnInfoListener(this);
        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            play();
        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            play();
            return true;
        }

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    }

    private ImageButton ibPlayFull;
    private ImageButton ibPlayState;
    private TextView tvVideoTime;
    private ProgressBar timeline;
    private View controller;

    /*===================================================================*/
    public void enableController(boolean isShowFullButton){
        controller = View.inflate(mContext, R.layout.layout_video_progress, null);
        timeline = controller.findViewById(R.id.pb_timeline);
        tvVideoTime = controller.findViewById(R.id.tv_video_time);
        ibPlayState = controller.findViewById(R.id.ib_play_state);
        ibPlayFull = controller.findViewById(R.id.ib_play_fullscreen);
        ibPlayState.requestFocus();

        if(!isShowFullButton){
            ibPlayFull.setVisibility(View.GONE);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = 30;
        addView(controller,params);
        startUpdate(0);
    }

    private void startUpdate(long time){
        controller.postDelayed(updateRunnable,time);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if(hdVideoView != null){
                updateForVideoView();
            } else if(sdVideoView != null){
                updateForIjk();
            }
            startUpdate(500);
        }
    };

    private void updateForVideoView(){
        if(hdVideoView.isPlaying()){
            ibPlayState.setImageResource(R.mipmap.pause);
        } else {
            ibPlayState.setImageResource(R.mipmap.play);
        }

        int currentPosition = hdVideoView.getCurrentPosition();
        int duration = hdVideoView.getDuration();

        timeline.setMax(duration);
        timeline.setProgress(currentPosition);

    }

    private void updateForIjk(){
        if(sdVideoView.isPlaying()){
            ibPlayState.setImageResource(R.mipmap.pause);
        } else {
            ibPlayState.setImageResource(R.mipmap.play);
        }

        int currentPosition = (int) sdVideoView.getCurrentPosition();
        int duration = (int) sdVideoView.getDuration();

        timeline.setMax(duration);
        timeline.setProgress(currentPosition);
    }


}
