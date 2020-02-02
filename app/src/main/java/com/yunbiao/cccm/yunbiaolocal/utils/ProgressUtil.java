package com.yunbiao.cccm.yunbiaolocal.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.cccm.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/2/28.
 */

public class ProgressUtil {

    private Toast mToast;

    private SimpleDateFormat mmss = new SimpleDateFormat("mm:ss");
    private static ProgressUtil instance;
    private View rootView;
    private TextView tvCurr;
    private TextView tvTotal;
    private ProgressBar pb;
    private Timer pbTimer;
    private Activity mAct;
    private TextView tvPlayState;

    public static ProgressUtil instance() {
        if (instance == null) {
            synchronized (ProgressUtil.class) {
                if (instance == null) {
                    instance = new ProgressUtil();
                }
            }
        }
        return instance;
    }

    private ProgressUtil() {
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
        tvPlayState = (TextView) act.findViewById(R.id.tv_play_state);
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

    Timer stateTimer;
    public void showPlayState(int state){
        tvPlayState.setVisibility(View.VISIBLE);
        if(stateTimer != null){
            stateTimer.cancel();
        }

        if (state == 0) {
            tvPlayState.setText("播放");
            stateTimer = new Timer();
            stateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvPlayState.setVisibility(View.GONE);
                        }
                    });
                }
            },1500);
            return;
        }

        tvPlayState.setText("暂停");
    }

    public void cancel() {
        rootView.setVisibility(View.GONE);
    }
}
