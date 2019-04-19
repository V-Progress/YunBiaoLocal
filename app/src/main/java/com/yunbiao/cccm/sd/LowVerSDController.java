package com.yunbiao.cccm.sd;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yunbiao.cccm.log.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/2/25.
 */
public class LowVerSDController implements SDController{

    private static LowVerSDController instance;

    private File sdRootDir;
    private File appRootDir;
    private File appResourceDir;
    private Context mContext;

    public static synchronized LowVerSDController instance() {
        if (instance == null) {
            synchronized (LowVerSDController.class){
                if(instance == null){
                    instance = new LowVerSDController();
                }
            }
        }
        return instance;
    }

    private LowVerSDController() {
    }

    public void generateStoragePath(String sdRootDir){
        if(TextUtils.isEmpty(sdRootDir)){
            return;
        }
        this.sdRootDir = new File(sdRootDir);
        LogUtil.D(TAG, "SD卡目录：" + this.sdRootDir.getPath());

        if(this.sdRootDir == null || (!this.sdRootDir.exists())){
            return;
        }

        //云标目录
        appRootDir = new File(sdRootDir, APP_ROOT_DIR);
        if(!appRootDir.exists()){
            appRootDir.mkdirs();
        }
        LogUtil.D(TAG, "根目录：" + appRootDir.getPath());

        //资源目录
        appResourceDir = new File(appRootDir, APP_RESOURCE_DIR);
        if(!appResourceDir.exists()){
            appResourceDir.mkdirs();
        }
        LogUtil.D(TAG, "资源目录：" + appResourceDir.getPath());
    }

    @Override
    public <T> T getAppRootDir() {
        if(appRootDir == null){
            appRootDir = new File(sdRootDir, APP_ROOT_DIR);
        }
        return (T) appRootDir;
    }

    public File getAppResourceDir(){
        if(appResourceDir == null){
            appResourceDir = new File(new File(sdRootDir, APP_ROOT_DIR), APP_RESOURCE_DIR);
        }
        return appResourceDir;
    }

    @Override
    public boolean init(@NonNull Context context, @NonNull String pathOrUri) {
        mContext = context;
        if(mContext == null){
            LogUtil.D("Context为null");
            return false;
        }
        if (TextUtils.isEmpty(pathOrUri)) {
            LogUtil.D("路径或URI为空");
            return false;
        }
        //检测SD卡根目录
        sdRootDir = new File(pathOrUri);
        if(!isSDCanUsed()){
            LogUtil.D("SD卡不可用");
            return false;
        }
        LogUtil.D("SD卡路径："+ sdRootDir.getPath());

        boolean mkdirs ;
        //检测yunbiao目录
        appRootDir = new File(sdRootDir, APP_ROOT_DIR);
        if (appRootDir != null || appRootDir.exists()) {
            mkdirs = true;
        } else {
            LogUtil.D("创建yunbiao目录");
            mkdirs = appRootDir.mkdirs();
        }
        LogUtil.D("yunbiao路径："+ appRootDir.getPath());

        //资源目录
        appResourceDir = new File(appRootDir, APP_RESOURCE_DIR);
        if(!appResourceDir.exists()){
            appResourceDir.mkdirs();
        }
        LogUtil.D(TAG, "资源目录：" + appResourceDir.getPath());

        return mkdirs;
    }

    @Override
    public boolean isSDCanUsed(){
        return sdRootDir != null && sdRootDir.exists() && sdRootDir.canRead() && sdRootDir.canWrite();
    }

    @Override
    public <T> T findResource(String fileName){
        return (T) new File(appResourceDir,fileName);
    }

    @Override
    public <T>OutputStream getOutputStream(T t) throws FileNotFoundException {
        if(t instanceof File){
            File file = (File) t;
            return new FileOutputStream(file,true);
        }
        new Exception("参数类型错误").printStackTrace();
        return null;
    }

    @Override
    public <T>OutputStream getOutputStreamCover(T t) throws FileNotFoundException {
        if(t instanceof File){
            File file = (File) t;
            return new FileOutputStream(file,false);
        }
        new Exception("参数类型错误").printStackTrace();
        return null;
    }

    @Override
    public <T>InputStream getInputStream(T t) throws FileNotFoundException {
        if(t instanceof File){
            File file = (File) t;
            return new FileInputStream(file);
        }
        new Exception("参数类型错误").printStackTrace();
        return null;
    }

}
