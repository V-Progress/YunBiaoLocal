package com.yunbiao.cccm;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.yunbiao.cccm.net2.SystemVersion;
import com.yunbiao.cccm.net2.cache.CacheManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/11/19.
 */

public class PathManager {
    private static String KEY_HIGHT_SD_PATH = "ext_sd_Path_h";
    private String APP_ROOT_DIR = "yunbiao";
    private String APP_RESOURCE_DIR = "resource";

    private static PathManager pathManager = new PathManager();
    private final Context context;

    private DocumentFile resDocFileDir;
    private File resFileDir;
    private File appDir;
    private DocumentFile appDocDir;

    public static PathManager instance() {
        return pathManager;
    }

    private PathManager() {
        context = APP.getContext();
    }

    public static void savePath(String sdPath) {
        CacheManager.SP.put(KEY_HIGHT_SD_PATH, sdPath);
    }

    public static String getPath() {
        return CacheManager.SP.get(KEY_HIGHT_SD_PATH, "");
    }

    public void initPath() {
        String path = getPath();
        if (SystemVersion.isLowVer()) {
            init_l(path);
        } else {
            init_h(path);
        }
    }

    private void init_l(String path){
        File sdDir = new File(path);
        Log.e(TAG, "init_l: SD根目录：" + sdDir.getPath() + "，是否可读写：" + sdDir.canRead() + "，" + sdDir.canWrite());

        appDir = new File(sdDir,APP_ROOT_DIR);
        if(appDir == null || !appDir.exists()){
            appDir.mkdirs();
        }
        Log.e(TAG, "init_l: SD根目录：" + appDir.getPath() + "，是否可读写：" + appDir.canRead() + "，" + appDir.canWrite());

        resFileDir = new File(appDir,APP_RESOURCE_DIR);
        if(resFileDir == null || !resFileDir.exists()){
            resFileDir.mkdirs();
        }
        Log.e(TAG, "init_l: SD根目录：" + resFileDir.getPath() + "，是否可读写：" + resFileDir.canRead() + "，" + resFileDir.canWrite());
    }

    private static final String TAG = "PathManager";
    private void init_h(String path){
        DocumentFile sdDir = DocumentFile.fromTreeUri(context, Uri.parse(path));

        appDocDir = sdDir.findFile(APP_ROOT_DIR);
        if(appDocDir == null || !appDocDir.exists()){
            appDocDir = sdDir.createDirectory(APP_ROOT_DIR);
        }

        resDocFileDir = appDocDir.findFile(APP_RESOURCE_DIR);
        if(resDocFileDir == null || !resDocFileDir.exists()){
            resDocFileDir = appDocDir.createDirectory(APP_RESOURCE_DIR);
        }
    }

    public File getAppDir(){
        return appDir;
    }

    public DocumentFile getAppDocDir(){
        return appDocDir;
    }

    public File getResFileDir(){
        return resFileDir;
    }

    public DocumentFile getResDocFileDir(){
        return resDocFileDir;
    }

    public OutputStream openOutputStream(Uri uri) throws FileNotFoundException {
        return context.getContentResolver().openOutputStream(uri,"wa");
    }
}
