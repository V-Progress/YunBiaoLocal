package com.yunbiao.cccm.sdOperator;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.FileNotFoundException;
import java.io.OutputStream;


/**
 * Created by Administrator on 2019/2/20.
 */

public class HighVerSDController implements SDController{
    private Context mContext;

    private DocumentFile sdRootDir;
    private DocumentFile appRootDir;
    private DocumentFile appResourceDir;

    private static HighVerSDController instance;

    public static synchronized HighVerSDController instance() {
        if (instance == null) {
            synchronized (HighVerSDController.class){
                if(instance == null){
                    instance = new HighVerSDController();
                }
            }
        }
        return instance;
    }

    private HighVerSDController() {
        this.mContext = APP.getContext();
    }

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

        appResourceDir = appRootDir.findFile(APP_RESOURCE_DIR);
        if(appResourceDir == null || (!appResourceDir.exists())){
            appResourceDir = appRootDir.createDirectory(APP_RESOURCE_DIR);
        }
        LogUtil.D(TAG, "RESOURCE目录：" + appResourceDir.getUri().toString());
        return true;
    }

    /***
     * SD卡是否可用
     * @return
     */
    @Override
    public boolean isSDCanUsed() {
        if (sdRootDir == null)
            return false;
        return sdRootDir.exists() && sdRootDir.canRead() && sdRootDir.canWrite();
    }

    /***
     * 在资源目录中查找资源
     * @param fileName
     * @return
     */
    @Override
    public DocumentFile findResource(String fileName) {
        if(appResourceDir == null){
            return null;
        }
        return appResourceDir.findFile(fileName);
    }

    /***
     * 创建视频资源文件（下载时）
     * @param fileName
     * @return
     */
    public DocumentFile createVideoRes(String fileName) {
        if(appResourceDir == null){
            return null;
        }
        return appResourceDir.createFile(videoType, fileName);
    }

    /***
     * 获取当前文件的输出流
     * @param t 类型为DocumentFile 或 File
     * @return
     * @throws FileNotFoundException
     */
    public <T> OutputStream getOutputStream(T t) throws FileNotFoundException {
        if(t instanceof DocumentFile){
            DocumentFile documentFile = (DocumentFile) t;
            return mContext.getContentResolver().openOutputStream(documentFile.getUri(),outputMode);
        }
        new Exception("参数类型错误").printStackTrace();
        return null;
    }

    @Override
    public <T> T getAppRootDir() {
        return (T) appRootDir;
    }

    @Override
    public <T> T getAppResourceDir() {
        return (T) appResourceDir;
    }

    /***
     * 判断文件是否存在
     * @param documentFile
     * @return
     */
    private boolean exists(DocumentFile documentFile) {
        return documentFile == null || (!documentFile.exists()) || (!documentFile.isDirectory());
    }
}
