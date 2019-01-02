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
    public static final String DOMAIN = BuildConfig.DOMAIN;//基础IP地址

    public static final String BASE_URL = PREFIX + DOMAIN + SLASH;//URL地址
//    public static final String BASE_URL = "http://192.168.1.101:8080/cccm/";//URL地址
    public static final String SERVER_PORT = BuildConfig.PORT;//端口号

    public static final int NET_TIME_OUT = 5;//网络超时时间，单位：分钟

    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布
    private static final int QUEUE_BUSINESS = 2; //商家板排队叫号
    private static final int QUEUE = 3;//排队叫号
    private static final int WEI_PRINT = 4;//微信打印
    private static final int WEI_METTING = 5;//微信会议
    private static final int YUNBIAO_PAD = 6;//云标画板

    public static Map<Integer,String> typeMap = new HashMap<>();

    public interface URL {
        String NET = "www.yunbiaowulian.com";
        String PRO = "182.92.148.106";
//        String TST = "sp.mediaorange.cn";//210.51.34.85
        String TST = "210.51.34.85";
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
         * 菜单界面停留时长
         */
        int MENU_STAY_DURATION = 60;

        /***
         * 微信单个消息显示总时长
         */
        int WEICHAT_MSG_TIME = 30;
    }

    {
        typeMap.put(MESSAGE_DISTRIBUTE,"信息发布");
        typeMap.put(QUEUE_BUSINESS,"商家板排队叫号");
        typeMap.put(QUEUE,"排队叫号");
        typeMap.put(WEI_PRINT,"微信打印");
        typeMap.put(WEI_METTING,"微信会议");
        typeMap.put(YUNBIAO_PAD,"云标画板");
    }

    public interface VERSION_TYPE{
        int TYPE = MESSAGE_DISTRIBUTE;
        String TYPE_STR = typeMap.get(TYPE);
    }

}
