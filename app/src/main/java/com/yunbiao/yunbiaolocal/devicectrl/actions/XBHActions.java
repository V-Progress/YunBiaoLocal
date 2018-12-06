package com.yunbiao.yunbiaolocal.devicectrl.actions;

/**
 * Created by jsx on 2017/1/17 0017.
 */

public class XBHActions {
    /**
     * 已经在系统中添加广播接收功能，所以发送已下广播实现
     */
    //重启
    public final static String ACTION_REBOOT = "android.intent.action.reboot";
    //关机
    public final static String ACTION_SHUTDOWN = "android.intent.action.shutdown";
    //屏幕旋转
    public final static String ROTATION_0 = "android.intent.rotation_0";
    public final static String ROTATION_90 = "android.intent.rotation_90";
    public final static String ROTATION_180 = "android.intent.rotation_180";
    public final static String ROTATION_270 = "android.intent.rotation_270";
    //输入源切换
    public final static String CHANGE_TO_HDMI = "com.android.lango.SwitchSourceToHDMI1";
    public final static String CHANGE_TO_VGA = "com.android.lango.SwitchSourceToVGA";
    public final static String CHANGE_TO_AV = "com.android.lango.SwitchSourceToAV1";
}
