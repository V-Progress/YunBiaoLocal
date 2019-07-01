package com.yunbiao.cccm.common;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.utils.CommonUtils;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HeartBeatClient {
    private static final String TAG = "HeartBeatClient";
    /**
     * 心跳频率 默认10s
     */
    private static String sbDeviceId = null;

    /**
     * 获取设备唯一编号
     *
     * @return
     */
    public static String getDeviceNo() {
        String sbDeviceId = CacheManager.SP.getDeviceUniCode();
        if(TextUtils.isEmpty(sbDeviceId)){
            sbDeviceId = getMacAddress();
            CacheManager.SP.putDeviceUniCode(sbDeviceId);
        }
        return sbDeviceId;
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress() {
        String macAddress = "";
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());

        assert wifiManager != null;
        boolean isActOpenWifi=false;
        if (!wifiManager.isWifiEnabled()) {//必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            isActOpenWifi=true;
        }
        if (null != info) {
            macAddress = info.getMacAddress();
            if (macAddress != null && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
                macAddress = CommonUtils.getSixOSMac();
            }
        }
        if (isActOpenWifi){
            wifiManager.setWifiEnabled(false);
        }
        Log.e("mac","wifi mac:"+macAddress);
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getLocalMacAddress();
            Log.e("mac","local mac:"+macAddress);
        }

        String mac = macAddress.toUpperCase();
        String macS = "";
        for (int i = mac.length() - 1; i >= 0; i--) {
            macS += mac.charAt(i);
        }
        UUID uuid2 = new UUID(macS.hashCode(), mac.hashCode());
        return uuid2.toString();
    }

    /**
     * 多种方法获取wifimac，防止出厂从未开启wifi导致无法获取wifimac
     * 从而引起因mac不一致导致出现资源未下载或设备号改变问题
     * @param restartNum//重复次数
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(int restartNum) {
        String macAddress = "";
        final WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
        assert wifiManager != null;
        boolean isActOpen=false;//是否是程序主动打开
        if (!wifiManager.isWifiEnabled()) {//必须先打开，才能获取到MAC地址
            wifiManager.setWifiEnabled(true);
            isActOpen=true;
        }
        if (null != info) {
            //循环几次获取mac，防止首次没打开过wifi时获取不到wifimac
            for (int index = 0; index <restartNum ; index++) {
                if(index!=0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                macAddress=CommonUtils.getWifiMacAddress();
                if (TextUtils.isEmpty(macAddress)){
                    macAddress = info.getMacAddress();
                }
                if (macAddress != null && macAddress.equals("02:00:00:00:00:00")) {//6.0及以上系统获取的mac错误
                    macAddress = CommonUtils.getSixOSMac();
                    LogUtil.D(TAG,"6.0wifi mac:"+macAddress);
                }
                if (!TextUtils.isEmpty(macAddress)){
                    break;
                }
            }
        }
        if (isActOpen){
            //延时关闭，防止有时关闭不了wifi
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    wifiManager.setWifiEnabled(false);
                }
            },300);
        }
        LogUtil.D(TAG,"wifi mac:"+macAddress);
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = CommonUtils.getLocalMacAddress();
            LogUtil.D(TAG,"local mac:"+macAddress);
        }

        String mac = macAddress.toUpperCase();
        String macS = "";
        for (int i = mac.length() - 1; i >= 0; i--) {
            macS += mac.charAt(i);
        }
        UUID uuid2 = new UUID(macS.hashCode(), mac.hashCode());
        LogUtil.D(TAG,"uuid2:"+uuid2.toString());
        return uuid2.toString();
    }
}
