package com.yunbiao.cccm.net2;

import android.os.Build;

/**
 * Created by Administrator on 2019/11/23.
 */

public class SystemVersion {
    private static boolean isLowVer = false;

    public static void initVersionTag(){
        isLowVer = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLowVer() {
        return isLowVer;
    }
}
