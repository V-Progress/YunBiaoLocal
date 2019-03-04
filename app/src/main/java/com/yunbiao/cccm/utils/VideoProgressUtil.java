package com.yunbiao.cccm.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunbiao.cccm.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/2/28.
 */

public class VideoProgressUtil {

    private SimpleDateFormat mmss = new SimpleDateFormat("mm:ss");
    private static VideoProgressUtil instance;
    private View rootView;
    private TextView tvCurr;
    private TextView tvTotal;
    private ProgressBar pb;
    private Timer pbTimer;
    private Activity mAct;
    private TextView tvPlayState;

    public static VideoProgressUtil instance() {
        if (instance == null) {
            synchronized (VideoProgressUtil.class) {
                if (instance == null) {
                    instance = new VideoProgressUtil();
                }
            }
        }
        return instance;
    }

    private VideoProgressUtil() {
    }

    public void init(@NonNull Activity act) {
        mAct = act;
        rootView = act.findViewById(R.id.fl_progress_video);
        if (rootView == null) {
            throw new RuntimeException();
        }
        tvCurr = (TextView) rootView.findViewById(R.id.tv_current_long);
        tvTotal = (TextView) rootView.findViewById(R.id.tv_total_long);
        pb = (ProgressBar) rootView.findViewById(R.id.progress_video);
        tvPlayState = (TextView) rootView.findViewById(R.id.tv_play_state);
    }

    public void updateProgress(int total, int curr) {
//        String currPosition = mmss.format(new Date(Long.valueOf(curr)));
//        String duration = mmss.format(new Date(Long.valueOf(total)));
//
//        tvCurr.setText(currPosition);
//        tvTotal.setText(duration);

        pb.setMax(total);
        pb.setProgress(curr);
    }

    public void showProgress(int total,int curr) {
        String currPosition = mmss.format(new Date(Long.valueOf(curr)));
        String duration = mmss.format(new Date(Long.valueOf(total)));

        tvCurr.setText(currPosition);
        tvTotal.setText(duration);

        rootView.setVisibility(View.VISIBLE);
        pb.setMax(total);
        pb.setProgress(curr);

        if (pbTimer != null) {
            pbTimer.cancel();
        }
        pbTimer = new Timer();
        pbTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setVisibility(View.GONE);
                    }
                });
            }
        }, 1500);
    }

    public void cancel() {
        rootView.setVisibility(View.GONE);
    }


    Timer stateTimer;
    public void showPlayState(int state){
        tvPlayState.setVisibility(View.VISIBLE);
        switch (state) {
            case 0:
                tvPlayState.setText("播放");
                break;
            case 1:
                tvPlayState.setText("暂停");
                break;
        }
        if(stateTimer != null){
            stateTimer.cancel();
        }
        stateTimer = new Timer();
        stateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                tvPlayState.setVisibility(View.GONE);
            }
        },500);

    }
}
