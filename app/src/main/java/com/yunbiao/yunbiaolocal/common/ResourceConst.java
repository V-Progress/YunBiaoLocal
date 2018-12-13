package com.yunbiao.yunbiaolocal.common;

import android.os.Environment;

import com.yunbiao.yunbiaolocal.common.Const;

public class ResourceConst {

    public interface LOCAL_RES {
        String EXTERNAL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();//外存根目录
        String APP_CACHE_BASE_PATH = "/mnt/sdcard/hsd";//外存根目录
        String APP_MAIN_DIR = EXTERNAL_DIR + "/yunbiao";//APP资源主目录

        //截屏存储
        String SCREEN_CACHE_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/screen";
        //参数资源存储
        String PROPERTY_CACHE_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/property";
        //资源存储
        String IMAGE_CACHE_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/resource";
        // 资源存储目录
        String WEI_CACHE_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/wei";
        //推送消息存储
        String PUSH_CACHE_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/push";
        //播放日志存储
        String PLAYLOG_PATH = EXTERNAL_DIR + APP_CACHE_BASE_PATH + "/playLog";

    }

    public interface REMOTE_RES {
        /***
         * 设备信息上传
         */
        String UPLOAD_DEVICE_INFO = Const.BASE_URL + "device/service/updateDeviceHardwareInfo.html";
        /**
         * 微信打印
         */
        String WEI_PRINT_URL = "http://www.yunbiaowulian.com/pn/print.do";

        /***
         * 错误文件上传
         */
        String UP_LOAD_ERR_FILE = Const.BASE_URL + "queue/upLoadText.html";

        /***
         * 设备信息上传
         */
        String DEVICE_ONLINE_STATUS = Const.BASE_URL + "device/status/getrunstatus.html";

        /***
         * 设备号
         */
        String SER_NUMBER = Const.BASE_URL + "device/status/getHasNumber.html";

        /**
         * 版本检测
         **/
        String VERSION_URL = Const.BASE_URL + "device/service/getversion.html";

        /**
         * 开关机时间获取
         **/
        String POWER_OFF_URL = Const.BASE_URL + "device/service/poweroff.html";

        /***
         * APP版本信息上传地址
         */
        String UPLOAD_APP_VERSION_URL = Const.BASE_URL + "device/service/uploadAppVersionNew.html";

        /***
         * 磁盘信息上传地址
         */
        String UPLOAD_DISK_URL = Const.BASE_URL + "device/service/uploadDisk.html";

        /**
         * 资源获取
         **/
        String RESOURCE_URL = Const.BASE_URL + "device/service/getresource.html";

        /**
         * 广告资源获取
         **/
        String ADS_RESOURCE_URL = Const.BASE_URL + "api/share/getDeviceAdvert.html";

        /**
         * 判断服务器和本地布局是否匹配
         **/
        String LAYOUT_CHANGE_STATUS = Const.BASE_URL + "device/service/layoutchangestatus.html";

        /**
         * 前端布局
         */
        String LAYOUT_MENU_URL = Const.BASE_URL + "device/service/getLayoutMenu.html";

        /**
         * 天气获取
         **/
        public static String WEATHER_URL = Const.BASE_URL + "weather/city.html";

        /**
         * 限号获取
         **/
        public static String CARRUN_URL = Const.BASE_URL + "weather/carrun.html";

        /**
         * 外币汇率获取
         **/
        public static String RMBRATE_URL = Const.BASE_URL + "weather/exrate.html";

        /**
         * 截图上传
         **/
        public static String SCREEN_UPLOAD_URL = Const.BASE_URL + "device/service/uploadScreenImg.html";

        /**
         * 上传进度
         **/
        public static String RES_UPLOAD_URL = Const.BASE_URL + "device/service/rsupdate.html";


        public static String SCAN_TO_CALL = Const.BASE_URL + "/mobilebusself/mobilebusselfpost/selectbyordersernum.html";

        public static String SETTIME = Const.BASE_URL + "common/service/getSystemTime.html";

        /**
         * 绑定设备
         */
        public static String DEC_NUM = Const.BASE_URL + "device/status/binduser.html";

        /**
         * 获取续费二维码
         */
        public static String QRCODE = Const.BASE_URL + "device/renewal/getopenrenewalqrcode.html";

        /**
         * 上传人脸识别
         */
        public static String UPLOADFACE = Const.BASE_URL + "visitors/saveVisitors.html";

        /**
         * 上传下载进度
         */
        public static String NETSPEEFURL = Const.BASE_URL + "device/status/uploadnetspeed.html";

        /**
         * 广告播放日志上传
         */
        public static String RECEIVELOGFILE_URL = Const.BASE_URL + "api/loginterface/insertLogFace.html";

        /**
         * 音量调节值获取
         * http://tyiyun.com/device/service/getVolume.html?deviceId=ffffffff-
         * be09-eca9-756a-0d8000000000
         */
        String VOLUME_URL = Const.BASE_URL + "device/service/getVolume.html";

    }


}
