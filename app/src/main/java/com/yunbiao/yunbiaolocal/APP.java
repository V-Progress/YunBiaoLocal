package com.yunbiao.yunbiaolocal;

import android.app.Application;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.media.AudioManager;

import com.yunbiao.yunbiaolocal.act.MainActivity;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import io.vov.vitamio.Vitamio;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2018/11/27.
 */

public class APP extends Application {

    private static APP instance;
    private static MainActivity mActivity;
    private static OkHttpClient okHttpClient;
    private static AudioManager audioManager;
    private static SmdtManager smdt;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Vitamio.initialize(this);
        // 安卓音频初始化
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        smdt = SmdtManager.create(this);
        okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .readTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .writeTimeout(Const.NET_TIME_OUT, TimeUnit.MINUTES)
                .build();

        OkHttpUtils.initClient(okHttpClient);
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

    public static MainActivity getMainActivity() {
        return mActivity;
    }

    public static AudioManager getAudioManager() {
        return audioManager;
    }

    public static SmdtManager getSmdt() {
        return smdt;
    }
}
