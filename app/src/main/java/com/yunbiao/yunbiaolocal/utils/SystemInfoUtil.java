package com.yunbiao.yunbiaolocal.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.yunbiao.yunbiaolocal.view.TipToast;
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

    //返回的安装包url
    private static String apkUrl = "http://211.157.160.102/imgserver/hsd.apk";

    /**
     * 获取当前版本号
     */
    private static String getVersionName() {
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
        final NetUtil.OnDownLoadListener downloadUpdateListener = APP.getMainActivity().downloadUpdateListener;
        //判断是否需要更新
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("clientVersion", getVersionName());
        paramMap.put("type", Const.VERSION_TYPE.TYPE + "");
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.VERSION_URL, paramMap, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                downloadUpdateListener.onError(e);
            }

            @Override
            public void onResponse(String response, int id) {
                if (response.startsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }
                judgeIsUpdate(response,downloadUpdateListener);
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
     * @param onDownLoadListener
     */
    public static void downloadUpdate(NetUtil.OnDownLoadListener onDownLoadListener){
        NetUtil.getInstance().downloadFile(apkUrl,onDownLoadListener);
    }

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
}
