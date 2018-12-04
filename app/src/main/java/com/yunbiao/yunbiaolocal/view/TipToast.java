package com.yunbiao.yunbiaolocal.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.R;

/**
 * Created by Administrator on 2018/12/4.
 */

public class TipToast extends Toast {

    private static TipToast toast;
    private static TextView tvToast;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public TipToast(Context context) {
        super(context);
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public void show() {
        super.show();
    }

    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    /**
     * 初始化Toast
     *
     * @param context 上下文
     * @param text    显示的文本
     */
    private static void initToast(Context context, CharSequence text) {
        try {
            cancelToast();

            toast = new TipToast(context);

            // 获取LayoutInflater对象
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 由layout文件创建一个View对象
            View layout = inflater.inflate(R.layout.layout_tip_toast, null);

            // 吐司上的文字
            tvToast = layout.findViewById(R.id.tv_tip_toast);

            tvToast.setText(text);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, -170);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图标状态 不显示图标
     */
    private static final int TYPE_HIDE = -1;
    /**
     * 图标状态 显示√
     */
    private static final int TYPE_TRUE = 0;
    /**
     * 图标状态 显示×
     */
    private static final int TYPE_FALSE = 1;

    /**
     * 显示Toast
     *
     * @param context 上下文
     * @param text    显示的文本
     * @param time    显示时长
     */
    private static void showToast(Context context, CharSequence text, int time) {
        // 初始化一个新的Toast对象
        initToast(context, text);
        tvToast.setText(text);

        // 设置显示时长
        if (time == Toast.LENGTH_LONG) {
            toast.setDuration(Toast.LENGTH_LONG);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        // 显示Toast
        toast.show();
    }

    public static void showShortToast(Context context,CharSequence text){
        showToast(context,text,Toast.LENGTH_SHORT);
    }

    public static void showLongToast(Context context,CharSequence text){
        showToast(context,text,Toast.LENGTH_LONG);
    }

}
