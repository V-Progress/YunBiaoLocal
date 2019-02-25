package com.yunbiao.cccm.sdOperator;

import android.support.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/2/25.
 */

public interface SDOperator {
    String TAG = "SDOperator";
    String appRootDirName = "yunbiao";
    String appResourceDirName = "resource";

    String videoType = "video/x-msvideo";
    String outputMode = "wa";

    <T> void generateStoragePath(@NonNull T t);

    boolean isSDCanUsed();

    <T> T findResource(@NonNull String fileName);

    <T>OutputStream getOutputStream(@NonNull T t) throws FileNotFoundException;

    <T> T getAppResourceDir();

    <T> T getListFiles();
}
