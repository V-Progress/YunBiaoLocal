package com.yunbiao.cccm.yunbiaolocal.br;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yunbiao.cccm.yunbiaolocal.APP;
import com.yunbiao.cccm.yunbiaolocal.MainActivity;
import com.yunbiao.cccm.yunbiaolocal.io.CopyFile;
import com.yunbiao.cccm.yunbiaolocal.sd.LowVerSDController;
import com.yunbiao.cccm.yunbiaolocal.sd.SDUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class USBBroadcastReceiver extends BroadcastReceiver {
    private String dataString;
    private String tempAction;


    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        dataString = intent.getDataString().substring(7);
        Log.e(TAG, "dataString--->" + dataString + ",action--->" + action);

        if (TextUtils.equals(action, tempAction)) {
            return;
        }
        tempAction = action;

        if (TextUtils.equals(Intent.ACTION_MEDIA_MOUNTED, action)) {
            if (SDUtil.isUSBDisk(dataString)) {
                Toast.makeText(context, "检测到U盘" + dataString, Toast.LENGTH_SHORT).show();
                new Thread(new CopyRunnable(context)).start();
            } else if (SDUtil.isSDCard(dataString)) {
                Toast.makeText(context, "检测到SD卡" + dataString, Toast.LENGTH_SHORT).show();
                if (APP.getMainActivity() != null && APP.getMainActivity().isForeground()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SDUtil.instance().checkSD();
                        }
                    },1000);

                }
            } else {
                Toast.makeText(context, "请插入SD卡或者U盘" + dataString, Toast.LENGTH_SHORT).show();
                MainActivity.sendMessage("0", null);
            }
        } else if ((TextUtils.equals(Intent.ACTION_MEDIA_REMOVED, action) || TextUtils.equals(Intent.ACTION_MEDIA_UNMOUNTED, action))
                & SDUtil.isSDCard(dataString)) {
            Toast.makeText(context, "SD卡已移除", Toast.LENGTH_SHORT).show();
            if (APP.getMainActivity() != null && APP.getMainActivity().isForeground()) {
                SDUtil.instance().checkSD();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SDUtil.KEY_SD_URI_CACHE, "").commit();
                }
            },800);
        }
    }

    class CopyRunnable implements Runnable {
        private Context mContext;

        public CopyRunnable(Context context) {
            mContext = context;
        }

        @Override
        public void run() {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (!LowVerSDController.instance().isSDCanUsed()) {
                    Log.e(TAG, "SD-----------> 不可用");
                    return;
                }

                MainActivity.sendMessage("1", null);//显示控制台
                addConsoleText("插入U盘" + dataString);
                File file = new File(dataString + "/yunbiao");
                if (!file.canRead()) {
                    addConsoleText("U盘中没有yunbiao目录");
                    closeConsole();
                    Log.e(TAG, "U盘中没有yunbiao目录 ");
                    return;
                }

                File sdRootDir = LowVerSDController.instance().getSDRootDir();
                Log.e(TAG, "appRootDir-----------> " + sdRootDir.getPath());

                CopyFile copyFile = new CopyFile();
                int amount = copyFile.fileCount(file);//文件数量
                Log.e(TAG, "文件数量------------>" + amount);
                addConsoleText("文件数量" + amount);
                MainActivity.sendMessage("3", String.valueOf(amount));
//                copyFile.copyFiles(file, toFile);//复制文件
                copyFile.copyFiles(file, sdRootDir);//复制文件
                addConsoleText("复制完成");
                //删除文件
                Map<String, File> fileMap = new HashMap<>();
//                File[] files = new File(toFile, "yunbiao").listFiles();
                File[] files = new File(sdRootDir, "yunbiao").listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        addConsoleText("删除文件" + f.getPath());
                        f.delete();
                        continue;
                    }
                    if (!f.getName().matches("\\d{8}-\\d{8}")) {
                        addConsoleText("删除目录" + f.getPath());
                        copyFile.deleteFile(f);
                        continue;
                    }

                    String dataString = f.getName().substring(0, f.getName().indexOf("-"));
                    fileMap.put(dataString, f);


                }

                SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");

//        //删除老目录
                String[] fileKey = fileMap.keySet().toArray(new String[0]);
                for (String dataString : fileKey) {
                    Log.e(TAG, "dataString--------------------->" + dataString);
                    if (!isSameWeek(dataString, formatter2)) {
                        File f = fileMap.get(dataString);
                        addConsoleText("删除目录" + f.getPath());
                        copyFile.deleteFile(f);
                        continue;
                    }

                }
                closeConsole();

//        Long[] fileKey = fileMap.keySet().toArray(new Long[0]);
//        Arrays.sort(fileKey);
//        Long current = Long.valueOf(new SimpleDateFormat("yyyyMMddyyyyMMdd").format(new Date()));
//        for (Long l : fileKey) {
//            if (current == 0) {
//                File f = fileMap.get(l);
//                addConsoleText("删除目录" + f.getPath());
//                copyFile.deleteFile(f);
//                continue;
//            }
//            if (l < current)
//                current = 0L;
//        }

            } else {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Android 5.0及以上版本暂不支持U盘拷贝", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void addConsoleText(String text) {
        SystemClock.sleep(1000);
        MainActivity.sendMessage("2", text);
    }

    private void closeConsole() {
        addConsoleText("加载视频");
        SystemClock.sleep(20000);
        MainActivity.sendMessage("0", null);
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

//            Log.e(TAG, "todayWeek------------> "+todayWeek );
            Log.e(TAG, "dateWeek------------> " + dateWeek);
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
