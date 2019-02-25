package com.yunbiao.cccm.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yunbiao.cccm.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/12/3.
 */

public class DialogUtil {

    private static DialogUtil instance;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private Timer timer;

    public static synchronized DialogUtil getInstance() {
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }

    public void showProgressDialog(final Context activity, final String title, final String message) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle(title);
                progressDialog.setMessage(message);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        });
    }

    public void dissmissProgress(final Context activity, final String msg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                if((activity != null) && (!TextUtils.isEmpty(msg))){
                    ToastUtil.showShort(activity,msg);
                }
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void showError(Context context,String title,String msg){
        showError(context,title,msg,0,null);
    }

    /***
     * 显示错误窗口
     * 注意：如果指定了delay参数，那么该窗口会在delay秒后关闭
     * @param context
     * @param title
     * @param msg
     * @param delay
     * @param runnable
     */
    public void showError(final Context context, final String title, final String msg, int delay, final Runnable runnable){

        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if(alertDialog != null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertDialog = builder.create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(msg);
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        });


        if(timer != null){
            timer.cancel();
            timer = null;
        }
        if(delay > 0){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(runnable != null){
                        ThreadUtil.getInstance().runInUIThread(runnable);
                    }
                    dismissError();
                }
            },delay*1000);
        }
    }

    public void dismissError(){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {

                if(alertDialog == null || !alertDialog.isShowing()){
                    return;
                }
                alertDialog.dismiss();
            }
        });
    }

    public void showCustomAlert(Context context, String message, final View.OnClickListener confirm, final View.OnClickListener cancel){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setContentView(R.layout.layout_mode_alert);
        alertDialog.setCancelable(true);
        TextView msg = alertDialog.findViewById(R.id.tv_mode_alert_msg);
        Button btnCancel = alertDialog.findViewById(R.id.btn_mode_alert_cancel);
        Button btnConfirm = alertDialog.findViewById(R.id.btn_mode_alert_confirm);
        msg.setText(message);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cancel != null){
                    cancel.onClick(v);
                }
                alertDialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirm != null){
                    confirm.onClick(v);
                }
                alertDialog.dismiss();
            }
        });
    }

}
