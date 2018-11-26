package com.yunbiao.yunbiaolocal.io;

import android.os.SystemClock;
import android.util.Log;

import com.yunbiao.yunbiaolocal.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CopyFile {
    /**
     * 复制文件
     */
    public void copyFiles(final File fromFile,final File toFile) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (fromFile.isDirectory()) {
                    File newFile = new File(toFile, fromFile.getName());
                    newFile.mkdirs();
                    File[] listFiles = fromFile.listFiles();
                    if (listFiles != null)
                        for (File file : listFiles)
                            copyFiles(file, newFile);
                } else if (fromFile.isFile()) {
                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = null;
                    try {
                        bis = new BufferedInputStream(new FileInputStream(fromFile));
                        bos = new BufferedOutputStream(new FileOutputStream(new File(toFile, fromFile.getName())));
                        int i;
                        for (byte[] b = new byte[8192]; (i = bis.read(b)) != -1; ){
                            bos.write(b, 0, i);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (bos != null)
                            try {
                                bos.flush();
                                bos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        if (bis != null)
                            try {
                                bis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        MainActivity.sendMessage("4", null);
                        SystemClock.sleep(500);
                    }
                }
            }
        });
    }

    /**
     * 统计文件数量
     */
    public int fileCount(String path) {
        return fileCount(new File(path));
    }

    /**
     * 统计文件数量
     */
    public int fileCount(File targetFile) {
        int amount = 0;
        if (targetFile.isDirectory()) {
            File[] listFiles = targetFile.listFiles();
            if (listFiles != null)
                for (File file : listFiles)
                    amount += fileCount(file);
        } else if (targetFile.isFile())
            amount++;
        return amount;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String path) {
        deleteFile(new File(path));
    }

    /**
     * 删除文件
     */
    public void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files)
                    deleteFile(f);
        }
        file.delete();
    }

    //拷贝进度监听
    public interface OnProgressListener{
        void onComplete(String fileName);
        void onProgress(long progress,String fileName);
        void onError(Exception e);
    }
}
