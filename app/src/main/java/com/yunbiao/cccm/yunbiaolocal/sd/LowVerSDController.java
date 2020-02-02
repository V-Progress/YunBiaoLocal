package com.yunbiao.cccm.yunbiaolocal.sd;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;


import com.yunbiao.cccm.yunbiaolocal.utils.LogUtil;

import java.io.File;

/**
 * Created by Administrator on 2019/2/27.
 */

public class LowVerSDController implements SDController {

    private File sdRootDir;
    private Context mContext;
    private static LowVerSDController instance;
    private File appRootDir;

    public static LowVerSDController instance(){
        if(instance == null){
            synchronized(LowVerSDController.class){
                if(instance == null){
                    instance = new LowVerSDController();
                }
            }
        }
        return instance;
    }

    private LowVerSDController(){}
    
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

        String log = "--";
        for (File file : appRootDir.listFiles()) {
            log += file.getName();
        }
        LogUtil.D("yunbiao目录下所有文件："+ log);
        return mkdirs;
    }

    @Override
    public boolean isSDCanUsed() {
        return sdRootDir != null && sdRootDir.exists() && sdRootDir.canRead() && sdRootDir.canWrite();
    }



    @Override
    public <T> T getAppRootDir() {
        return (T) appRootDir;
    }

    @Override
    public <T> T getSDRootDir() {
        return (T) sdRootDir;
    }


}
