package com.yunbiao.cccm.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.yunbiao.cccm.activity.MainActivity;
import com.yunbiao.cccm.sdOperator.HighVerSDOperator;
import com.yunbiao.cccm.sdOperator.LowVerSDOperator;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2019/1/16.
 */

public class SDUtil {

    private static SDUtil instance;
    private MainActivity mActivity;
    private CheckSDListener mListener;
    public final static String PREF_DEFAULT_URI = "pref_default_uri";

    public interface CheckSDListener {
        void sdCanUsed(boolean isCanUsed);
    }

    public synchronized static SDUtil instance() {
        if (instance == null) {
            instance = new SDUtil();
        }
        return instance;
    }

    /***
     * 初始化
     * @param activity 当前activity
     * @param checkSDListener 检测SD监听
     * @return
     */
    public SDUtil init(MainActivity activity, CheckSDListener checkSDListener) {
        LogUtil.E("初始化SDUtil");
        mActivity = activity;
        mListener = checkSDListener;
        return instance;
    }

    private SDUtil() {
    }

    /***
     * 检测SD卡
     * @return
     */
    public void checkSD() {
        LogUtil.E("开始检查SD卡");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.E("当前版本高于API21，申请SD卡读取权限");
            reqSDPermi();
        } else {
//            String appRootOfSdCardRemovable = getAppRootOfSdCardRemovable(mActivity);
//            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
//            String extSDPath = defaultSharedPreferences.getString("extSD", "");
//            LogUtil.E("当前版本低于API21" + "检测到目录：" + appRootOfSdCardRemovable + "---缓存目录：" + extSDPath);
//            if (!TextUtils.isEmpty(extSDPath) && !TextUtils.equals(appRootOfSdCardRemovable, extSDPath)) {
//                appRootOfSdCardRemovable = extSDPath;
//            }
//            mListener.sdCanUsed(isCanUsed(appRootOfSdCardRemovable));

            LowVerSDOperator.instance().generateStoragePath("/mnt/extsd");

            mListener.sdCanUsed(LowVerSDOperator.instance().isSDCanUsed());
        }
    }

    /***
     * 申请SD卡读写权限
     */
    public void reqSDPermi() {
        String strUri = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(PREF_DEFAULT_URI, null);
        if (TextUtils.isEmpty(strUri)) {
            LogUtil.E("uri为空，打开申请界面");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            mActivity.startActivityForResult(intent, 1);
        } else {
            Uri uri = Uri.parse(strUri);
            LogUtil.E("缓存的uri为："+uri);
            HighVerSDOperator.instance().generateStoragePath(uri);
            LogUtil.E("检测SD卡是否可用"+HighVerSDOperator.instance().isSDCanUsed());
            mListener.sdCanUsed(HighVerSDOperator.instance().isSDCanUsed());
        }
    }

    public void onActivityResult(/*Activity context, */int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (requestCode == 1 && resultCode == -1) {
                Uri uri = data.getData();
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                mActivity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                PreferenceManager.getDefaultSharedPreferences(mActivity).edit().putString(PREF_DEFAULT_URI, uri.toString()).commit();
                HighVerSDOperator.instance().generateStoragePath(uri);
                mListener.sdCanUsed(HighVerSDOperator.instance().isSDCanUsed());
            }
        }
    }
    private int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.D("123", "已取得权限");
            } else {
                LogUtil.D("123", "未取得权限");
            }
        }
    }

    public static boolean isCanUsedHighVer(){
        return HighVerSDOperator.instance().isSDCanUsed();
    }

    public static boolean isCanUsedLowVer(){
        return LowVerSDOperator.instance().isSDCanUsed();
    }

    /***
     * 是否是U盘
     * @param path
     * @return
     */
    public static boolean isUSBDisk(String path) {
        return path.contains("usbhost") || path.contains("usb_storage") || path.contains("udisk") || path.contains("USB_DISK");
    }

    /***
     * 是否是SD卡
     * @param path
     * @return
     */
    public static boolean isSDCard(String path) {
        return path.contains("extsd") || path.contains("external_sd") || path.contains("sd") || path.contains("ext");
    }

    //取出可移除的SD卡路径
    private String getAppRootOfSdCardRemovable(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        /**
         * 这一句取的还是内置卡的目录。
         * /storage/emulated/0/Android/data/com.newayte.nvideo.phone/cache
         * 神奇的是，加上这一句，这个可移动卡就能访问了。
         * 猜测是相当于执行了某种初始化动作。
         */
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                if ((Boolean) isRemovable.invoke(storageVolumeElement)) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
