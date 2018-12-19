package com.yunbiao.yunbiaolocal.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.baidu.location.Address;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.cache.CacheManager;

/**
 * Created by Administrator on 2018/12/19.
 */

public class BDLocationListener extends BDAbstractLocationListener {
    private boolean isSended = false;
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Address addr = bdLocation.getAddress();

        double latitude = bdLocation.getLatitude();
        double longitude = bdLocation.getLongitude();
        String city = bdLocation.getCity();
        String address = addr.address;
        double altitude = bdLocation.getAltitude();
        LogUtil.D("纬度："+latitude);
        LogUtil.D("经度："+longitude);
        LogUtil.D("城市："+city);
        LogUtil.D("地址："+address);
        LogUtil.D("海拔："+altitude);

        CacheManager.SP.putLatitude(String.valueOf(latitude));
        CacheManager.SP.putLongitude(String.valueOf(longitude));
        CacheManager.SP.putCityName(city);
        CacheManager.SP.putAddress(address);
        CacheManager.SP.putAltitude(String.valueOf(altitude));

    }
}
