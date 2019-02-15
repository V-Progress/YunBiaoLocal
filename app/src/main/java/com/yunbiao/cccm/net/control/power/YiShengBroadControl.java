package com.yunbiao.cccm.net.control.power;



class YiShengBroadControl {
    private String SET_POWER_ON_OFF = "android.intent.action.setpoweronoff";

    /**
     * int[] timeonArray = {2014,10,1,8,30}; //下次开机具体日期时间，即在2014/10/1 8:30会开机
     * int[] timeoffArray = {2014,9,1,8,25}; //下次关机具体日期时间，即在2014/9/1 8:25会关机
     */
    YiShengBroadControl() {
        PowerControllerTool.getPowerContrArray(SET_POWER_ON_OFF);
    }
}
