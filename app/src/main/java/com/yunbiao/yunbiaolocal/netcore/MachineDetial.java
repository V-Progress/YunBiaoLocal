package com.yunbiao.yunbiaolocal.netcore;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.ThreadUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by LiuShao on 2016/3/4.
 */

public class MachineDetial {
    private static final String TAG = "MachineDetial";
    private String upMechineDetialUrl = Const.BASE_URL + "device/service/updateDeviceHardwareInfo.html";

    private static MachineDetial machineDetial;

    public static MachineDetial getInstance() {
        if (machineDetial == null) {
            machineDetial = new MachineDetial();
        }
        return machineDetial;
    }

    private MachineDetial() {
//        DeviceLocation.getDeviceLocation().getLocation();
    }

//    private LocationBean LocationBean;
    private boolean isLocationInited = false;
//
//    public void setLocation(LocationBean LocationBean) {
//        this.LocationBean = LocationBean;
//        isLocationInited = true;
//        upLoadHardWareMessage();
//        DeviceLocation.getDeviceLocation().stopLocation();
//    }
//
//    public LocationBean getLocation() {
//        isLocationInited = true;
//        upLoadHardWareMessage();
//        DeviceLocation.getDeviceLocation().stopLocation();
//        return LocationBean;
//    }

    /**
     * 上传设备信息
     */
    public void upLoadHardWareMessage() {
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());
                map.put("screenWidth", String.valueOf(CommonUtils.getScreenWidth(APP.getContext())));
                map.put("screenHeight", String.valueOf(CommonUtils.getScreenHeight(APP.getContext())));
                map.put("diskSpace", CommonUtils.getMemoryTotalSize());
                map.put("useSpace", CommonUtils.getMemoryUsedSize());
                map.put("softwareVersion", CommonUtils.getAppVersion(APP.getContext()) + "_" + Const.VERSION_TYPE.TYPE);
//                map.put("screenRotate", String.valueOf(SystemProperties.get("persist.sys.hwrotation")));
                map.put("deviceCpu", CommonUtils.getCpuName() + " " + CommonUtils.getNumCores() + "核" + CommonUtils
                        .getMaxCpuFreq() + "khz");
                map.put("deviceIp", CommonUtils.getIpAddress());//当前设备IP地址
                map.put("mac", CommonUtils.getLocalMacAddress());//设备的本机MAC地址
                if (isLocationInited) {
//                    map.put("latitude", LocationBean.getAltitude());
//                    map.put("longitude", LocationBean.getLongitude());
//                    map.put("address", LocationBean.getAdress());
//                    map.put("addressHeight", LocationBean.getAdressHeight());
//                    map.put("cityName", LocationBean.getCity());
//                    //定位后在SharedPreferences存入定位得到的城市名字，后边获取。重启后定位覆盖
//                    String city = LocationBean.getCity();
//                    SpUtils.saveString(APP.getContext(), SpUtils.CITY_NAME, city);
                }
                NetUtil.getInstance().post(upMechineDetialUrl, map, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        LogUtil.E(e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        LogUtil.E(response);
                    }
                });
            }
        });
    }
}
