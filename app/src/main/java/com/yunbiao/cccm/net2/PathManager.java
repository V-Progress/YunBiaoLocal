package com.yunbiao.cccm.net2;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.yunbiao.cccm.APP;
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

        File appDir = new File(sdDir,APP_ROOT_DIR);
        if(appDir == null || !appDir.exists()){
            appDir.mkdirs();
        }

        resFileDir = new File(appDir,APP_RESOURCE_DIR);
        if(resFileDir == null || !resFileDir.exists()){
            resFileDir.mkdirs();
        }
    }

    private static final String TAG = "PathManager";
    private void init_h(String path){
        DocumentFile sdDir = DocumentFile.fromTreeUri(context, Uri.parse(path));

        DocumentFile appDir = sdDir.findFile(APP_ROOT_DIR);
        if(appDir == null || !appDir.exists()){
            appDir = sdDir.createDirectory(APP_ROOT_DIR);
        }

        resDocFileDir = appDir.findFile(APP_RESOURCE_DIR);
        if(resDocFileDir == null || !resDocFileDir.exists()){
            resDocFileDir = appDir.createDirectory(APP_RESOURCE_DIR);
        }
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
