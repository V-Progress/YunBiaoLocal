package com.yunbiao.cccm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.commonsdk.UMConfigure;
import com.yunbiao.cccm.net2.activity.MainActivity;
import com.yunbiao.cccm.net2.activity.MenuActivity;
import com.yunbiao.cccm.net2.common.Const;
import com.yunbiao.cccm.net2.log.BlockDetectByPrinter;
import com.yunbiao.cccm.net2.db.DaoManager;
import com.yunbiao.cccm.net2.utils.CommonUtils;
import com.yunbiao.cccm.net2.listener.BDLocationListener;
import com.yunbiao.cccm.net2.log.Log2FileUtil;
import com.yunbiao.cccm.net2.utils.RestartAPPTool;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2018/11/27.
 */

public class APP extends Application {

    private static APP instance;
    private static MainActivity mActivity;
    private static MenuActivity mMenuActivity;
    private static OkHttpClient okHttpClient;
    private static AudioManager audioManager;
    private static SmdtManager smdt;
    private static List<Activity> actList;

    private static LocationClient locationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        BlockDetectByPrinter.start();

        //初始化本地包
        com.yunbiao.cccm.yunbiaolocal.APP.onCreate(this);

        instance = this;
        actList = new ArrayList<>();
        smdt = SmdtManager.create(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化

        Log2FileUtil.startLogcatManager(this);

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_BOX, "");
        UMConfigure.setLogEnabled(true);

        //初始化OKHTTPUTILS
        okHttpClient = new OkHttpClient()
                .newBuilder()
                .retryOnConnectionFailure(true)
                .connectTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .readTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .writeTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .build();
        OkHttpUtils.initClient(okHttpClient);

        //初始化定位
        initLocation();

        //初始化ImageLoader
        ImageLoaderConfiguration imageLoaderConfiguration = ImageLoaderConfiguration.createDefault(APP.getContext());
        ImageLoader.getInstance().init(imageLoaderConfiguration);

        //保存主板信息
        CommonUtils.saveBroadInfo();

        DaoManager.get().initDb();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(600000);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setIsNeedAddress(true);
        option.setIsNeedAltitude(true);
        option.setIsNeedLocationDescribe(true);
        locationClient = new LocationClient(this, option);

        BDLocationListener bdLocationListener = new BDLocationListener();
        locationClient.registerLocationListener(bdLocationListener);
        locationClient.start();
    }

    public static Application getContext() {
        return instance;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        mActivity = mainActivity;
    }

    public static void setMenuActivity(MenuActivity menuActivity) {
        mMenuActivity = menuActivity;
    }

    public static MainActivity getMainActivity() {
        return mActivity;
    }

    public static MenuActivity getMenuActivity() {
        return mMenuActivity;
    }

    public static AudioManager getAudioManager() {
        return audioManager;
    }

    public static SmdtManager getSmdt() {
        return smdt;
    }

    public static void addActivity(Activity activity) {
        actList.add(activity);
    }

    public static void removeActivity(Activity activity) {
        actList.remove(activity);
    }

    public static void resetApp() {
        //停止所有Activity
        for (Activity a : actList) {
            if (a != null) {
                a.finish();
            }
        }
        //清空缓存的activity
        actList.clear();
    }

    public static void exit() {
        //停止所有Activity
        for (Activity a : actList) {
            if (a != null) {
                a.finish();
            }
        }
        //清空缓存的activity
        actList.clear();
        Log2FileUtil.stopLogcatManager();
        //关闭整个应用
        System.exit(0);
    }

    public static void restart() {
        Intent intent = instance.getPackageManager().getLaunchIntentForPackage(instance.getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(instance, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) instance.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用

        exit();
    }
}
