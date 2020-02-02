package com.yunbiao.cccm.yunbiaolocal.sd;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.yunbiao.cccm.yunbiaolocal.MainActivity;
import com.yunbiao.cccm.yunbiaolocal.utils.LogUtil;

/**
 * Created by Administrator on 2019/2/27.
 */

public class SDUtil {
    public static final String KEY_SD_URI_CACHE = "sdUriCache";
    private static SDUtil instance;
    private static CheckSDListener mListener;
    private static Activity mAct;
    private final int REQUEST_CODE = 111;
    private final int RESULT_OK = -1;
    private final int RESULT_CANCEL = 0;

    public static SDUtil init(@NonNull MainActivity act, @NonNull CheckSDListener listener) {
        mAct = act;
        mListener = listener;
        return instance();
    }

    public static SDUtil instance() {
        if (instance == null) {
            synchronized (SDUtil.class) {
                if (instance == null) {
                    instance = new SDUtil();
                }
            }
        }
        return instance;
    }

    private SDUtil() {
    }

    public interface CheckSDListener {
        void sdCanUsed(boolean isCanUsed);
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

}
