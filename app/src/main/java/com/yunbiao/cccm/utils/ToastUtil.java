package com.yunbiao.cccm.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2019/1/4.
 */

public class ToastUtil {

    private static Toast toast;

    public static void showShort(Context context, String msg){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showLong(Context context, String msg){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}