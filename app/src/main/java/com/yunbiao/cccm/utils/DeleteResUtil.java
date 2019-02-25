package com.yunbiao.cccm.utils;

import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.sdOperator.HighVerSDOperator;
import com.yunbiao.cccm.sdOperator.LowVerSDOperator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DeleteResUtil {
    private static String TAG = "DeleteResUtil";
    private static final int DAY_TAG = 5;//代表删除多少天前的数据


    public static void checkExpireFile(){
        LogUtil.D(TAG, "检查过期文件");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            removeExpireFileLowVer();
        } else {
            removeExpireFileHighVer();
        }
    }

    public static void removeExpireFileHighVer(){
        DocumentFile appResourceDir = HighVerSDOperator.instance().getAppResourceDir();
        if(appResourceDir == null || (!appResourceDir.exists())){
            LogUtil.D(TAG, "不存在！");
            return;
        }

        DocumentFile[] listFiles = HighVerSDOperator.instance().getListFiles();
        LogUtil.D(TAG, "共有：" + listFiles.length + " 个文件");

        //获取今天的日期
        String tDateStr = DateUtil.yyyyMMdd_Format(new Date());
        Date tDate = DateUtil.yyyyMMdd_Parse(tDateStr);

        List<DeleteModel> deleteList = new ArrayList<>();

        for (int i = 0; i < listFiles.length; i++) {
            DocumentFile file = listFiles[i];

            //根据修改时间
            long time = file.lastModified();
            String modifyTimeStr = DateUtil.yyyyMMdd_Format(new Date(time));
            Date modiDate = DateUtil.yyyyMMdd_Parse(modifyTimeStr);

            long day = (tDate.getTime() - modiDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day >= DAY_TAG) {
                boolean delete = file.delete();

                DeleteModel deleteModel = new DeleteModel();
                deleteModel.name = file.getName();
                deleteModel.modiDate = modifyTimeStr;
                deleteModel.delete = delete;

                deleteList.add(deleteModel);
            }
        }
        LogUtil.D(TAG,"共有"+deleteList.size()+"个过期文件：删除情况"+deleteList.toString());

    }
    
    
    public static void removeExpireFileLowVer() {
        File resDir = LowVerSDOperator.instance().getAppResourceDir();
        LogUtil.D(TAG, "当前目录：" + resDir);
        if (!resDir.exists()) {
            LogUtil.D(TAG, "不存在！" + resDir);
            return;
        }

        File[] resFiles = LowVerSDOperator.instance().getListFiles();
        LogUtil.D(TAG, "共有：" + resFiles.length + " 个文件");

        //获取今天的日期
        String tDateStr = DateUtil.yyyyMMdd_Format(new Date());
        Date tDate = DateUtil.yyyyMMdd_Parse(tDateStr);

        List<DeleteModel> deleteList = new ArrayList<>();

        for (int i = 0; i < resFiles.length; i++) {
            File file = resFiles[i];

            //根据修改时间
            long time = file.lastModified();
            String modifyTimeStr = DateUtil.yyyyMMdd_Format(new Date(time));
            Date modiDate = DateUtil.yyyyMMdd_Parse(modifyTimeStr);

            long day = (tDate.getTime() - modiDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day >= DAY_TAG) {
                boolean delete = file.delete();

                DeleteModel deleteModel = new DeleteModel();
                deleteModel.name = file.getName();
                deleteModel.modiDate = modifyTimeStr;
                deleteModel.delete = delete;

                deleteList.add(deleteModel);
            }
        }
        LogUtil.D(TAG,"共有"+deleteList.size()+"个过期文件：删除情况"+deleteList.toString());
    }

    static class DeleteModel{
        String name;
        String modiDate;
        boolean delete;

        @Override
        public String toString() {
            return "DeleteModel{" +
                    "name='" + name + '\'' +
                    ", modiDate='" + modiDate + '\'' +
                    ", delete=" + delete +
                    '}';
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
