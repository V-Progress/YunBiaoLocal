package com.yunbiao.yunbiaolocal.devicectrl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.devicectrl.actions.JYDActions;
import com.yunbiao.yunbiaolocal.netcore.HeartBeatClient;
import com.yunbiao.yunbiaolocal.cache.ResConstants;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class ScreenShot {

    private static ScreenShot screenShot = null;

    private ScreenShot() {
    }

    public static ScreenShot getInstanse() {
        if (screenShot == null) {
            synchronized (ScreenShot.class) {
                if (screenShot == null) {
                    screenShot = new ScreenShot();
                }
            }
        }
        return screenShot;
    }

    private void sendCutFinish(String sid, String filePath) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("sid", sid);
//        NetTool.communication02(ResConstants.SCREEN_UPLOAD_URL, params, filePath, "screenimage");
    }

    private void cutA20Screen() {
        try {
            // 图片截取
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            String path = ResConstants.SCREEN_CACHE_PATH;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String sid = HeartBeatClient.getDeviceNo();
            String filePath = path + sid + ".png";
            dos.writeBytes("screencap -p " + filePath + "\n");
            dos.writeBytes("exit\n");
            process.waitFor();
            // 图片旋转
            int i = 0;
//            String hwrotation = SystemProperties.get("persist.sys.hwrotation");
//            if (!TextUtils.isEmpty(hwrotation)) {
//                i = Integer.parseInt(hwrotation);
//            } else {
//                int rotation = APP.getMainActivity().getWindowManager().getDefaultDisplay().getRotation();
//                if (rotation == Surface.ROTATION_90) {
//                    i = 90;
//                } else if (rotation == Surface.ROTATION_180) {
//                    i = 180;
//                } else if (rotation == Surface.ROTATION_270) {
//                    i = 270;
//                }
//            }
            Bitmap decodeFile = BitmapFactory.decodeFile(path + sid + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            if (i != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-i);
                decodeFile = Bitmap.createBitmap(decodeFile, 0, 0, decodeFile.getWidth(), decodeFile.getHeight(), matrix, true);
            }
            // 图片压缩
            decodeFile.compress(CompressFormat.JPEG, 20, fileOutputStream);
            fileOutputStream.close();
            // 图片上传
            sendCutFinish(sid, filePath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void cutA83Screen() {
        cutYsScreen();
    }

    private void cutZhsdScreen() {
        String zhpath = "/mnt/internal_sd/Screenshots/";
        String sid = HeartBeatClient.getDeviceNo();
        String zhFilePath = zhpath + sid + ".png";
        File file = new File(zhFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        ZHBroadControl.getZhBroadControl().screenCut(zhpath, sid);
        while (!new File(zhFilePath).exists()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sendCutFinish(sid, zhFilePath);
    }

    private void cutYsScreen() {
        try {
            Bitmap bitmap = snapShotWithStatusBar();
            String path = ResConstants.SCREEN_CACHE_PATH;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String sid = HeartBeatClient.getDeviceNo();
            String filePath = path + sid + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            // 图片压缩
            bitmap.compress(CompressFormat.JPEG, 20, fileOutputStream);
            fileOutputStream.close();
            // 图片上传
            sendCutFinish(sid, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 截屏,通用接口
    public Bitmap snapShotWithStatusBar() {
        View view = APP.getMainActivity().getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        DisplayMetrics dm = new DisplayMetrics();
        APP.getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  // 获取屏幕的宽  像素
        int height = dm.heightPixels; // 获取屏幕的高  像素
        Bitmap bp = Bitmap.createScaledBitmap(bmp, width, height, true);
//        Bitmap bp = Bitmap.createBitmap(bmp, 0, 0, width, height);

        view.destroyDrawingCache();
        return bp;
    }

    //建益达截屏
    private void cutJYDScreen() {
        String sid = HeartBeatClient.getDeviceNo();
        String cutPicPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/screenshot.png";
        File file1 = new File(cutPicPath);
        if (file1.exists()) {
            file1.delete();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        TYTool.sendBroadcast(JYDActions.SCREEN_CAP);
        Intent intent=new Intent(JYDActions.SCREEN_CAP);
        APP.getMainActivity().sendBroadcast(intent);

        while (!new File(cutPicPath).exists()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sendCutFinish(sid, cutPicPath);// 图片上传

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new File(cutPicPath).delete();
    }
//
//    /**
//     * 屏幕截图
//     */
    public void shootScreen() {
        Integer type = CommonUtils.getBroadType();
        switch (type.intValue()) {
            case 0:
                String androidModel = APP.getSmdt().getAndroidModel();
                Log.e(TAG, "AndroidModel: " + androidModel);
                switch (androidModel) {
                    case "SoftwinerEvb":
                        cutA20Screen();
                        break;
                    default:
                        cutA83Screen();
                        break;
                }
                break;
            case 3:
            case 4:
                cutYsScreen();
                break;
            case 1:
                cutZhsdScreen();
                break;
            case 5:
                cutA83Screen();//建益达截屏广播发送失败，暂用本地截屏
//                cutJYDScreen();
                break;
        }
    }
}
