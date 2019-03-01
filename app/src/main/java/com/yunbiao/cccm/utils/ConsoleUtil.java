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
import com.yunbiao.cccm.common.Const;

import butterknife.BindView;
import rjsv.circularview.CircleView;

/**
 * Created by Administrator on 2019/3/1.
 */

public class ConsoleUtil {

    private static ConsoleUtil instance;
    private MainActivity mainAct;

    /*加载框-----*/
    LinearLayout llLoadingMain;
    ProgressBar pbLoadingMain;
    TextView tvLoadingMain;

    /*更新下载进度条---*/
    LinearLayout llUpdateArea;
    FrameLayout flRoot;
    ProgressBar pbUpdate;
    TextView tvSpeed;

    /*资源下载进度条---*/
    TextView tvConsoleMain;
    ScrollView svConsoleMain;
    CircleView progressChildMain;
    CircleView progressParentMain;
    TextView tvNumMain;
    TextView tvProgressMain;
    LinearLayout llConsoleMain;


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
    public void openConsole() {
        //如果是正式环境，则不开启控制台和进度条
        if (!Const.SYSTEM_CONFIG.IS_PRO) {
            llConsoleMain.setVisibility(View.VISIBLE);
        }
    }

    /*
     * 关闭控制台
     */
    public void closeConsole() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                llConsoleMain.setVisibility(View.GONE);
                progressParentMain.setMaximumValue(0);
                tvConsoleMain.setText("");
            }
        }, 3000);
    }

    public void updateConsole(String msg) {
        String lastStr = tvConsoleMain.getText().toString();
        tvConsoleMain.setText(lastStr + "\n" + msg);
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




}
