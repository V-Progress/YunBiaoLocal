package com.yunbiao.cccm.sd;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/2/25.
 */

public interface SDController {
    String TAG = "SDOperator";
    String APP_ROOT_DIR = "yunbiao";
    String APP_RESOURCE_DIR = "resource";
    String APP_LIST_BACKUP = "listBackUp";

    String videoType = "video/x-msvideo";
    String outputMode = "wa";

    /***
     * 初始化SD卡路径
     * @param pathOrUri SD卡根路径
     * @return SD卡是否可用
     */
    boolean init(@NonNull Context context, @NonNull String pathOrUri);


    boolean isSDCanUsed();

    <T> T findResource(@NonNull String fileName);

    <T>OutputStream getOutputStream(@NonNull T t) throws FileNotFoundException;

    <T>OutputStream getOutputStreamCover(@NonNull T t) throws FileNotFoundException;

    <T>InputStream getInputStream(@NonNull T t) throws FileNotFoundException;

    <T> T getAppResourceDir();

    <T> T getAppRootDir();
}
