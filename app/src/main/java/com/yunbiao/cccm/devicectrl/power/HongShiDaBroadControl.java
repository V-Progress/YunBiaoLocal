package com.yunbiao.cccm.devicectrl.power;


import com.yunbiao.cccm.devicectrl.power.PowerControllerTool;

class HongShiDaBroadControl {
    //    1,2,3,4,5,6,7,;16:43
    private String SET_POWER_ON_OFF = "android.56iq.intent.action.setpoweronoff";

    HongShiDaBroadControl() {
        PowerControllerTool.getPowerContrArray(SET_POWER_ON_OFF);
    }
}
