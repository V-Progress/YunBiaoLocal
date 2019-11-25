package com.janev.easyijkplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
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
    private TextureVideoView videoView;
    private IjkVideoView ijkVideoView;
    private Queue<String> playQueue = new LinkedList<>();
    private boolean isCycle = true;
    private String currentPath = "";
//    private PlayController playController;

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
            videoView = createVideoView(mContext);
            view = videoView;
        } else {
            ijkVideoView = createIjkVideoView(mContext);
            view = ijkVideoView;
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(view,layoutParams);

    }

    private TextureVideoView createVideoView(Context context) {
        TextureVideoView videoView = new TextureVideoView(context);
        HCPlayerListener listener = new HCPlayerListener(videoView);
        return videoView;
    }

    private IjkVideoView createIjkVideoView(Context context) {
        IjkVideoView ijkVideoView = new IjkVideoView(context);
        IjkPlayerListener listener = new IjkPlayerListener(ijkVideoView);
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
            videoView.seekTo((int) position);
        } else {
            ijkVideoView.seekTo((int) position);
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
            videoView.setVideoPath(playPath);
            videoView.start();
        } else {
            ijkVideoView.setVideoPath(playPath);
            ijkVideoView.start();
        }
    }

    public void stop() {
        currentPath = "";
        if (canUsedHardCodec) {
            videoView.stopPlayback();
        } else {
            ijkVideoView.stopPlayback();
        }
    }

    public void pause() {
        if (canUsedHardCodec) {
            videoView.pause();
        } else {
            ijkVideoView.pause();
        }
    }

    public void resume() {
        if (canUsedHardCodec) {
            videoView.resume();
            videoView.start();
        } else {
            ijkVideoView.resume();
            ijkVideoView.start();
        }
    }

    private int seekToOffset = 3;//快进偏移量，单位：秒
    public void fastForward(){
        if(canUsedHardCodec){
            int forward = videoView.getCurrentPosition() + (seekToOffset * 1000);
            videoView.seekTo(forward);
        } else {
            long forward = ijkVideoView.getCurrentPosition() + (seekToOffset * 1000);
            ijkVideoView.seekTo((int) forward);
        }
    }

    public void fastBackward(){
        if(canUsedHardCodec){
            int backward = videoView.getCurrentPosition() - (seekToOffset * 1000);
            videoView.seekTo(backward);
        } else {
            long backward = ijkVideoView.getCurrentPosition() - (seekToOffset * 1000);
            ijkVideoView.seekTo((int) backward);
        }
    }

    public void toggle(){
        if(canUsedHardCodec){
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
        } else {
            if (ijkVideoView.isPlaying()) {
                ijkVideoView.pause();
            } else {
                ijkVideoView.start();
            }
        }
    }

    public boolean isPlaying(){
        return canUsedHardCodec ? videoView.isPlaying() : ijkVideoView.isPlaying();
    }

    public long getCurrentPosition(){
        return canUsedHardCodec ? videoView.getCurrentPosition() : ijkVideoView.getCurrentPosition();
    }

    public String getCurrentPath(){
        return currentPath;
    }

    class HCPlayerListener implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

        public HCPlayerListener(TextureVideoView videoView) {
//            videoView.setOnPreparedListener(this);
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

    class IjkPlayerListener implements IMediaPlayer.OnErrorListener,
            IMediaPlayer.OnInfoListener, IMediaPlayer.OnCompletionListener {

        public IjkPlayerListener(IjkVideoView ijkVideoView) {
//            ijkVideoView.setOnPreparedListener(this);
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
            if(videoView != null){
                updateForVideoView();
            } else if(ijkVideoView != null){
                updateForIjk();
            }
            startUpdate(500);
        }
    };

    private void updateForVideoView(){
        if(videoView.isPlaying()){
            ibPlayState.setImageResource(R.mipmap.pause);
        } else {
            ibPlayState.setImageResource(R.mipmap.play);
        }

        int currentPosition = videoView.getCurrentPosition();
        int duration = videoView.getDuration();

        timeline.setMax(duration);
        timeline.setProgress(currentPosition);

    }

    private void updateForIjk(){
        if(ijkVideoView.isPlaying()){
            ibPlayState.setImageResource(R.mipmap.pause);
        } else {
            ibPlayState.setImageResource(R.mipmap.play);
        }

        int currentPosition = (int) ijkVideoView.getCurrentPosition();
        int duration = (int) ijkVideoView.getDuration();

        timeline.setMax(duration);
        timeline.setProgress(currentPosition);
    }


}
