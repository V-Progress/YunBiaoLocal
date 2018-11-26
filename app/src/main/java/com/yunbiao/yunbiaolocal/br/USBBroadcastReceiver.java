package com.yunbiao.yunbiaolocal.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageSwitcher;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.MainActivity;
import com.yunbiao.yunbiaolocal.io.CopyFile;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

public class USBBroadcastReceiver extends BroadcastReceiver implements Runnable {
    private String dataString;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataString = intent.getDataString().substring(7);
//        Toast.makeText(context, "dataString---------->"+dataString+ dataString, Toast.LENGTH_LONG).show();
        Log.e(TAG, "dataString---------->"+dataString );
        if (!dataString.matches("/mnt/usbhost\\d")&&!dataString.matches("/storage/usbhost\\d")) {
            Toast.makeText(context, "请插入SD卡或者U盘" + dataString, Toast.LENGTH_SHORT).show();
            MainActivity.sendMessage("0", null);
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        String filePath=Environment.getExternalStorageDirectory().toString();

        Log.e(TAG, "filePath-----------> "+filePath );
        MainActivity.sendMessage("1", null);//显示控制台
        addConsoleText("插入U盘" + dataString);
        File file = new File(dataString + "/yunbiao");
        if (!file.canRead()) {
            addConsoleText("U盘中没有yunbiao目录");
            closeConsole();
            Log.e(TAG, "U盘中没有yunbiao目录 ");
            return;
        }


        File toFile = new File(filePath);
//        File toFile = new File("/mnt/extsd");
//        if (!toFile.canRead()) {
//            addConsoleText("没有SD卡");
//            closeConsole();
//            Log.e(TAG, "没有SD卡");
//            return;
//        }
        Log.e(getClass().getSimpleName(),"正在拷贝文件");
        CopyFile copyFile = new CopyFile();
        int amount = copyFile.fileCount(file);//文件数量
        Log.e(TAG, "文件数量------------>"+amount );
        addConsoleText("文件数量" + amount);
        MainActivity.sendMessage("3", String.valueOf(amount));
        copyFile.copyFiles(file, toFile);//复制文件

        addConsoleText("复制完成");
        //删除文件
        Map<String, File> fileMap = new HashMap<>();
        File[] files = new File(toFile, "yunbiao").listFiles();
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

            String dataString=f.getName().substring(0,f.getName().indexOf("-"));
            fileMap.put(dataString, f);


        }

        SimpleDateFormat formatter2  = new SimpleDateFormat("yyyyMMdd");

//        //删除老目录
        String[] fileKey = fileMap.keySet().toArray(new String[0]);
        for (String dataString:fileKey){
            Log.e(TAG, "dataString--------------------->"+dataString);
            if ( !isSameWeek(dataString,formatter2)){
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

    private boolean isSameWeek(String dataString,SimpleDateFormat formatter){

        try {
            Date  date = formatter.parse(dataString);
            // 0.先把Date类型的对象转换Calendar类型的对象
            Calendar todayCal = Calendar.getInstance();
            Calendar dateCal = Calendar.getInstance();

            todayCal.setTime(new Date());
            dateCal.setTime(date);

            int todayWeek=todayCal.get(Calendar.WEEK_OF_YEAR);
            int dateWeek=dateCal.get(Calendar.WEEK_OF_YEAR);

//            Log.e(TAG, "todayWeek------------> "+todayWeek );
            Log.e(TAG, "dateWeek------------> "+dateWeek );
            // 1.比较当前日期在年份中的周数是否相同
            if (todayWeek==dateWeek|(todayWeek==dateWeek+1)|(todayWeek==dateWeek-1)) {
                return true;
            }else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

}
