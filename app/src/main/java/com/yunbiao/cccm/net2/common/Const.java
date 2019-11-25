package com.yunbiao.cccm.net2.common;

/**
 * Created by Administrator on 2018/11/28.
 */

public class Const {
    public static final String API_KEY = "1234567890";

    private static final String PREFIX = "http://";//协议头
    private static final String SLASH = "/";//斜杠
    private static final String SUFFIX = "cccm/";//斜杠

    public static final String SERVICE_HOSTS = "210.51.34.85";//云端IP地址
    public static final String SERVICE_PORT = ":80";
    public static final String XMPP_HOSTS = "210.51.34.85";//基础IP地址
    public static final String XMPP_PORT = "5222";//端口号

    public static final String BASE_URL = PREFIX + SERVICE_HOSTS + SERVICE_PORT + SLASH;//URL地址

    public static final int NET_TIME_OUT = 5;//网络超时时间，单位：分钟
    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布

    public interface SYSTEM_CONFIG {
        int DATA_HANDLE_THREAD_NUMBER = 5;//数据处理的线程数量
        int REMOTE_THREAD_NUMBER = 2;//网络请求专用线程数量
        boolean IS_LOG_TO_FILE = true;//是否开启LOG
        int MENU_STAY_DURATION = 60;//菜单界面停留时长
        int WEICHAT_MSG_TIME = 30;//微信单个消息显示总时长
    }

    public interface VERSION_TYPE{
        int TYPE = MESSAGE_DISTRIBUTE;
    }

}
