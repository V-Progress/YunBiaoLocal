package com.yunbiao.cccm.yunbiaolocal.sd;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.yunbiaolocal.utils.LogUtil;


/**
 * Created by Administrator on 2019/2/27.
 */

public class HighVerSDController implements SDController {

    private Context mContext;
    private DocumentFile sdRootDir;
    private DocumentFile appRootDir;

    private static HighVerSDController instance;
    
    public static HighVerSDController instance(){
        if(instance == null){
            synchronized(HighVerSDController.class){
                if(instance == null){
                    instance = new HighVerSDController();
                }
            }
        }
        return instance;
    }
    
    private HighVerSDController(){}
    
    @Override
    public boolean init(@NonNull Context context, @NonNull String pathOrUri) {
        mContext = context;
        if (mContext == null) {
            LogUtil.D("Context为null");
            return false;
        }
        if (TextUtils.isEmpty(pathOrUri)) {
            LogUtil.D("路径或URI为空");
            return false;
        }
        Uri sdRootUri = Uri.parse(pathOrUri);
        if(sdRootUri == null){
            LogUtil.D("转换URI出错");
            return false;
        }

        sdRootDir = DocumentFile.fromTreeUri(mContext, sdRootUri);
        if(!isSDCanUsed()){
            LogUtil.D("SD卡不可用");
            return false;
        }
        LogUtil.D("SD卡路径："+sdRootDir.getUri().toString());

        appRootDir = sdRootDir.findFile(APP_ROOT_DIR);
        if(appRootDir == null || (!appRootDir.exists())){
            appRootDir = sdRootDir.createDirectory(APP_ROOT_DIR);
        }
        LogUtil.D("APP根路径："+ appRootDir.getUri().toString());
        return true;
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
