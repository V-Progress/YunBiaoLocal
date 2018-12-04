package com.yunbiao.yunbiaolocal;

/**
 * Created by Administrator on 2018/11/28.
 */

public class Const {
    public static final String API_KEY = "1234567890";

    private static final String PREFIX = "http://";//协议头
    private static final String SLASH = "/";//斜杠
    public static final String DOMAIN = BuildConfig.DOMAIN;//基础IP地址

    public static final String BASE_URL = PREFIX + DOMAIN + SLASH;//URL地址
    public static final String SERVER_PORT = BuildConfig.PORT_TST;//端口号

    public static final int NET_TIME_OUT = 5;//网络超时时间，单位：分钟
    /**
     * 微信打印
     */
    public static String WEI_PRINT = "http://www.yunbiaowulian.com/pn/print.do";

    public static String UP_LOAD_ERR_FILE = BASE_URL + "queue/upLoadText.html";


    public interface URL {
        String NET = "www.yunbiaowulian.com";
        String PRO = "182.92.148.106";
        String DEV = "192.168.1.101";
    }

    public interface PORT {
        String PRO = "5222";
        String DEV = "5222";
    }

    public interface SYSTEM_CONFIG {
        int DATA_RESOLVE_THREAD_NUMBER = 4;
        String LOG_TAG = "YUNBIAO";
        boolean IS_LOG = true;
    }

}
