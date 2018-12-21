package com.yunbiao.yunbiaolocal;

import android.app.Activity;
import android.app.Application;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.media.AudioManager;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.yunbiao.yunbiaolocal.act.AbsoluteActivity;
import com.yunbiao.yunbiaolocal.act.MainActivity;
import com.yunbiao.yunbiaolocal.act.MenuActivity;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;
import com.yunbiao.yunbiaolocal.utils.BDLocationListener;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.vov.vitamio.Vitamio;
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

    private static AbsoluteActivity absoluteActivity;
    private static LocationClient locationClient;

    public static void setAbsAct(AbsoluteActivity absAct){
        absoluteActivity = absAct;
    }
    public static AbsoluteActivity getAbsoluteActivity(){
        return absoluteActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        actList = new ArrayList<>();
        smdt = SmdtManager.create(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);// 安卓音频初始化

        //初始化定位
        initLocation();

        //初始化VITAMIO
        Vitamio.initialize(this);

        //初始化OKHTTPUTILS
        okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .readTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .writeTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .build();
        OkHttpUtils.initClient(okHttpClient);

        //初始化ImageLoader
        ImageLoaderConfiguration imageLoaderConfiguration = ImageLoaderConfiguration.createDefault(APP.getContext());
        ImageLoader.getInstance().init(imageLoaderConfiguration);

        //初始化设备号
        HeartBeatClient.createDeviceNo();

        //保存主板信息
        CommonUtils.saveBroadInfo();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(600000);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setIsNeedAddress(true);
        option.setIsNeedAltitude(true);
        option.setIsNeedLocationDescribe(true);
        locationClient = new LocationClient(this,option);

        BDLocationListener bdLocationListener = new BDLocationListener();
        locationClient.registerLocationListener(bdLocationListener);
        locationClient.start();
    }

    public static LocationClient getLocationClient(){
        return locationClient;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
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

    public static void removeActivity(Activity activity){
        actList.remove(activity);
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

        //关闭整个应用
        System.exit(0);
    }
}
