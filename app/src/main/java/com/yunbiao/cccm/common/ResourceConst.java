package com.yunbiao.cccm.common;

import android.os.Build;
import android.os.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceConst {

    //播放列表和预览列表
    private static List<String> playList = new ArrayList<>();
    private static Map<String, String> previewMap = new HashMap<>();

    public static List<String> getPlayList() {
        return playList;
    }

    public static Map<String, String> getPreviewMap() {
        return previewMap;
    }

    /***
     * 清空播放列表和预览列表
     */
    public static void clearPalyList(){
        playList.clear();
        previewMap.clear();
    }

    /***
     * 添加播放列表条目
     * @param item
     */
    public static void addPlayItem(String item){
        playList.add(item);
    }

    /***
     * 添加预览条目
     * @param key
     * @param value
     */
    public static void addPreviewItem(String key,String value){
        previewMap.put(key,value);
    }

    public static class LOCAL_RES {
        private static String EXTERNAL_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();//外存根目录

        static {
            if(Build.VERSION.SDK_INT >= 21){
                // TODO: 2019/1/21 API21以上暂时未解决SD卡存储问题
            }else{
                EXTERNAL_ROOT_DIR = "/mnt/extsd";
            }
        }

        public static String APP_MAIN_DIR = EXTERNAL_ROOT_DIR + "/yunbiao";//APP资源主目录
        //参数资源存储
        public static String PROPERTY_CACHE_PATH = APP_MAIN_DIR + "/property";
        //资源存储
        public static String RES_SAVE_PATH = APP_MAIN_DIR + "/resource";
        //截屏存储
        public static String SCREEN_CACHE_PATH = APP_MAIN_DIR + "/screen";
        //图片资源存储
        public static String IMAGE_CACHE_PATH = APP_MAIN_DIR + "/img";

        //本地资源
        public static String LOCAL_RES_SAVE_PATH = APP_MAIN_DIR + "/local";
        //微信资源存储目录
        public static String WEI_CACHE_PATH = APP_MAIN_DIR + "/wei";
        //推送消息存储
        public static String PUSH_CACHE_PATH = APP_MAIN_DIR + "/push";
        //播放日志存储
        public static String PLAYLOG_PATH = APP_MAIN_DIR + "/playLog";

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
