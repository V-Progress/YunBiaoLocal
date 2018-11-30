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


    public interface CONTROL_EVENT{
        int OPEN_CONSOLE = 1;
        int UPDATE_CONSOLE = 2;
        int INIT_PROGRESS = 3;
        int UPDATE_PROGRESS = 4;
        int INIT_PLAYER = 0;
        int CLOSE_CONSOLE = 0;
        int VIDEO_PLAY = 11;
        int VIDEO_STOP = 10;
    }

}
