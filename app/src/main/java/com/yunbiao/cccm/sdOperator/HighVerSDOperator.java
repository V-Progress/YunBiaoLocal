package com.yunbiao.cccm.sdOperator;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;


/**
 * Created by Administrator on 2019/2/20.
 */

public class HighVerSDOperator implements SDOperator{
    private Context mContext;

    private DocumentFile sdRootDocument;
    private DocumentFile appRootDocument;
    private DocumentFile appResourceDir;

    private static HighVerSDOperator instance;

    public static synchronized HighVerSDOperator instance() {
        if (instance == null) {
            instance = new HighVerSDOperator();
        }
        return instance;
    }

    private HighVerSDOperator() {
        this.mContext = APP.getContext();
    }

    /***
     * 生成存储路径
     * @param t 类型为 Uri或String
     */
    @Override
    public <T> void generateStoragePath(T t) {
        if(t instanceof Uri){
            Uri uri = (Uri) t;
            //初始化SD卡目录
            if (uri == null) {
                return;
            }
            sdRootDocument = DocumentFile.fromTreeUri(mContext, uri);
            LogUtil.D(TAG, "SD卡目录：" + sdRootDocument.getUri().toString());

            //初始化云标根目录
            if (sdRootDocument == null || (!isSDCanUsed())) {
                return;
            }
            appRootDocument = sdRootDocument.findFile(appRootDirName);
            if (appRootDocument == null || (!appRootDocument.exists()) || (!appRootDocument.isDirectory())) {
                appRootDocument = sdRootDocument.createDirectory(appRootDirName);
            }
            LogUtil.D(TAG, "根目录：" + appRootDocument.getUri().toString());

            //初始化资源目录
            if (appRootDocument == null) {
                return;
            }
            appResourceDir = appRootDocument.findFile(appResourceDirName);
            if (exists(appRootDocument)) {
                appResourceDir = appRootDocument.createDirectory(appResourceDirName);
            }
            LogUtil.D(TAG, "资源目录：" + appResourceDir.getUri().toString());
        }else{
            new Exception("参数类型错误").printStackTrace();
        }
    }

    /***
     * SD卡是否可用
     * @return
     */
    @Override
    public boolean isSDCanUsed() {
        if (sdRootDocument == null)
            return false;
        return sdRootDocument.exists() && sdRootDocument.canRead() && sdRootDocument.canWrite();
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
        return (T) appRootDocument;
    }

    @Override
    public <T> T getAppResourceDir() {
        return (T) appResourceDir;
    }

    @Override
    public <T> T getListFiles() {
        if(appResourceDir != null && appResourceDir.exists()){
            return (T) appResourceDir.listFiles();
        }
        return null;
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
