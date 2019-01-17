package com.yunbiao.cccm.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.TimerUtil;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2019/1/16.
 */

public class SDUtil {
    private static String TAG = "SDUtil";
    private static SDUtil instance;
    private Activity mActivity;
    private CheckSDListener mListener;
    private final static String PREF_DEFAULT_URI = "pref_default_uri";

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
    public SDUtil init(Activity activity, CheckSDListener checkSDListener) {
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
        TimerUtil.delayExecute(2000, new TimerUtil.OnTimerListener() {
            @Override
            public void onTimeFinish() {
                if (Build.VERSION.SDK_INT >= 21) {
                    LogUtil.D(TAG, "当前版本高于API21，申请SD卡读取权限");
                    reqSDPermi();
                } else {
                    String appRootOfSdCardRemovable = getAppRootOfSdCardRemovable(mActivity);
                    String extSDPath = CacheManager.SP.getExtSDPath();
                    LogUtil.D(TAG, "当前版本低于API21" + "检测到目录：" + appRootOfSdCardRemovable + "---缓存目录：" + extSDPath);
                    if (!TextUtils.isEmpty(extSDPath) && !TextUtils.equals(appRootOfSdCardRemovable, extSDPath)) {
                        appRootOfSdCardRemovable = extSDPath;
                    }
                    boolean canUsed = isCanUsed(appRootOfSdCardRemovable);
                    LogUtil.D(TAG, "是否可用：" + canUsed);
                    mListener.sdCanUsed(canUsed);
                }
            }
        });
    }

    public void reqSDPermi() {
        String strUri = PreferenceManager.getDefaultSharedPreferences(mActivity).getString("pref_default_uri", null);
        if (TextUtils.isEmpty(strUri)) {
            LogUtil.D(TAG, "uri为空，打开申请界面");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            mActivity.startActivityForResult(intent, 1);
        } else {
            Uri uri = Uri.parse(strUri);
            DocumentFile documentFile = DocumentFile.fromTreeUri(mActivity, uri);
            LogUtil.D(TAG, "uri路径：" + documentFile.getUri() + "是否可读：" + documentFile.canRead() + " 是否可写：" + documentFile.canWrite());
            mListener.sdCanUsed(isCanUsed(documentFile));
        }
    }

    public void onActivityResult(Activity context, int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (requestCode == 1 && resultCode == -1) {
                Uri uri = data.getData();
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_DEFAULT_URI, uri.toString()).commit();
                DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
                LogUtil.D(TAG, "uri路径：" + documentFile.getUri() + "是否可读：" + documentFile.canRead() + " 是否可写：" + documentFile.canWrite());
                mListener.sdCanUsed(isCanUsed(documentFile));

//                createFile(context, documentFile);
            }
        }
    }

    private void createFile(Activity context, DocumentFile documentFile) {
        DocumentFile newFile = documentFile.createFile("text/plain", "M...y Novel");
        OutputStream out = null;
        try {
            out = context.getContentResolver().openOutputStream(newFile.getUri());
            out.write("A long time ago...".getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 是否可用
     * @param filePath 目录路径
     * @return
     */
    public boolean isCanUsed(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead() && file.canWrite();
    }

    public boolean isCanUsed(DocumentFile file) {
        LogUtil.E(TAG, "SDCard目录：" + file.getUri() + "是否存在：" + file.exists() + "---是否可读：" + file.canRead() + "---是否可写：" + file.canWrite());
        return file.exists() && file.canRead() && file.canWrite();
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
