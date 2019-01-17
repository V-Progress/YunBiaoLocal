package com.yunbiao.cccm.common;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

public class ResourceConst {

    public interface LOCAL_RES {
        String EXTERNAL_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();//外存根目录
        String APP_MAIN_DIR = EXTERNAL_ROOT_DIR + "/yunbiao";//APP资源主目录
        //参数资源存储
        String PROPERTY_CACHE_PATH = APP_MAIN_DIR + "/property";
        //资源存储
        String RES_SAVE_PATH = APP_MAIN_DIR + "/resource";
        //本地资源
        String LOCAL_RES_SAVE_PATH = APP_MAIN_DIR + "/local";

        //截屏存储
        String SCREEN_CACHE_PATH = APP_MAIN_DIR + "/screen";
        //图片资源存储
        String IMAGE_CACHE_PATH = APP_MAIN_DIR + "/img";
        //微信资源存储目录
        String WEI_CACHE_PATH = APP_MAIN_DIR + "/wei";
        //推送消息存储
        String PUSH_CACHE_PATH = APP_MAIN_DIR + "/push";
        //播放日志存储
        String PLAYLOG_PATH = APP_MAIN_DIR + "/playLog";

    }

    public interface REMOTE_RES {
        /***
         * 设备信息上传
         */
        String UPLOAD_DEVICE_INFO = Const.BASE_URL + "api/device/updateDeviceHardwareInfo.html";

        /***
         * APP版本信息上传地址
         */
        String UPLOAD_APP_VERSION_URL = Const.BASE_URL + "api/device/uploadAppVersion.html";

        /***
         * 磁盘信息上传地址
         */
        String UPLOAD_DISK_URL = Const.BASE_URL + "api/device/uploadDisk.html";

        /**
         * 截图上传
         **/
        String SCREEN_UPLOAD_URL = Const.BASE_URL + "api/device/uploadScreenImg.html";

        /***
         * 获取主播放资源
         */
        String GET_RESOURCE = Const.BASE_URL + "api/layout/getlayoutconfig.html";

        /***
         * 资源下载进度上传
         */
        String RES_PROGRESS_UPLOAD = Const.BASE_URL + "api/layout/rsupdate.html";

        /***
         * 设备号
         */
        String SER_NUMBER = Const.BASE_URL + "api/device/getHasNumber.html";

        /**
         * 开关机时间获取
         **/
        String POWER_OFF_URL = Const.BASE_URL + "api/device/poweroff.html";

        /***
         * 插播资源获取
         */
        String INSERT_CONTENT = Const.BASE_URL + "api/deviceinsert/getdeviceinsertlist.html";

        /**
         * 版本检测
         **/
        String VERSION_URL = Const.BASE_URL + "api/device/getversion.html";

        /***
         * 设备信息上传
         */
        String DEVICE_ONLINE_STATUS = Const.BASE_URL + "api/device/status/getrunstatus.html";
    }
}
