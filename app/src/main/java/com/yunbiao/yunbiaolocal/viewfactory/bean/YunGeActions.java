package com.yunbiao.yunbiaolocal.viewfactory.bean;

import android.os.Build;

/**
 * Created by Administrator on 2017/8/10.
 */

public class YunGeActions {

    //启动一下app的am命令
    public final static String AMSTART = "am start -n?com.herewell.cloud.android.player/.AppStartActivity";

    //隐藏 显示
    public final static String BRODCAST_HIDE_PLAYER = "com.oohlink.player.hide";
    public final static String BRODCAST_RESUME_PLAYER = "com.oohlink.player.resume";

    //两个apk的包名
    public final static String APP_PLAYER = "com.herewell.cloud.android.player";
    public final static String APP_UPDATER = "com.herewell.cloud.android.updater";
    public final static String APP_PLAYER_ACTIVITY = "com.herewell.cloud.android.player.AppStartActivity";

    //文件保存路径
    public final static String SAVEPATH = "/sdcard/mnt/sdcard/hsd/yunge";

    //apk
    public final static String APKURLFOUR = "http://res.yungeshidai.com/other/player_floatwindow_android44sign_1040.zip";
    public final static String APKURLFIVE = "http://res.yungeshidai.com/other/player_floatwindow_android51sign_1040.zip";

    public static String getAPKURl() {
        String url = "";
        String release = Build.VERSION.RELEASE;
        if (release.startsWith("5")) {
            url = APKURLFIVE;
        } else {
            url = APKURLFOUR;
        }
        return url;
    }

}
