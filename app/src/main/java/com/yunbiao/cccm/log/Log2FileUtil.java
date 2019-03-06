package com.yunbiao.cccm.log;

import android.content.Context;
import android.os.Environment;

import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.Const;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.SystemInfoUtil;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * 日志输出到文件工具类
 *
 * @author 李超
 * @DateTime 2017/11/6
 * @Version V1.0.0
 * @Description:
 */
public class Log2FileUtil {

    private String TAG = getClass().getSimpleName();

    private static Log2FileUtil INSTANCE;

    private static final int LOG_FILE_MAX_NUM = 2;

    private LogDumper mLogDumper = null;
    private static int mPId;
    private static StringBuilder logHead = new StringBuilder();
//    private static File folder;

    private static Log2FileUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Log2FileUtil();
        }
        return INSTANCE;
    }

    private Log2FileUtil() {
        mPId = android.os.Process.myPid();
        logHead.append("APP_VER:"+SystemInfoUtil.getVersionName())
                .append("\n")
                .append("SYSTEM_SDK:"+SystemInfoUtil.getSystemSDK())
                .append("\n")
                .append("SYSTEM_VER:"+SystemInfoUtil.getSystemVersion())
                .append("\n")
                .append("BOARD_INFO:"+CacheManager.SP.getBroadInfo())
                .append("\n")
                .append("DEVICE_NO:" + HeartBeatClient.getDeviceNo())
                .append("\n")
                .append("------------------------------------------------------------------------------------------")
                .append("\n");
    }

    public static void startLogcatManager(Context ctx) {
        if (Const.SYSTEM_CONFIG.IS_LOG_TO_FILE) {
            getInstance().start();
        }
    }

    private void start() {
        File folder = new File(Environment.getExternalStorageDirectory(), "yunbiao_Log");
        if (!folder.exists()) {
            boolean mkdirs = folder.mkdirs();
            if (!mkdirs) {
                LogUtil.E(TAG, "创建日志存储目录失败");
            }
        }

        //检查过期文件
        checkExpiFile(folder);

        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), folder);
        }
        mLogDumper.start();
    }

    /**
     * 检查文件数量
     * 日志文件值保留最近5天的
     */
    private void checkExpiFile(File folder) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".log");
            }
        };
        File[] logFiles = folder.listFiles(filter);
        if(logFiles == null || logFiles.length <= 0){
            LogUtil.E(TAG,"日志目录中没有文件");
            return;
        }
        for (File logFile : logFiles) {
            String name = logFile.getName().substring(0, 8);
            Date fileDate = DateUtil.yyyyMMdd_Parse(name);
            Date todayDate = DateUtil.getTodayDate();
            long day = (todayDate.getTime() - fileDate.getTime()) / (24 * 60 * 60 * 1000);
            if (day > LOG_FILE_MAX_NUM) {
                boolean delete = logFile.delete();
                if (!delete) {
                    LogUtil.E(TAG, "过期日志删除失败");
                }
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

        public LogDumper(String pid, File dir) {
            mPID = pid;

            try {
                File logFile = new File(dir, DateUtil.getTodayStr() + ".log");
                out = new FileOutputStream(logFile, true);
                if(logFile.length() <= 0){
                    out.write(logHead.toString().getBytes());
                }
            } catch (IOException e) {
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
                LogUtil.D(TAG,"\n----------------------日志记录开始------------------------");
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
                        out.write((line + "\n").getBytes());
                    }
                }
                LogUtil.D("-------------------------日志记录结束-----------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                close(mReader);
                close(out);
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

//    public static File[] queryUploadFiles() {
//        FilenameFilter filter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String filename) {
//                return filename.endsWith(".log");
//            }
//        };
//        File[] files = folder.listFiles(filter);
//        return files;
//    }
//
//    public static String getFolerPath() {
//        return folder.getPath();
//    }

    private void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
