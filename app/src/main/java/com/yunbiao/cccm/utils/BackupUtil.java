package com.yunbiao.cccm.utils;

import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;
import com.yunbiao.cccm.sd.HighVerSDController;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.sd.SDController;
import com.yunbiao.cccm.utils.DateUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019/4/19.
 */

public class BackupUtil {
    private static final String TAG = "BackupUtil";
    private static SDController controller;
    private static ExecutorService singleThreadExecutor;
    static {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            controller = HighVerSDController.instance();
        } else {
            controller = LowVerSDController.instance();
        }
    }

    public interface ReadBackupListener{
        void getData(List<VideoDataModel.Play> list);
    }

    /***
     * 获取高版本的备份文件
     * @return
     */
    private static DocumentFile getBackupDir_H(){
        DocumentFile appRootDir = controller.getAppRootDir();
        if(appRootDir == null){
            return null;
        }
        Log.e(TAG, "目录: 主目录====="+appRootDir.getUri());

        DocumentFile backupDir = appRootDir.findFile(SDController.APP_LIST_BACKUP);
        if(backupDir == null || backupDir.isFile()){
            backupDir = appRootDir.createDirectory(SDController.APP_LIST_BACKUP);
        }
        Log.e(TAG, "目录: 备份目录====="+backupDir.getUri());
        return backupDir;
    }

    /***
     * 获取低版本的备份文件
     * @return
     */
    private static File getBackupDir_L(){
        File appRootDir = controller.getAppRootDir();
        if(!appRootDir.exists()){
            return null;
        }
        Log.e(TAG, "目录: 主目录====="+appRootDir.getPath());
        File backupDir = new File(appRootDir,SDController.APP_LIST_BACKUP);
        if(!backupDir.exists()){
            backupDir.mkdirs();
        }
        Log.e(TAG, "目录: 备份目录====="+backupDir.getPath());
        return backupDir;
    }

    /**
     * 读取备份文件
     * @param listener
     */
    public static void readBackup(final ReadBackupListener listener){
        final List<VideoDataModel.Play> backupList = new ArrayList<>();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentFile backupDir_h = getBackupDir_H();
                    if(backupDir_h == null){
                        return;
                    }
                    DocumentFile[] documentFiles = backupDir_h.listFiles();
                    Log.e(TAG, "恢复: ----------" + (documentFiles == null? 0 : documentFiles.length));
                    for (DocumentFile documentFile : documentFiles) {
                        Log.e(TAG, "恢复: -----" + documentFile.getUri());
                        try {
//                            String name = documentFile.getName();
//                            Date todayDate = DateUtil.getTodayDate();
//                            Date date = DateUtil.yyyyMMdd_Parse(name);
//                            if(date.after(todayDate)){
//                                continue;
//                            }

                            String read = read(controller.getInputStream(documentFile));
                            Log.e(TAG, "恢复: ---" + read);
                            VideoDataModel.Play play = new Gson().fromJson(read, VideoDataModel.Play.class);
                            backupList.add(play);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    File backupDir_l = getBackupDir_L();
                    if(!backupDir_l.exists()){
                        return;
                    }
                    File[] files = backupDir_l.listFiles();
                    Log.e(TAG, "恢复: ----------" + (files == null ? 0 : files.length));
                    for (File file : files) {
                        Log.e(TAG, "恢复: -----" + file.getPath());
                        try {
//                            String name = file.getName();
//                            Date todayDate = DateUtil.getTodayDate();
//                            Date date = DateUtil.yyyyMMdd_Parse(name);
//                            if(date.after(todayDate)){
//                                continue;
//                            }

                            String read = read(controller.getInputStream(file));
                            Log.e(TAG, "恢复: ---" + read);
                            VideoDataModel.Play play = new Gson().fromJson(read, VideoDataModel.Play.class);
                            backupList.add(play);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(listener != null){
                    listener.getData(backupList);
                }
            }
        });
    }

    /***
     * 读取文件内容
     * @param inputStream
     * @return
     */
    private static String read(InputStream inputStream){
        StringBuffer stringBuffer = new StringBuffer();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(inputStream);

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            //从文件中按字节读取内容，到文件尾部时read方法将返回-1
            while ((bytesRead = bis.read(buffer)) != -1) {

                //将读取的字节转为字符串对象
                String chunk = new String(buffer, 0, bytesRead);
                stringBuffer.append(chunk);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bis != null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }

    /***
     * 备份今日播放列表
     * @param name
     * @param todayPlay
     */
    public static void backup(final String name, final VideoDataModel.Play todayPlay){
        if(TextUtils.isEmpty(name) || todayPlay == null){
            return;
        }

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String todayPlayJson = new Gson().toJson(todayPlay);
                OutputStream outputStream = null;
                Object backupFile;
                FileWriter fw;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentFile backupDir = getBackupDir_H();
                    if(backupDir == null){
                        return;
                    }
                    DocumentFile file = backupDir.findFile(name);
                    if(file == null || !file.exists()){
                        file = backupDir.createFile("text",name);
                    }else if(file.exists() && file.isFile()){
                        boolean delete = file.delete();
                        Log.e(TAG, "备份: -----文件存在，删除："+delete);
                        file = backupDir.createFile("text",name);
                    }
                    Log.e(TAG, "备份: 备份文件====="+file.getUri());
                    backupFile = file;
                } else {
                    File backupDir = getBackupDir_L();
                    File file = new File(backupDir,name);
                    Log.e(TAG, "备份: 备份文件====="+file.getPath());
                    if(file.exists() && file.isFile()){
                        boolean delete = file.delete();
                        Log.e(TAG, "备份: -----文件存在，删除："+delete);
                    }
                    backupFile = file;
                }

                try {
                    outputStream = new BufferedOutputStream(controller.getOutputStream(backupFile));
                    outputStream.write(todayPlayJson.getBytes());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(outputStream != null){
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static void checkExpFile(){
        Log.d(TAG, "checkExpFile: 检测过期的备份列表");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile backupDir_h = getBackupDir_H();
            if(backupDir_h == null || !backupDir_h.exists() || !backupDir_h.isDirectory()){
                return;
            }
            DocumentFile[] documentFiles = backupDir_h.listFiles();
            if(documentFiles == null){
                return;
            }
            Log.d(TAG, "checkExpFile: 共有："+documentFiles.length+"个文件");
            for (DocumentFile documentFile : documentFiles) {
                String name = documentFile.getName();
                Date date = DateUtil.yyyyMMdd_Parse(name);
                Date todayDate = DateUtil.getTodayDate();
                long day = (todayDate.getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                if(day >= 7){
                    Log.d(TAG, "checkExpFile: 删除："+name);
                    documentFile.delete();
                }
            }
        } else {
            File backupDir_l = getBackupDir_L();
            if(backupDir_l == null || !backupDir_l.exists() || !backupDir_l.isDirectory()){
                return;
            }
            File[] files = backupDir_l.listFiles();
            if(files == null){
                return;
            }
            Log.d(TAG, "checkExpFile: 共有："+files.length+"个文件");
            for (File file : files) {
                String name = file.getName();
                Date date = DateUtil.yyyyMMdd_Parse(name);
                Date todayDate = DateUtil.getTodayDate();
                long day = (todayDate.getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                if(day >= 7){
                    Log.d(TAG, "checkExpFile: 删除："+name);
                    file.delete();
                }
            }
        }
    }

}
