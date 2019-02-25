package com.yunbiao.cccm.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.cccm.common.Const;
import com.yunbiao.cccm.common.HeartBeatClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 日志输出到文件工具类
 * @author 李超
 * @DateTime 2017/11/6
 * @Version V1.0.0
 * @Description:
 */
public class Log2FileUtil {

    private static Log2FileUtil INSTANCE = null;
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
    private static int mPId;
    private static String PATH_LOGCAT;
    private static String folderPath;
    private static final int LOG_FILE_MAX_NUM = 2;
    private static String logHead;
    private LogDumper mLogDumper = null;

    private static Log2FileUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Log2FileUtil();
        }
        return INSTANCE;
    }

    private Log2FileUtil() {
        mPId = android.os.Process.myPid();
        String deviceNo = HeartBeatClient.getDeviceNo();
        deviceNo = deviceNo == null? "11111111111" : deviceNo;
        logHead = deviceNo
                .concat(" ")
                .concat(SystemInfoUtil.getVersionName())
                .concat(" ")
                .concat(SystemInfoUtil.getSystemVersion())
                .concat(" ")
                .concat(SystemInfoUtil.getSystemSDK());
    }

    public static void startLogcatManager(Context ctx) {
        if (Const.SYSTEM_CONFIG.IS_LOG_TO_FILE) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // save in SD card first
                folderPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "yunbiao_Log";
            } else {
                // If the SD card does not exist, save in the directory of application.
                folderPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "yunbiao_Log";
            }

            getInstance().start();
        }
    }

    private void start() {
        PATH_LOGCAT = setFolderPath(folderPath);
        if(TextUtils.isEmpty(PATH_LOGCAT)){
            return;
        }
        checkFileNumb(folderPath);
        if (mLogDumper == null){
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        }
        mLogDumper.start();
    }

    private String setFolderPath(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!folder.isDirectory()){
            LogUtil.E("The logcat folder path is not a directory: " + folderPath);
            return null;
        }
        return folderPath.endsWith("/") ? folderPath : folderPath + "/";
    }

    /**
     * 检查文件数量
     * 日志文件值保留最近5天的
     */
    private void checkFileNumb(String folderPath) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".log");
            }
        };
        File file = new File(folderPath);
        File[] files = file.listFiles(filter);
        List<String> list = new ArrayList<>();
        for(File f : files){
            list.add(f.getName().substring(0, 8));
        }
        Collections.sort(list);
        File delFile;
        for (int i=0 ; i<list.size()-LOG_FILE_MAX_NUM ; i++) {
            delFile = new File(folderPath, list.get(i)+".log");
            if (delFile.delete()) {
                LogUtil.D("删除日志文件成功:"+delFile.getName());
            } else {
                LogUtil.D("删除日志文件失败:"+delFile.getName());
            }
        }
    }

    /**
     * 写日志文件监控进程
     */
    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        private String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, simpleDateFormat1.format(new Date()) + ".log"), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /**
             * log level：*:v , *:d , *:w , *:e , *:f , *:s
             * */
            //show log of all level
             cmds = "logcat | grep \"(" + mPID + ")\"";

            //Show the current mPID process level of E and W log.
//            cmds = "logcat *:e | grep \"(" + mPID + ")\"";
//            cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";

            //Print label filtering information
//            cmds = "logcat -s way";
        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                LogUtil.D("日志记录开始");
                logcatProc = Runtime.getRuntime().exec(cmds);

                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line;
                /**
                 * 写日志文件监控进程。
                 * 日志实时写入到文件中推断进程一直存在，while循环一直进行，但又没有像想象中的一直打印"日志记录.."，而是有日志时才打印
                 * 原因分析：mReader.readLine()读logcatProc流时，未读到结束，内部阻塞，while循序等待，故不会一直执行。这也正式我们
                 * 想要的结果，监控进程一直存在但又不会一直执行。
                 */
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((logHead + line + "\n").getBytes());
                    }
                }
                LogUtil.D("日志记录结束");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    private void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    public static void stopLogcatManager() {
        if (Const.SYSTEM_CONFIG.IS_LOG_TO_FILE) {
            Log2FileUtil.getInstance().stop();
        }
    }

    public static File[] queryUploadFiles(){
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".log");
            }
        };
        File file = new File(folderPath);
        File[] files = file.listFiles(filter);
        return files;
    }

    public static String getFolerPath() {
        return folderPath;
    }

}
