package com.yunbiao.cccm.common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2019/1/4.
 */

public class ToastUtil {

    private static Toast toast;

    public static void showShort(final Context context, final String msg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static void showLong(final Context context, final String msg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
