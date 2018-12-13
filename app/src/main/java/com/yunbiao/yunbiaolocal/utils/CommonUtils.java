package com.yunbiao.yunbiaolocal.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.cache.CacheManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(currentTime);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 获取网络类型 0无网络 -1位置网络 1WIFI 2ETHERNET 3MOBILE
     */
    public static int getNetType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) APP.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                return 0;
            }
            int type = activeNetworkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return 1;
            } else if (type == ConnectivityManager.TYPE_ETHERNET) {
                return 2;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                return 3;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    /**
     * 获取本机的ip地址 1WIFI 2ETHERNET 3MOBILE
     */
    public static String getIpAddress() {
        String ip = "";
        int netType = getNetType();
        if (netType == 1) {
            ip = getWifiIP();
        } else if (netType == 2) {
            ip = getEthernetIP();
        } else if (netType == 3) {
            ip = getMobileIP();
        } else {
            ip = "UNKNOWN_NETWORK";
        }
        return ip;
    }

    /**
     * 获取wifiIP
     */
    private static String getWifiIP() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String wifiIP = intToIp(ipAddress);
        return wifiIP;
    }

    /**
     * 获取以太网IP
     */
    private static String getEthernetIP() {
        String ethernetIP = "";
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface intf = netInterfaces.nextElement();
                if (intf.getName().toLowerCase().equals("eth0") || intf.getName().toLowerCase().equals("wlan0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ethernetIP = inetAddress.getHostAddress();
                        }
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ethernetIP;
    }

    /**
     * 获取GSM网络的IP地址
     */
    private static String getMobileIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String mobileIP = inetAddress.getHostAddress();
                        return mobileIP;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 判断SDCard是否可用
     */
    private static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡路径
     */
    private static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     *
     * @return
     */
    public static long getSDCardAllSize() {
        if (isSDCardEnable()) {
            StatFs stat = new StatFs(getSDCardPath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getAvailableBlocks() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocks();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获取当前版本号
     */
    public static String getAppVersion(Context context) {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception ignored) {
        }
        return version;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    //CPU个数
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取CPU最大频率，单位KHZ
     *
     * @return
     */
    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        Double cpuGhz = Double.valueOf(0);
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
            cpuGhz = Double.valueOf(result.trim()) / 1000000;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "N/A";
        }
        return String.valueOf(cpuGhz);
    }

    // 获取CPU名字
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            return array[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取xmpp连接情况
     *
     * @return
     */
    public static String getXmppConnected() {
        String connectStatus;
        if (Constants.xmppManager != null && Constants.xmppManager.isConnected()) {
            connectStatus = "在线";
        } else {
            connectStatus = "离线";
        }
        return connectStatus;
    }

    /**
     * 获取连接的wifi名称
     *
     * @return
     */
    public static String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    /**
     * 获取当前屏幕旋转角度
     *
     * @param activity
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    public static int getDisplayRotation(Activity activity) {
        if (activity == null)
            return 0;
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static String getMemoryTotalSize() {
        File innerCardFile = Environment.getExternalStorageDirectory();
        long totalSize = innerCardFile.getTotalSpace();
        return String.valueOf(totalSize / 1024 / 1024);
    }

    public static String getMemoryUsedSize() {
        File innerCardFile = Environment.getExternalStorageDirectory();
        long totalSize = innerCardFile.getTotalSpace();
        long freeSize = innerCardFile.getFreeSpace();
        long usedSize = totalSize - freeSize;
        return String.valueOf(usedSize / 1024 / 1024);
    }

    /**
     * 重启APP
     */
//    public static void reLoadApp() {
//        Intent intent = new Intent();
//        String packgeName = APP.getContext().getPackageName();
//        // 参数1：包名，参数2：程序入口的activity
//        intent.setClassName(packgeName, packgeName + ".MainActivity");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent restartIntent = PendingIntent.getActivity(APP.getContext().getApplicationContext(), -1, intent, 0);
//        // 1秒钟后重启应用
//        AlarmManager mgr = (AlarmManager) APP.getContext().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restartIntent);
//        BaseActivity.finishAll();
//    }

    public static String getAvailaleSize(String path, Context context) {
        StatFs stat = new StatFs(path);    /*获取block的SIZE*/
        long blockSize = stat.getBlockSize();    /*空闲的Block的数量*/
        long availableBlocks = stat.getAvailableBlocks();   /* 返回bit大小值*/
        long totalBlocks = stat.getBlockCount();
        long uedBlocks = totalBlocks - availableBlocks;
        return context.getResources().getString(R.string.used) + Formatter.formatFileSize(APP.getContext(), uedBlocks * blockSize) + context.getResources().getString(R.string.no_used) + Formatter.formatFileSize(APP.getContext(), totalBlocks * blockSize);
    }

    /**
     * 获取主板的信息存到sp里 供以后判断主板厂家使用
     */
    public static void saveBroadInfo() {
        Process process = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
            InputStream outs = process.getInputStream();
            InputStreamReader isrout = new InputStreamReader(outs);
            br = new BufferedReader(isrout, 8 * 1024);
            StringBuffer result = new StringBuffer("");
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            String broadInfo = result.toString();
            LogUtil.E(TAG, "主板信息: " + broadInfo);
            CacheManager.SP.putBroadInfo(broadInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是不是板子的来源厂家
     *
     * @return 0其他（国威）  1中恒  2深圳鸿世达科技  3亿晟科技  4小百合  5建益达
     */
    public static Integer getBroadType() {
        String broad_info = CacheManager.SP.getBroadInfo();
        if (broad_info.contains("zhsd")) {
            return 1;
        } else if (broad_info.contains("yunbiao") || broad_info.contains("lichao")|| broad_info.contains("shizhenxi")) {//yunbiao   shizhenxi
            return 4;
        } else if (broad_info.contains("ubunt")) {
            return 2;
        } else if (broad_info.contains("edge")) {//edge   xqt
            return 3;
        } else if (broad_info.contains("zhoutao")) {
            return 5;
        } else {
            return 0;
        }
    }

    /**
     * 获取线的MAC地址
     */
    public static String getLocalMacAddress() {
        String mac = null;
        try {
            Enumeration localEnumeration = NetworkInterface.getNetworkInterfaces();

            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface) localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();

                if (interfaceName == null) {
                    continue;
                }
                if (interfaceName.equals("eth0")) {
                    mac = convertToMac(localNetworkInterface.getHardwareAddress());
                    if (mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    /**
     * 6.0及以上的安卓系统获取mac为02:00:00:00:00:00，使用这个方法修改
     */
    public static String getSixOSMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value = 0;
            if (b >= 0 && b <= 16) {
                value = b;
                sb.append("0" + Integer.toHexString(value));
            } else if (b > 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * 获取IMEI号
     */
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }

    /**
     * 设备是否有摄像头
     */
//    public static String checkCamera() {//1有  0没有
//        Integer type = CommonUtils.getBroadType();
//        String isHas;
//        if (type == 0) {//荣朗 判断摄像头是否存在 判断dev/video0是否存在
//            File file = new File("dev/video0");
//            boolean exists = file.exists();
//            if (exists) {
//                isHas = "1";
//            } else {
//                isHas = "0";
//            }
//            return isHas;
//        } else {//其他主板
//            int camera = CameraTool.getCamera();
//            if (camera != -1) {//有
//                isHas = "1";
//            } else {
//                isHas = "0";
//            }
//            return isHas;
//        }
//    }

    /**
     * wifi必须打开否则获取不到
     *
     * @return
     */
    public static String getWifiMacAddress(){
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "";//"02:00:00:00:00:00";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "";//"02:00:00:00:00:00";
        }
        return macAddress;
    }
}


