package com.yunbiao.yunbiaolocal.utils;

import com.yunbiao.yunbiaolocal.common.ResourceConst;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DeleteResUtil {
    public static void removeExpireFile(){
        File rootDir = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);//yunbiao目录下
        LogUtil.E("当前目录：" + rootDir);
        if (!rootDir.exists()) {
            LogUtil.E("不存在！" + rootDir);
            return;
        }

        try {
            File[] resDirs = rootDir.listFiles();

            //获取今天的日期
            String tDateStr = DateUtil.yyyyMMdd_Format(new Date());
            Date tDate = DateUtil.yyyyMMdd_Parse(tDateStr);

            for (int i = 0; i < resDirs.length; i++) {
                File file = resDirs[i];

                //根据修改时间
                long time = file.lastModified();
                String modifyTimeStr = DateUtil.yyyyMMdd_Format(new Date(time));
                Date modiDate = DateUtil.yyyyMMdd_Parse(modifyTimeStr);
                LogUtil.E("该文件的修改时间为："+modifyTimeStr);

                long day = (tDate.getTime() - modiDate.getTime()) / (24 * 60 * 60 * 1000);
                if (day >= 14) {
                    file.delete();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /***
     * 根据文件夹删数据
     */
    public static void removeExpireResource() {
        File rootDir = new File(ResourceConst.LOCAL_RES.APP_MAIN_DIR);//yunbiao目录下
        LogUtil.E("当前目录：" + rootDir);
        if (!rootDir.exists()) {
            LogUtil.E("不存在！" + rootDir);
            return;
        }

        try {
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
                LogUtil.E("之前日期：" + DateUtil.yyyyMMdd_Format(yDate));
                long day = (tDate.getTime() - yDate.getTime()) / (24 * 60 * 60 * 1000);
                if (day >= 14) {
                    deleteFile(resDir);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
