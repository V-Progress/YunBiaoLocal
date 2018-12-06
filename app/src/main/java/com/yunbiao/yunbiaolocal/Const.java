package com.yunbiao.yunbiaolocal;

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
    public static final String SERVER_PORT = BuildConfig.PORT_TST;//端口号

    public static final int NET_TIME_OUT = 5;//网络超时时间，单位：分钟

    private static final int MESSAGE_DISTRIBUTE = 1; //信息发布
    private static final int QUEUE_BUSINESS = 2; //商家板排队叫号
    private static final int QUEUE = 3;//排队叫号
    private static final int WEI_PRINT = 4;//微信打印
    private static final int WEI_METTING = 5;//微信会议
    private static final int YUNBIAO_PAD = 6;//云标画板

    public static Map<Integer,String> typeMap = new HashMap<>();

    /**
     * 微信打印
     */
    public static String WEI_PRINT_URL = "http://www.yunbiaowulian.com/pn/print.do";

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
