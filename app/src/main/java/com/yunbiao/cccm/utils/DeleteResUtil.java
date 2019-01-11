package com.yunbiao.cccm.utils;

import com.yunbiao.cccm.common.ResourceConst;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DeleteResUtil {
    private static String TAG = "DeleteResUtil";

    public static void removeExpireFile() {
        LogUtil.D(TAG, "检查过期文件");

        File resDir = new File(ResourceConst.LOCAL_RES.RES_SAVE_PATH);//yunbiao目录下
        LogUtil.D(TAG, "当前目录：" + resDir);
        if (!resDir.exists()) {
            LogUtil.D(TAG, "不存在！" + resDir);
            return;
        }

        File[] resFiles = resDir.listFiles();
        LogUtil.D(TAG, "共有：" + resFiles.length + " 个文件");

        //获取今天的日期
        String tDateStr = DateUtil.yyyyMMdd_Format(new Date());
        Date tDate = DateUtil.yyyyMMdd_Parse(tDateStr);

        for (int i = 0; i < resFiles.length; i++) {
            File file = resFiles[i];

            //根据修改时间
            long time = file.lastModified();
            String modifyTimeStr = DateUtil.yyyyMMdd_Format(new Date(time));
            Date modiDate = DateUtil.yyyyMMdd_Parse(modifyTimeStr);

            long day = (tDate.getTime() - modiDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day >= 14) {
                boolean delete = file.delete();
                LogUtil.D("删除结果：" + delete);
            }
        }

    }

    /***
     * 根据文件夹删数据
     */
    public static void removeExpireResource() {
        File rootDir = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);//yunbiao目录下
        LogUtil.D(TAG, "当前目录：" + rootDir);
        if (!rootDir.exists()) {
            LogUtil.D(TAG, "不存在！" + rootDir);
            return;
        }

        File[] resDirs = rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.matches("201\\d{5}");
            }
        });

        //获取今天的日期
        String tDateStr = DateUtil.yyyyMMdd_Format(new Date());
        Date tDate = DateUtil.yyyyMMdd_Parse(tDateStr);
        for (int i = 0; i < resDirs.length; i++) {
            File resDir = resDirs[i];

            Date yDate = DateUtil.yyyyMMdd_Parse(resDir.getName());
            LogUtil.D(TAG, "之前日期：" + DateUtil.yyyyMMdd_Format(yDate));
            long day = (tDate.getTime() - yDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day >= 14) {
                boolean delete = resDir.delete();
                LogUtil.D(TAG, "删除结果：" + delete);
                deleteFile(resDir);
            }
        }
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) { //如果是文件夹
            File[] files = file.listFiles();
            for (File file2 : files) {
                deleteFile(file2);
            }
        }
        file.delete();
    }

}
