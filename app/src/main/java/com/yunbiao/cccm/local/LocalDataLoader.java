package com.yunbiao.cccm.local;

import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.PathManager;
import com.yunbiao.cccm.net2.SystemVersion;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2020/1/13.
 */

public class LocalDataLoader {
    private DataCallback mCallback;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public void load(DataCallback callback) {
        mCallback = callback;
        if (SystemVersion.isLowVer()) {
            readOnFile();
        } else {
            readOnDocumentFile();
        }
    }

    public void readOnFile() {
        File appDir = PathManager.instance().getAppDir();
        if (appDir == null || !appDir.exists()) {
            if (mCallback != null) {
                mCallback.resPathNotExists("\"/yunbiao/resource\" 路径不存在，请检查");
            }
            return;
        }

        File[] files = appDir.listFiles();
        if (files == null || files.length <= 0) {
            if (mCallback == null) {
                mCallback.noResource();
            }
            return;
        }

        File currDir = null;
        for (File file : files) {
            String name = file.getName();
            if (!name.matches("20\\d{6}-20\\d{6}")) {
                continue;
            }

            String[] split = name.split("-");
            String startDateStr = split[0];
            String endDateStr = split[1];
            try {
                Date currDate = new Date();
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);

                if (currDate.before(startDate) || currDate.after(endDate)) {
                    continue;
                }

                currDir = file;
                break;
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
        }

        if(currDir == null){
            if(mCallback != null){
                mCallback.noResource();
            }
            return;
        }


    }

    public void readOnDocumentFile() {
        DocumentFile appDocDir = PathManager.instance().getAppDocDir();
        if (appDocDir == null || !appDocDir.exists()) {
            if (mCallback != null) {
                mCallback.resPathNotExists("\"/yunbiao/resource\" 路径不存在，请检查");
            }
            return;
        }

        DocumentFile[] documentFiles = appDocDir.listFiles();
        if (documentFiles == null || documentFiles.length <= 0) {
            if (mCallback == null) {
                mCallback.noResource();
            }
            return;
        }

        DocumentFile currDir = null;
        for (DocumentFile docFile : documentFiles) {
            String name = docFile.getName();
            if (!name.matches("20\\d{6}-20\\d{6}")) {
                continue;
            }

            String[] split = name.split("-");
            String startDateStr = split[0];
            String endDateStr = split[1];
            try {
                Date currDate = new Date();
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);

                if (currDate.before(startDate) || currDate.after(endDate)) {
                    continue;
                }
                currDir = docFile;
                break;
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
        }

        if(currDir == null){
            if(mCallback != null){
                mCallback.noResource();
            }
            return;
        }

    }

    public interface DataCallback {
        void resPathNotExists(String pathName);

        void noResource();
    }
}
