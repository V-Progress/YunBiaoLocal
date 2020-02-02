package com.yunbiao.cccm.net2.common;

import android.os.Environment;

/**
 * Created by Administrator on 2018/11/28.
 */

public class Const {
    public static final String API_KEY = "1234567890";

    private static final String PREFIX = "http://";//协议头
    private static final String SLASH = "/";//斜杠
    private static final String SUFFIX = "cccm/";//斜杠

    //正式地址
    public static final String SERVICE_HOSTS = "210.51.34.85";//云端IP地址
    public static final String SERVICE_PORT = ":80";
    public static final String XMPP_HOSTS = "210.51.34.85";//基础IP地址
    public static final String XMPP_PORT = "5222";//端口号
    public static final String BASE_URL = PREFIX + SERVICE_HOSTS + SERVICE_PORT + SLASH;//URL地址

    //吴建超地址
//    public static final String SERVICE_HOSTS = "wei.yunbiao.tv";
//    public static final String SERVICE_PORT = ":8855";
//    public static final String XMPP_HOSTS = "192.168.1.21";
//    public static final String XMPP_PORT = "5222";
//    public static final String BASE_URL = PREFIX + SERVICE_HOSTS + SERVICE_PORT + SLASH + SUFFIX;//URL地址

    public static final int NET_TIME_OUT = 8;//网络超时时间，单位：分钟
    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布

    public interface SYSTEM_CONFIG {
        int DATA_HANDLE_THREAD_NUMBER = 5;//数据处理的线程数量
        int REMOTE_THREAD_NUMBER = 2;//网络请求专用线程数量
        boolean IS_LOG_TO_FILE = true;//是否开启LOG
        int MENU_STAY_DURATION = 60;//菜单界面停留时长
        int WEICHAT_MSG_TIME = 30;//微信单个消息显示总时长
    }

    public interface VERSION_TYPE {
        int TYPE = MESSAGE_DISTRIBUTE;
    }

    public static final int TYPE_SD_CARD = 0;//基础路径为SD卡
    public static final int TYPE_USB_DISK = 1;//U盘
    public static final int TYPE_ENVIRONMENT_STORAGE = 2;//内部存储

    /***
     * 当前设备的存储类型
     */
    public static int STORAGE_TYPE = TYPE_SD_CARD;

    public static String getStorageType() {
        return STORAGE_TYPE == TYPE_SD_CARD ? "SD卡" : STORAGE_TYPE == TYPE_USB_DISK ? "U盘" : "内部存储";
    }
}
