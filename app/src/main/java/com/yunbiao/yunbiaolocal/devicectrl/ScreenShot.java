package com.yunbiao.yunbiaolocal.devicectrl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.devicectrl.actions.JYDActions;
import com.yunbiao.yunbiaolocal.netcore.HeartBeatClient;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ScreenShot {

    private static ScreenShot screenShot = null;
    private final String CUT_SCREEN_NAME = "/screenshot.png";

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
        communication02(ResourceConst.REMOTE_RES.SCREEN_UPLOAD_URL, params, filePath, "screenimage");
    }

    private void cutA20Screen() {
        try {
            // 图片截取
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            String path = ResourceConst.LOCAL_RES.SCREEN_CACHE_PATH;
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
            String path = ResourceConst.LOCAL_RES.SCREEN_CACHE_PATH;
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
        String cutPicPath = ResourceConst.LOCAL_RES.EXTERNAL_DIR + CUT_SCREEN_NAME;
        File file1 = new File(cutPicPath);
        if (file1.exists()) {
            file1.delete();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    /**
     * @param urlString 对应的URL 页面  只发送普通数据 ,调用此方法
     * @param params    需要发送的相关数据 包括调用的方法
     * @param imageuri  图片或文件手机上的地址 如:sdcard/photo/123.jpg
     * @param img       图片名称
     */
    private void communication02(String urlString, Map<String, Object> params, String imageuri, String img) {
        String result = "";

        String end = "\r\n";
        // 是我定义的上传URL
        String MULTIPART_FORM_DATA = "multipart/form-data";
        String BOUNDARY = "---------7d4a6d158c9"; // 数据分隔线
        String imguri = "";
        if (!imageuri.equals("")) {
            imguri = imageuri.substring(imageuri.lastIndexOf("/") + 1);// 获得图片或文件名称
        }
        if (!urlString.equals("")) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);// 允许输入
                conn.setDoOutput(true);// 允许输出
                conn.setUseCaches(false);// 不使用Cache
                conn.setConnectTimeout(6000);// 6秒钟连接超时
                conn.setReadTimeout(60000);// 6秒钟读数据超时
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);

                StringBuilder sb = new StringBuilder();

                // 上传的表单参数部分，格式请参考文章
                for (Map.Entry<String, Object> entry : params.entrySet()) {// 构建表单字段内容
                    sb.append("--");
                    sb.append(BOUNDARY);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                    sb.append(entry.getValue());
                    sb.append("\r\n");
                }

                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(sb.toString().getBytes());

                if (!imageuri.equals("") && !imageuri.equals(null)) {
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + img + "\"; filename=\"" + imguri + "\"" + "\r\n" + "Content-Type: image/jpeg\r\n\r\n");
                    FileInputStream fis = new FileInputStream(imageuri);
                    byte[] buffer = new byte[1024]; // 8k
                    int count = 0;
                    while ((count = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, count);
                    }
                    dos.writeBytes(end);
                    fis.close();
                }
                dos.writeBytes("--" + BOUNDARY + "--\r\n");
                dos.flush();

                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                result = br.readLine();
            } catch (Exception e) {
                e.printStackTrace();
                result = "{\"ret\":\"898\"}";
            }
        }
    }

}
