package com.yunbiao.yunbiaolocal.utils;

import android.text.TextUtils;

import com.baidu.location.Address;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.yunbiao.yunbiaolocal.cache.CacheManager;

/**
 * Created by Administrator on 2018/12/19.
 */

public class BDLocationListener extends BDAbstractLocationListener {
    private boolean isSended = false;
    private final static double defaultVal = 4.9E-324;
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        String locLat = CacheManager.SP.getLatitude();
        String locLon = CacheManager.SP.getLongitude();
        String locCN = CacheManager.SP.getCityName();
        String locAdd = CacheManager.SP.getAddress();
        String locAlt = CacheManager.SP.getAltitude();

        //只有在缓存为空或者数据不为默认值的情况下缓存
        //纬度
        double latitude = bdLocation.getLatitude();
        if(TextUtils.isEmpty(locLat) || (latitude!=defaultVal)){
            CacheManager.SP.putLatitude(String.valueOf(latitude));
        }

        //经度
        double longitude = bdLocation.getLongitude();
        if(TextUtils.isEmpty(locLon) || (longitude!=defaultVal)){
            CacheManager.SP.putLongitude(String.valueOf(longitude));
        }

        //城市名
        String city = bdLocation.getCity();
        if(TextUtils.isEmpty(locCN) || (!TextUtils.isEmpty(city))){
            CacheManager.SP.putCityName(city);
        }

        //地址
        Address addr = bdLocation.getAddress();
        String address = addr.address;
        if(TextUtils.isEmpty(locAdd) || (!TextUtils.isEmpty(address))){
            CacheManager.SP.putAddress(address);
        }

        //海拔
        double altitude = bdLocation.getAltitude();
        if(TextUtils.isEmpty(locAlt) || (altitude != defaultVal)){
            CacheManager.SP.putAltitude(String.valueOf(altitude));
        }

        LogUtil.D("纬度："+latitude);
        LogUtil.D("经度："+longitude);
        LogUtil.D("城市："+city);
        LogUtil.D("地址："+address);
        LogUtil.D("海拔："+altitude);
    }
}
