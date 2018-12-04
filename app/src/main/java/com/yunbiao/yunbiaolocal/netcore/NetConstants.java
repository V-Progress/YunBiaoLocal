package com.yunbiao.yunbiaolocal.netcore;

public class NetConstants {

    public final static Integer YBWEB = 1, TESTWEB = 2, LOCALTEST = 3, LOCAL_SERVER = 4;

    //当前的服务器类型
    public static int CURRENT_SERVER_TYPE = LOCALTEST;
    /**
     * 资源url web
     */
    public static String RESOURCE_URL = "http://www.yunbiaowulian.com/";
    /**
     * 微信打印
     */
    public static String WEI_PRINT = "http://www.yunbiaowulian.com/pn/print.do";

    public static String UP_LOAD_ERR_FILE = RESOURCE_URL + "queue/upLoadText.html";

    static {
        initConstants(YBWEB);
    }

    public static void initConstants(Integer type) {
        if (type == 1) {
            initYBWebConstants();
        } else if (type == 2) {
            initTestWebConstants();
        } else if (type == 3) {
            initLocalTestConstants();
        }
        UP_LOAD_ERR_FILE = RESOURCE_URL + "queue/upLoadText.html";
    }

    public static void initYBWebConstants() {
        RESOURCE_URL = "http://www.yunbiaowulian.com/";
        WEI_PRINT = "http://www.yunbiaowulian.com/pn/print.do";
//      RESOURCE_URL = "http://10.21.6.130:80/ruc/";
//      WEI_PRINT = "http://10.21.6.130:80/ruc/print.do";
    }

    public static void initTestWebConstants() {
//        RESOURCE_URL = "http://192.168.1.210/";
//        RESOURCE_URL = "http://120.27.105.40/";
//        RESOURCE_URL = "http://yc.yunbiaowulian.com/yb/";
        RESOURCE_URL = "http://wei.yunbiaowulian.com:8083/yb/";
//        RESOURCE_URL="http://lc.yunbiaowulian.com/";
    }

    public static void initLocalTestConstants() {
//        RESOURCE_URL = "http://192.168.1.101:8080/yb/";
        RESOURCE_URL = "http://192.168.12.50:8855/yb/";
//        RESOURCE_URL="http://192.168.2.195:8855/yb";
    }

    public static void initConstant(String ip, String port) {
        CURRENT_SERVER_TYPE = LOCAL_SERVER;
        RESOURCE_URL = "http://" + ip.trim() + ":" + port.trim() + "/yb/";
        WEI_PRINT = "http://" + ip.trim() + ":" + port.trim() + "/yb/print.do";
//        ResConstants.initWebConnect();
    }

    public static void initYbConstant() {
        RESOURCE_URL = "http://www.yunbiaowulian.com/";
        WEI_PRINT = "http://www.yunbiaowulian.com/pn/print.do";
//        ResConstants.initWebConnect();
    }

}


