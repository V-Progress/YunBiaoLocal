package com.yunbiao.cccm.net2;

import android.os.Build;
import android.util.Log;

/**
 * Created by Administrator on 2019/11/23.
 */

public class SystemVersion {
    private static final String TAG = "SystemVersion";
    private static boolean isLowVer = true;
    private static boolean isInsertFirst = true;
    private static boolean hasInsert = false;

    public static void initVersionTag(){
        isLowVer = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP;

        Log.e(TAG, "initVersionTag: 当前系统版本：" + Build.VERSION.SDK_INT);
        Log.e(TAG, "initVersionTag: 目标系统版本：" + Build.VERSION_CODES.LOLLIPOP);
        Log.e(TAG, "initVersionTag: 是否低于目标：" + isLowVer);
    }

    public static boolean isLowVer() {
        return isLowVer;
    }

    public static boolean isInsertFirst() {
        return isInsertFirst;
    }

    public static void setIsInsertFirst(boolean isInsertFirst) {
        SystemVersion.isInsertFirst = isInsertFirst;
    }

    public static boolean isHasInsert() {
        return hasInsert;
    }

    public static void setHasInsert(boolean hasInsert) {
        SystemVersion.hasInsert = hasInsert;
    }
}
