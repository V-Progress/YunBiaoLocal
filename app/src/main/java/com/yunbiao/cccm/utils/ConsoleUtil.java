package com.yunbiao.cccm.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.MainActivity;
import com.yunbiao.cccm.log.LogUtil;

import rjsv.circularview.CircleView;

/**
 * Created by Administrator on 2019/3/1.
 */

public class ConsoleUtil {

    private static ConsoleUtil instance;
    private MainActivity mainAct;

    /*加载框-----*/
    private LinearLayout llLoadingMain;
    private ProgressBar pbLoadingMain;
    private TextView tvLoadingMain;

    /*更新下载进度条---*/
    private LinearLayout llUpdateArea;
    private FrameLayout flRoot;
    private ProgressBar pbUpdate;
    private TextView tvSpeed;

    /*资源下载进度条---*/
    private TextView tvConsoleMain;
    private ScrollView svConsoleMain;
    private CircleView progressChildMain;
    private CircleView progressParentMain;
    private TextView tvNumMain;
    private TextView tvProgressMain;
    private LinearLayout llConsoleMain;
    private View retryRoot;
    private TextView tvRetryProgress;
    private TextView tvRetryName;

    public static ConsoleUtil instance(){
        if(instance == null){
            synchronized(ConsoleUtil.class){
                if(instance == null){
                    instance = new ConsoleUtil();
                }
            }
        }
        return instance;
    }

    private ConsoleUtil(){}

    public void init(@NonNull MainActivity mainActivity){
        mainAct = mainActivity;
        llLoadingMain = mainAct.findViewById(R.id.fl_loading_main);
        pbLoadingMain = mainAct.findViewById(R.id.pb_loading_main);
        tvLoadingMain = mainAct.findViewById(R.id.tv_loading_main);

        llUpdateArea = mainAct.findViewById(R.id.ll_update_area);
        flRoot = mainAct.findViewById(R.id.fl_root);
        pbUpdate = mainAct.findViewById(R.id.pb_update);
        tvSpeed = mainAct.findViewById(R.id.tv_speed_main);

        retryRoot = mainAct.findViewById(R.id.ll_retry_root);
        retryRoot.setVisibility(View.GONE);
        tvRetryName = mainAct.findViewById(R.id.tv_retry_filename);
        tvRetryProgress = mainAct.findViewById(R.id.tv_retry_progress);

        tvConsoleMain = mainAct.findViewById(R.id.tv_console_main);
        svConsoleMain = mainAct.findViewById(R.id.sv_console_main);
        progressChildMain = mainAct.findViewById(R.id.progress_child_main);
        progressParentMain = mainAct.findViewById(R.id.progress_parent_main);
        tvNumMain = mainAct.findViewById(R.id.tv_num_main);
        tvProgressMain = mainAct.findViewById(R.id.tv_progress_main);
        llConsoleMain = mainAct.findViewById(R.id.ll_console_main);
        svConsoleMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                svConsoleMain.post(new Runnable() {
                    @Override
                    public void run() {
                        svConsoleMain.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    //------进度条控制----------------------------------------------------------------
    /*
    * 打开控制台
    */
    Handler consoleHandler = new Handler();
    Runnable consoleRunnable = new Runnable() {
        @Override
        public void run() {
            if(llConsoleMain.isShown()){
                llConsoleMain.setVisibility(View.GONE);
            }
        }
    };

    private final int CONSOLE_WHAT = 11;
    private final int CONSOLE_SHOW_TIME = 15 * 1000;
    public void showConsole(){
        LogUtil.E("-------"+llConsoleMain.isShown());
        llConsoleMain.setVisibility(View.VISIBLE);
        svConsoleMain.setFocusable(true);
        consoleHandler.removeCallbacks(consoleRunnable);
        consoleHandler.postDelayed(consoleRunnable,CONSOLE_SHOW_TIME);
    }

    public void hideConsole(){
        svConsoleMain.setFocusable(false);
        LogUtil.E("-------"+llConsoleMain.isShown());
        llConsoleMain.setVisibility(View.GONE);
        consoleHandler.removeCallbacks(consoleRunnable);
    }

    public void updateConsole(String msg) {
        String lastStr = tvConsoleMain.getText().toString();
        tvConsoleMain.setText(lastStr + "\n\n" + msg);
    }

    public void initProgress(int parentMax) {
        tvNumMain.setText(0 + "/" + parentMax);
        progressParentMain.setMaximumValue(parentMax);
        progressChildMain.setMaximumValue(100);
    }

    public void updateChildProgress(int pg) {
        progressChildMain.setProgressValue(pg);
        tvProgressMain.setText(pg + "%");
    }

    public void updateParentProgress(int pg) {
        progressParentMain.setProgressValue(pg);

        String num = tvNumMain.getText().toString();
        if (TextUtils.isEmpty(num)) {
            return;
        }
        String[] split = num.split("/");
        if (split.length < 2) {
            return;
        }
        tvNumMain.setText(pg + "/" + split[1]);
    }

    public void updateDownloadSpeed(String speed) {
        tvSpeed.setText(speed);
    }

    public void openLoading(String loadingMsg) {
        tvLoadingMain.setText(loadingMsg);
        pbLoadingMain.setInterpolator(new AccelerateDecelerateInterpolator());
        llLoadingMain.setVisibility(View.VISIBLE);
    }

    public void closeLoading() {
        llLoadingMain.setVisibility(View.GONE);
    }

    public void openRetry(){
        retryRoot.setVisibility(View.VISIBLE);
    }

    public void updateRetry(String fileName,String progressStr){
        tvRetryName.setText(fileName);
        tvRetryProgress.setText(progressStr+"%");
    }

    public void closeRetry(){
        retryRoot.setVisibility(View.GONE);

    }
}
