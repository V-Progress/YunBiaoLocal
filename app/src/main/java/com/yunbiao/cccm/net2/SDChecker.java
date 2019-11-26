package com.yunbiao.cccm.net2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.net2.common.Const;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2019/11/20.
 */

public class SDChecker {
    private static SDChecker sdChecker = new SDChecker();
    private static Activity activity;
    private ScheduledExecutorService scheduledExecutorService;

    private final String LOW_SD_PATH = "/mnt/extsd";

    private final String HIGH_USB_DISK1 = "/mnt/usb_storage/USB_DISK1";
    private final String HIGH_USB_DISK2 = "/mnt/usb_storage/USB_DISK2";
    private final String HIGH_USB_DISK3 = "/mnt/usb_storage/USB_DISK3";

    private final String LOW_USB_DISK1 = "/mnt/usbhost1";
    private final String LOW_USB_DISK2 = "/mnt/usbhost2";
    private final String LOW_USB_DISK3 = "/mnt/usbhost3";

    private SDChecker() {
    }

    public static SDChecker instance() {
        return sdChecker;
    }

    private boolean canUsed(File file){
        return file != null & file.exists() & file.canRead() & file.canWrite();
    }

    private Runnable checkSDLow = new Runnable() {
        @Override
        public void run() {
            if(Const.STORAGE_TYPE == Const.TYPE_USB_DISK){
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    File usbFile1 = new File(LOW_USB_DISK1);
                    boolean b1 = canUsed(usbFile1);
                    if(b1){
                        ready(usbFile1.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }

                    File usbFile2 = new File(LOW_USB_DISK2);
                    boolean b2 = canUsed(usbFile2);
                    if(b2){
                        ready(usbFile2.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }

                    File  usbFile3 = new File(LOW_USB_DISK3);
                    boolean b3 = canUsed(usbFile3);
                    if(b3){
                        ready(usbFile3.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }
                } else {
                    File usbFile1 = new File(HIGH_USB_DISK1);
                    boolean b1 = canUsed(usbFile1);
                    if(b1){
                        ready(usbFile1.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }

                    File usbFile2 = new File(HIGH_USB_DISK2);
                    boolean b2 = canUsed(usbFile2);
                    if(b2){
                        ready(usbFile2.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }

                    File  usbFile3 = new File(HIGH_USB_DISK3);
                    boolean b3 = canUsed(usbFile3);
                    if(b3){
                        ready(usbFile3.getPath());
                        scheduledExecutorService.shutdownNow();
                        return;
                    }
                }
            } else {
                File sdPath = new File(LOW_SD_PATH);
                boolean sdCanUsed = sdPath != null & sdPath.exists() & sdPath.canRead() & sdPath.canWrite();

                if (sdCanUsed) {
                    ready(sdPath.getPath());
                    scheduledExecutorService.shutdownNow();
                    return;
                }

            }
            waitSD();
        }
    };

    private final int RESULT_OK = -1;
    private final int REQUEST_CODE = 111;
    private boolean isDocumentTreeOpened = false;
    private Runnable checkSDHigh = new Runnable() {
        @Override
        public void run() {
            String path = PathManager.getPath();
            d("缓存中的路径：" + path);
            if (!path.startsWith("content://")) {
                d("路径异常，重置");
                path = "";
            }

            if (!TextUtils.isEmpty(path)) {
                Uri sdUri = Uri.parse(path);
                DocumentFile sdRootDir = DocumentFile.fromTreeUri(activity, sdUri);
                boolean canUsed = sdRootDir != null & sdRootDir.exists() & sdRootDir.canRead() & sdRootDir.canWrite();
                d("路径是否可用：" + canUsed);
                if (canUsed) {
                    ready(sdRootDir.getUri().toString());
                    scheduledExecutorService.shutdownNow();
                    return;
                }
            }

            d("路径不可用，选择路径");

            if (!isDocumentTreeOpened) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                activity.startActivityForResult(intent, REQUEST_CODE);
                isDocumentTreeOpened = true;
            }

            waitSD();
        }
    };

    private static final String TAG = "SDChecker";

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "onActivityResult: 开始处理");
            isDocumentTreeOpened = false;
            if (requestCode == REQUEST_CODE) {
                Log.e(TAG, "onActivityResult: 是自己的标签");
                switch (resultCode) {
                    case RESULT_OK:
                        Uri sdUri = data.getData();
                        Log.e(TAG, "onActivityResult: 获取了data：" + sdUri.toString());
                        activity.getContentResolver().takePersistableUriPermission(sdUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        DocumentFile sdRootDir = DocumentFile.fromTreeUri(activity, sdUri);
                        Log.e(TAG, "onActivityResult: 获取了DocumentFile：" + sdRootDir);
                        boolean canUsed = sdRootDir.exists() && sdRootDir.canRead() && sdRootDir.canWrite();
                        Log.e(TAG, "onActivityResult: 获取了DocumentFile可用吗：" + canUsed);
                        if (canUsed) {
                            ready(sdRootDir.getUri().toString());
                            scheduledExecutorService.shutdownNow();
                            return;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void check(Activity activity, Callback callback) {
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.callback = callback;
        this.activity = activity;
        startCheck();
        if (SystemVersion.isLowVer()) {
            scheduledExecutorService.scheduleAtFixedRate(checkSDLow, 2, 1, TimeUnit.SECONDS);
        } else {
            scheduledExecutorService.scheduleAtFixedRate(checkSDHigh, 2, 1, TimeUnit.SECONDS);
        }
    }

    private void startCheck() {
        if (callback != null) {
            callback.start();
        }
    }

    private void waitSD() {
        if (callback != null) {
            callback.waitSD();
        }
    }

    private void ready(String path) {

        if (callback != null) {
            callback.ready(path);
        }
    }

    private Callback callback;

    public interface Callback {
        void start();

        void waitSD();

        void ready(String path);
    }

    private void d(String log) {
        Log.e(TAG, log);
    }
}
