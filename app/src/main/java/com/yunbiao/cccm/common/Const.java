package com.yunbiao.cccm.common;

import com.yunbiao.cccm.BuildConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/28.
 */

public class Const {
    public static final String API_KEY = "1234567890";

    private static final String PREFIX = "http://";//协议头
    private static final String SLASH = "/";//斜杠
    private static final String SUFFIX = "cccm/";//斜杠

    public static final String HOSTS = BuildConfig.HOSTS;//基础IP地址
//    public static final String BASE_URL = PREFIX + HOSTS + ":8080" + SLASH + SUFFIX;//URL地址
    public static final String BASE_URL = PREFIX + HOSTS + SLASH;//URL地址

    public static final String SERVER_PORT = BuildConfig.PORT;//端口号

    public static final int NET_TIME_OUT = 5;//网络超时时间，单位：分钟
    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布
    private static final int QUEUE_BUSINESS = 2; //商家板排队叫号
    private static final int QUEUE = 3;//排队叫号
    private static final int WEI_PRINT = 4;//微信打印
    private static final int WEI_METTING = 5;//微信会议
    private static final int YUNBIAO_PAD = 6;//云标画板

    public interface URL {
        String PRO = "210.51.34.85";
//        String PRO = "192.168.1.101";

        String DEV = "192.168.1.101";
    }

    public interface PORT {
        String PRO = "5222";
        String DEV = "5222";
    }

    public interface SYSTEM_CONFIG {
        /***
         * 数据处理的线程数量
         */
        int DATA_HANDLE_THREAD_NUMBER = 5;

        /***
         * 网络请求专用线程数量
         */
        int REMOTE_THREAD_NUMBER = 2;

        /***
         * 日志通用TAG
         */
        String LOG_TAG = "YUNBIAO_";

        /***
         * 是否输出日志
         */
        boolean IS_LOG = true;

        /***
         *
         */
        boolean IS_LOG_TO_FILE = BuildConfig.IS_LOG_TO_FILE;

        /***
         * 菜单界面停留时长
         */
        int MENU_STAY_DURATION = 60;

        /***
         * 微信单个消息显示总时长
         */
        int WEICHAT_MSG_TIME = 30;
    }

    public interface VERSION_TYPE{
        int TYPE = MESSAGE_DISTRIBUTE;
    }

}
