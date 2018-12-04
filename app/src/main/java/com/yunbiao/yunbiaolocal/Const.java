package com.yunbiao.yunbiaolocal;

/**
 * Created by Administrator on 2018/11/28.
 */

public class Const {
    public static final String API_KEY = "1234567890";

    private static final String PREFIX = "http://";
    public static final int SERVER_TYPE = BuildConfig.SERVER_TYPE;
    public static final String URL = BuildConfig.URL_TST;
    public static final String BASE_URL = PREFIX + URL;
    public static final String PORT = BuildConfig.PORT_TST;

    public static final int NET_TIME_OUT = 5;


    private interface URL{
        String SERVER_URL = "";
        String SERVER_PORT = "";
    }

    public interface SYSTEM_CONFIG{
        int DATA_RESOLVE_THREAD_NUMBER = 4;
        String LOG_TAG = "YUNBIAO";
    }

}
