package com.yunbiao.cccm.net2.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.common.Const;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.net2.common.ResourceConst;
import com.yunbiao.cccm.net2.common.HeartBeatClient;
import com.yunbiao.cccm.net2.log.LogUtil;
import com.yunbiao.cccm.net2.view.TipToast;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/12/6.
 */

public class SystemInfoUtil {
    private static String TAG = "SystemInfoUtil";
    //返回的安装包url
    private static String apkUrl = "http://211.157.160.102/imgserver/hsd.apk";

    public static String getSystemSDK(){
        int sdkInt = Build.VERSION.SDK_INT;
        return String.valueOf(sdkInt);
    }

    public static String getSystemVersion(){
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前版本号
     */
    public static String getVersionName() {
        String version = "";
        try {
            PackageManager packageManager = APP.getContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(APP.getContext().getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /***
     * 上传APP版本
     */
    public static void uploadAppVersion() {
        String versionName = getVersionName();
        TipToast.showLongToast(APP.getMainActivity(),"版本:"+versionName);
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("version", versionName);
        paramMap.put("type", Const.VERSION_TYPE.TYPE + "");
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.UPLOAD_APP_VERSION_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {

            }
        });
    }

    /**
     * 上传磁盘数据
     */
    public static void uploadDiskInfo() {
        String diskInfo = getSDDiskCon();
        String ss = "磁盘:" + diskInfo;
        TipToast.showLongToast(APP.getMainActivity(),ss);
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
        paramMap.put("diskInfo", diskInfo);
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.UPLOAD_DISK_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {

            }
        });
    }

    /***
     * 获取磁盘空间
     * @return
     */
    public static String getSDDiskCon() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return "已用:" + (useStr) + "/可用:" + allStr;
        } else {
            return "";
        }
    }

    /***
     * 获取外部内存空间
     * @param context
     * @return
     */
    public static String getEnSDDiskCon(Context context) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            double blockSize = sf.getBlockSize();
            double blockCount = sf.getBlockCount();
            double availCount = sf.getAvailableBlocks();

            Double level = (availCount * blockSize / 1024);
            Double all = (blockSize * blockCount / 1024);

            Double use = all - level;

            Double useDou = use / 1024 / 1024;
//            Double levelDou = level / 1024 / 1024;
            Double allDou = all / 1024 / 1024;

            BigDecimal useB = new BigDecimal(useDou);
            double useF = useB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

//            BigDecimal levelB = new BigDecimal(levelDou);
//            double levelF = levelB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            BigDecimal allB = new BigDecimal(allDou);
            double allF = allB.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            String useStr = useF + "G";
            if (useF < 1) {
                useF = useF * 1024;
                useStr = useF + "M";
            }
//            String levelStr = levelF + "G";
//            if (levelF < 1) {
//                levelF = levelF * 1024;
//                levelStr = levelF + "M";
//            }
            String allStr = allF + "G";
            if (allF < 1) {
                allF = allF * 1024;
                allStr = allF + "M";
            }
            return context.getResources().getString(R.string.used) + (useStr) + context.getResources().getString(R.string
                    .no_used) + allStr;
        } else {
            return "";
        }
    }

    /***
     * 删除其他文件
     */
    public static void deleteOtherFile() {

        // 获取本地磁盘已经存在的资源
        String imagePath = ResourceConst.LOCAL_RES.IMAGE_CACHE_PATH;
        File resource = new File(imagePath);
        Map<String, File> fileMap = new HashMap<>();
        if (resource.exists()) {
            File[] files = resource.listFiles();
            for (File hasFile : files) {
                fileMap.put(hasFile.getName(), hasFile);
            }
        } else {
            resource.mkdirs();
        }

        //删除剩下的数据
        Set<String> keySet = fileMap.keySet();
        for (String fileTemp : keySet) {
            File hasFile = (File) fileMap.get(fileTemp);
            hasFile.delete();
        }
    }


    //外部接口让主Activity调用
    public static void checkUpdateInfo() {
        //判断是否需要更新
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("clientVersion", getVersionName());
        paramMap.put("type", Const.VERSION_TYPE.TYPE + "");
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.VERSION_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtil.E("检查更新ERROR: " + e.getMessage());
                if(onDownLoadListener != null){
                    onDownLoadListener.onError(e);
                }
            }

            @Override
            public void onResponse(String response, int id) {
                if (response.startsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }
                LogUtil.D("检查更新RESPONSE: " + response);
                judgeIsUpdate(response,onDownLoadListener);
            }
        });
    }

    /***
     * 检查更新
     * @param isUpdate
     * @param onDownLoadListener
     */
    private static void judgeIsUpdate(String isUpdate, NetUtil.OnDownLoadListener onDownLoadListener) {
        //返回
        switch (isUpdate) {
            case "1": //不需要更新
                onDownLoadListener.onError(new Exception("1"));
                break;
            case "faile":  //网络不通，或者解析出错
                onDownLoadListener.onError(new Exception("faile"));
                break;
            default:
                apkUrl = isUpdate;
                //下载apk
                downloadUpdate(onDownLoadListener);
                break;
        }
    }

    /***
     * 下载更新
     */
    public static void downloadUpdate(NetUtil.OnDownLoadListener onDownLoadListener){
        NetUtil.getInstance().downloadFile(apkUrl, onDownLoadListener);
    }
    private static NetUtil.OnDownLoadListener onDownLoadListener = new NetUtil.OnDownLoadListener() {
        @Override
        public void onStart(String fileName) {
            LogUtil.D(TAG,"开始下载更新...");
        }

        @Override
        public void onProgress(int progress) {
            LogUtil.D(TAG,"下载更新进度..."+progress);
        }

        @Override
        public void onComplete(File response) {
            installApk(APP.getContext(),response);
        }

        @Override
        public void onFinish() {
            LogUtil.D(TAG,"下载更新结束...");
        }

        @Override
        public void onError(Exception e) {
            LogUtil.D(TAG,"下载更新错误..."+e.getMessage());
        }
    };

    /**
     * 安装apk
     *
     * @param
     */
    public static void installApk(Context context,File file) {
        File apkfile = file;
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 判断前后摄像头
     */
    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    private static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_BACK = 1;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static int getCamera() {
        if (hasFrontFacingCamera()) {
            return 1;
        } else if (hasBackFacingCamera()) {
            return 0;
        }
        return -1;
    }

    private static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

}
