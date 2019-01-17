package com.yunbiao.cccm.utils;

import android.util.Log;

import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.resolve.VideoDirectoryFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/30.
 */

public class CopyUtil {

    private copyFileListener baseCopyListener;
    private static CopyUtil copyUtil;
    private final String YUNBIAO_DIR = "/yunbiao";
    private int fileCount;
    private int fileNum = 0;

    public synchronized static CopyUtil getInstance() {
        if (copyUtil == null) {
            copyUtil = new CopyUtil();
        }
        return copyUtil;
    }

    public CopyUtil() {
    }

    public void USB2Local(final String usbFilePath, final copyFileListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    throw new NullPointerException("listener can not be null!");
                }
                baseCopyListener = listener;
                String usbPath = usbFilePath + YUNBIAO_DIR;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //检查目录
                File usbFile = new File(usbPath);
                if (!usbFile.canRead()) {
                    baseCopyListener.onNoFile();
                    LogUtil.D("CopyUtil", "U盘中没有yunbiao目录 ");
                    return;
                }

                baseCopyListener.onCopyStart(usbPath);

                //检查文件数量
                fileCount = fileCount(usbFile);//文件数量
                baseCopyListener.onFileCount(fileCount);

                //初始化本地目录
                File localFile = new File(ResourceConst.LOCAL_RES.EXTERNAL_ROOT_DIR);

                //删除老文件
//                deleteOldFile(localFile);

                //开始拷贝
                copyFiles(usbFile, localFile);//复制文件

                baseCopyListener.onFinish();
            }
        }).start();
    }

    /**
     * 复制文件
     */
    private void copyFiles(final File usbFile, final File localFile) {

        LogUtil.E(usbFile.getAbsolutePath() + "----->"+localFile.getAbsolutePath());

        if (usbFile.isDirectory()) {
            File newFile = new File(localFile, usbFile.getName());
            newFile.mkdirs();
            File[] listFiles = usbFile.listFiles();
            if (listFiles != null)
                for (File file : listFiles)
                    copyFiles(file, newFile);
        } else if (usbFile.isFile()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(usbFile));
                bos = new BufferedOutputStream(new FileOutputStream(new File(localFile, usbFile.getName())));
                int i;
                for (byte[] b = new byte[8192]; (i = bis.read(b)) != -1; ) {
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

                baseCopyListener.onCopying(++fileNum);
                if (fileNum >= fileCount) {
                    fileNum = 0;
                    baseCopyListener.onCopyComplete();
                }
            }
        }
    }

    /**
     * 复制文件
     */
    private void copyFiles2(final File usbFile, final File localFile) {
        if (usbFile.isDirectory()) {
            File newFile = new File(localFile, usbFile.getName());
            newFile.mkdirs();
            File[] listFiles = usbFile.listFiles();
            if (listFiles != null)
                for (File file : listFiles)
                    copyFiles(file, newFile);
        } else if (usbFile.isFile()) {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(usbFile));
                bos = new BufferedOutputStream(new FileOutputStream(new File(localFile, usbFile.getName())));
                int i;
                for (byte[] b = new byte[8192]; (i = bis.read(b)) != -1; ) {
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

                baseCopyListener.onCopying(++fileNum);
                if (fileNum >= fileCount) {
                    fileNum = 0;
                    baseCopyListener.onCopyComplete();
                }
            }
        }
    }

    private void deleteOldFile(final File localFile) {
        //删除文件
        Map<String, File> fileMap = new HashMap<>();
        File[] files = new File(localFile, "yunbiao").listFiles(new VideoDirectoryFilter());
        if(files == null || files.length <= 0){
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                baseCopyListener.onDeleteFile(f.getPath());
                f.delete();
                continue;
            }
            if (!f.getName().matches("\\d{8}-\\d{8}")) {
                baseCopyListener.onDeleteFile(f.getPath());
                deleteFile(f);
                continue;
            }

            String ds = f.getName().substring(0, f.getName().indexOf("-"));
            fileMap.put(ds, f);
        }

        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");

        //删除老目录
        String[] fileKey = fileMap.keySet().toArray(new String[0]);
        for (String fk : fileKey) {
            if (!isSameWeek(fk, formatter2)) {
                File f = fileMap.get(fk);
                baseCopyListener.onDeleteFile(f.getPath());
                deleteFile(f);
                continue;
            }

        }
    }

    /**
     * 统计文件数量
     */
    private int fileCount(File targetFile) {
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
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files)
                    deleteFile(f);
        }
        file.delete();
    }

    private boolean isSameWeek(String dataString, SimpleDateFormat formatter) {

        try {
            Date date = formatter.parse(dataString);
            // 0.先把Date类型的对象转换Calendar类型的对象
            Calendar todayCal = Calendar.getInstance();
            Calendar dateCal = Calendar.getInstance();

            todayCal.setTime(new Date());
            dateCal.setTime(date);

            int todayWeek = todayCal.get(Calendar.WEEK_OF_YEAR);
            int dateWeek = dateCal.get(Calendar.WEEK_OF_YEAR);

            // 1.比较当前日期在年份中的周数是否相同
            if (todayWeek == dateWeek | (todayWeek == dateWeek + 1) | (todayWeek == dateWeek - 1)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
