package com.yunbiao.cccm.sdOperator;

import android.text.TextUtils;

import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/2/25.
 */
public class LowVerSDOperator implements SDOperator{

    private static LowVerSDOperator instance;

    private File sdRootFile;
    private File appRootFile;
    private File appResourceDir;

    public static synchronized LowVerSDOperator instance() {
        if (instance == null) {
            instance = new LowVerSDOperator();
        }
        return instance;
    }

    private LowVerSDOperator() {
    }

    public void generateStoragePath(String sdRootDir){
        if(TextUtils.isEmpty(sdRootDir)){
            return;
        }
        sdRootFile = new File(sdRootDir);
        LogUtil.D(TAG, "SD卡目录：" + sdRootFile.getPath());

        if(sdRootFile == null || (!sdRootFile.exists())){
            return;
        }

        //云标目录
        appRootFile = new File(sdRootDir,appRootDirName);
        if(!appRootFile.exists()){
            appRootFile.mkdirs();
        }
        LogUtil.D(TAG, "根目录：" + appRootFile.getPath());

        //资源目录
        appResourceDir = new File(appRootFile,appResourceDirName);
        if(!appResourceDir.exists()){
            appResourceDir.mkdirs();
        }
        LogUtil.D(TAG, "资源目录：" + appResourceDir.getPath());
    }

    public File getAppResourceDir(){
        if(appResourceDir == null){
            appResourceDir = new File(new File(sdRootFile,appRootDirName),appResourceDirName);
        }
        return appResourceDir;
    }

    @Override
    public <T> void generateStoragePath(T t) {
        if(t instanceof String){
            String sdRootDir = (String) t;
            if(TextUtils.isEmpty(sdRootDir)){
                return;
            }
            sdRootFile = new File(sdRootDir);
            LogUtil.D(TAG, "SD卡目录：" + sdRootFile.getPath());

            if(sdRootFile == null || (!sdRootFile.exists())){
                return;
            }

            //云标目录
            appRootFile = new File(sdRootDir,appRootDirName);
            if(!appRootFile.exists()){
                appRootFile.mkdirs();
            }
            LogUtil.D(TAG, "根目录：" + appRootFile.getPath());

            //资源目录
            appResourceDir = new File(appRootFile,appResourceDirName);
            if(!appResourceDir.exists()){
                appResourceDir.mkdirs();
            }
            LogUtil.D(TAG, "资源目录：" + appResourceDir.getPath());
        } else {
            new Exception("参数类型错误").printStackTrace();
        }
    }

    @Override
    public boolean isSDCanUsed(){
        return sdRootFile != null && sdRootFile.exists() && sdRootFile.canRead() && sdRootFile.canWrite();
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
    public <T> T getListFiles() {
        return (T) appResourceDir.listFiles();
    }

}
