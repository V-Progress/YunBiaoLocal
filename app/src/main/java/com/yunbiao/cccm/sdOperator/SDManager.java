package com.yunbiao.cccm.sdOperator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yunbiao.cccm.activity.MainActivity;
import com.yunbiao.cccm.utils.LogUtil;

/**
 * Created by Administrator on 2019/1/16.
 */

public class SDManager {

    private static SDManager instance;

    public static final String KEY_SD_URI_CACHE = "sdUriCache";

    private final int REQUEST_CODE = 111;
    private final int RESULT_OK = -1;
    private static CheckSDListener mListener;
    private static Activity mAct;

    public interface CheckSDListener {
        void sdCanUsed(boolean isCanUsed);
    }

    public static SDManager instance(){
        if(instance == null){
            synchronized(SDManager.class){
                if(instance == null){
                    instance = new SDManager();
                }
            }
        }
        return instance;
    }

    private SDManager() {
    }

    public static SDManager init(@NonNull MainActivity act, @NonNull CheckSDListener listener) {
        mAct = act;
        mListener = listener;
        return instance();
    }

    boolean isDocumentTreeOpened = false;

    public void checkSD() {
        LogUtil.E("调用了检测SD卡");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mListener.sdCanUsed(LowVerSDController.instance().init(mAct, "/mnt/extsd"));
        } else {
            String sdUriStr = PreferenceManager.getDefaultSharedPreferences(mAct).getString(KEY_SD_URI_CACHE, "");
            if (!TextUtils.isEmpty(sdUriStr)) {
                mListener.sdCanUsed(HighVerSDController.instance().init(mAct, sdUriStr));
                return;
            }
            if (!isDocumentTreeOpened) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                mAct.startActivityForResult(intent, REQUEST_CODE);
                isDocumentTreeOpened = true;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.E("onActivityResult:"+requestCode +"---"+resultCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            isDocumentTreeOpened = false;
            if (requestCode == REQUEST_CODE) {
                switch (resultCode) {
                    case RESULT_OK:
                        Uri uri = data.getData();
                        mAct.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        PreferenceManager.getDefaultSharedPreferences(mAct).edit().putString(KEY_SD_URI_CACHE, uri.toString()).commit();
                        mListener.sdCanUsed(HighVerSDController.instance().init(mAct, uri.toString()));
                        break;
                    default:
                        mListener.sdCanUsed(false);
                        break;
                }
            }
        }
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

/*
    //取出可移除的SD卡路径
    private String getAppRootOfSdCardRemovable(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        *//**
     * 这一句取的还是内置卡的目录。
     * /storage/emulated/0/Android/data/com.newayte.nvideo.phone/cache
     * 神奇的是，加上这一句，这个可移动卡就能访问了。
     * 猜测是相当于执行了某种初始化动作。
     *//*
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
    }*/
}
