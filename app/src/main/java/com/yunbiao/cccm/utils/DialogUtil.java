package com.yunbiao.cccm.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import android.widget.Toast;

/**
 * Created by Administrator on 2018/12/3.
 */

public class DialogUtil {

    private static DialogUtil instance;
    private ProgressDialog progressDialog;

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
                    Toast.makeText(activity,msg,Toast.LENGTH_LONG).show();
                }
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
            }
        });
    }

}
