package com.yunbiao.cccm.net2;

import android.os.Build;
import android.util.Log;

import com.yunbiao.cccm.net2.common.Const;

/**
 * Created by Administrator on 2019/11/23.
 */

public class SystemVersion {
    private static final String TAG = "SystemVersion";
    private static boolean isLowVer = true;//低版本

    private static boolean isInsertFirst = true;//是否插播在前
    private static boolean hasInsert = false;//是否有插播

    public static void initVersionTag(){
        // TODO: 2019/11/26
        isLowVer = Const.STORAGE_TYPE == Const.TYPE_USB_DISK || Const.STORAGE_TYPE == Const.TYPE_ENVIRONMENT_STORAGE ? true : Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP;

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
