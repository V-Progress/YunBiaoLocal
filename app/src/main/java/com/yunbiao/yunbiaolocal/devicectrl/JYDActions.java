package com.yunbiao.yunbiaolocal.devicectrl;

/**
 * Created by jsx on 2017/1/17 0017.
 */

public class JYDActions {
    /**
     * 建益达  A90 主板
     */

    //重启
    public final static String ACTION_REBOOT = "android.intent.action.reboot";

    //关机
    public final static String ACTION_SHUTDOWN = "android.intent.action.shutdown";

    //休眠（关闭屏幕背光，网络保持连接）
    public final static String ACTION_GOTOSLEEP = "android.intent.action.gotosleep";

    //唤醒休眠（打开屏幕背光）
    public final static String ACTION_EXITSLEEP = "android.intent.action.exitsleep";

    //屏幕旋转
    public final static String ROTATION_0 = "android.intent.rotation_0";  //旋转0度
    public final static String ROTATION_90 = "android.intent.rotation_90"; //旋转90度
    public final static String ROTATION_180 = "android.intent.rotation_180"; //旋转180度
    public final static String ROTATION_270 = "android.intent.rotation_270"; //旋转270度

    //RTC时间更新
    /*播放器会同步信发系统服务器时间然后设置为系统时间，系统在收到该广播后，获取当前系统时间，
    然后将系统时间设置到RTC芯片内，开机重启后需要读取RTC芯片时间然后同步系统时间*/
    public final static String RTC_TIME_UPDATE = "android.tvservice.intent.action.RTC_TIME_UPDATE";

    //截图
    /*系统收到该命令后，截取当前系统的画面，能够截取视频，图片命名为screenshot.png,保存路劲如下
    Environment.getExternalStorageDirectory().getAbsolutePath()+"/screenshot.png"*/
    public final static String SCREEN_CAP = "android.intent.action.screencap";

    //APP静默安装
    /* 发送该广播时会携带被安装APP的安装路径，解析如下：
    String apppath = intent.getStringExtra("apppath");
    得到路径的形式如：/mnt/sdcard/xxx/xxx/xxx.apk ,在静默安装的时候”pm install -r -d path” ,需要传入-d参数，可以低版本覆盖安装*/
    public final static String ACTION_INSTALL_APP = "com.android.lango.installapp";

    //关闭第三方应用的广播
    /*发送该广播时，会将第三方应用的包名传递过来，解析如下
    String packagename = intent.getStringExtra("packagename");
    然后根据包名关闭该APP*/
    public final static String ACTION_KILL_BACKGROUND_APP = "com.android.lango.killapp";

    //监听系统点击的广播 在系统接收到点击事件后，发出该广播即可
    public final static String START_LTV_MANAGE_SERVER = "xbh.intent.action.start_ltvmanageservice";

    //定时开关机
    /* 携带的数据格式如下：
    intent.putString(“poweronoff”,value);
    如果是星期常开：value如下：3*0-1-2-3-4-5-6，
    3表示星期常开，0-1-2-3-4-5-6示周一至周日，星期任意选择
    如果为星期定时：value如下：4*0-1-2-3-4-5-6*00:00-01:59*02:00-02:59*03:00-03:59*04:00-23:59，
    4表示星期定时，0-1-2-3-4-5-6表示周一至周日，星期任意选择
    00:00-01:59：00:00表示开机时间 01:59表示关机时间，时间段不限制*/
    public final static String SET_POWER_ON_OFF = "android.intent.action.setpoweronoff";


    public final static String UPDATEALARM = "com.byteflyer.updatealarm";


}
