package com.yunbiao.yunbiaolocal.cache;

import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.common.ResourceConst;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class CacheManager {

    private static final String CACHE_SAVE_PATH = ResourceConst.LOCAL_RES.PROPERTY_CACHE_PATH;
    private static FileCache fileCache;
    private static SPCache spCache;
    static {
        //初始化两种缓存
        fileCache = FileCache.get(new File(CACHE_SAVE_PATH));
        spCache = new SPCache(APP.getContext());
    }

    //SP缓存
    private static final String DEVICE_NUMBER = "deviceNum";//系统号
    private static final String DEVICE_SETTING_PWD = "settingPwd";//系统设置密码
    private static final String DEVICE_UNIQUE_CODE = "MAC";//设备唯一码
    private static final String DEVICE_IP = "machine_ip";//设备IP地址
    private static final String DEVICE_NAME = "deciveName";//设备名称
    private static final String DEVICE_RUNSTATUS = "runStatus";//运行状态
    private static final String DEVICE_BINDSTATUS = "bindStatus";//绑定状态
    private static final String EXPIRE_DATE = "expireDate";//有效期
    private static final String DEVICE_QR_CODE = "deviceQrCode";//QR码
    private static final String DEVICE_ISMIRROR = "isMirror ";//是否镜面
    private static final String DEVICE_TYPE_CONSTANT = "DTYPE";//设备类型
    private static final String DEVICE_BROAD_INFO = "broad_info";//主板信息
    public static final String CITY_NAME = "city_name";//定位城市s

    //文件缓存
    private static final String LAYOUT_DATA = "layoutJson";//布局数据
    private static final String SAVE_SOUND_MUSIC = "save_sound_music";
    private static final String ADSINFO_TEMP="adsinfo_temp";//广告信息缓存
    public static final String INSERT_TYPE = "insert_type";

    //暂未使用
    private static final String LAYOUT_POSITION = "layoutPosition";
    private static final String DEVICE_RUNKEY_CONSTANT = "RUNKEY";
    private static final String LAYOUT_CURRENT_CONSTANT = "CURRENT";
    private static final String SCREEN_ROTATE = "rotate";
    private static final String USED_SD = "usedSd";//用户存储用的内置sd卡或者外置sd卡
    private static final String FACEDETECT = "faceDetect";
    private static final String FACESHOW = "faceShow";

    public static class SP{

        /*中恒板子开关机用的是屏幕休眠，所以得设置关机的时候同时关闭声音*/
        public static void putCurrentVolume(String soundNum) {
            spCache.saveString(SAVE_SOUND_MUSIC, soundNum);
        }

        public static String getCurrentVolume() {
            return spCache.getString(SAVE_SOUND_MUSIC,"0");
        }


        public static String getCityName(){
            return spCache.getString(CITY_NAME,"");
        }

        public static void putCityName(String cityName){
            spCache.saveString(CITY_NAME,cityName);
        }

        public static void putBroadInfo(String info){
            spCache.saveString(DEVICE_BROAD_INFO,info);
        }
        public static String getBroadInfo(){
            return spCache.getString(DEVICE_BROAD_INFO,"");
        }

        public static void putDeviceType(String type){
            spCache.saveString(DEVICE_TYPE_CONSTANT,type);
        }

        public static String getDeviceType(){
            return spCache.getString(DEVICE_TYPE_CONSTANT,"-1");
        }

        public static void putDeviceQrCode(String code){
            spCache.saveString(DEVICE_QR_CODE,code);
        }

        public static String getDeviceQrCode(){
            return spCache.getString(DEVICE_QR_CODE,"");
        }

        public static void putExpireDate(String date){
            spCache.saveString(EXPIRE_DATE,date);
        }

        public static String getExpireDate(){
            return spCache.getString(EXPIRE_DATE,"");
        }

        public static void putBindStatus(String status){
            spCache.saveString(DEVICE_BINDSTATUS,status);
        }
        public static String getBindStatus(){
            return spCache.getString(DEVICE_BINDSTATUS,"");
        }

        public static void putRunStatus(String status){
            spCache.saveString(DEVICE_RUNSTATUS,status);
        }

        public static String getRunStatus(){
            return spCache.getString(DEVICE_RUNSTATUS,"");
        }

        public static void putDeviceName(String name){
            spCache.saveString(DEVICE_NAME,name);
        }

        public static String getDeviceName(){
            return spCache.getString(DEVICE_NAME,"");
        }

        public static void putDeviceNum(String deviceNum){
            spCache.saveString(DEVICE_NUMBER,deviceNum);
        }

        public static String getDeviceNum(){
            return spCache.getString(DEVICE_NUMBER,"");
        }

        public static void putAccessCode(String settingPwd){
            spCache.saveString(DEVICE_SETTING_PWD,settingPwd);
        }

        public static String getAccessCode(){
            return spCache.getString(DEVICE_SETTING_PWD,"");
        }

        public static void putDeviceUniCode(String code){
            spCache.saveString(DEVICE_UNIQUE_CODE,code);
        }

        public static String getDeviceUniCode(){
            return spCache.getString(DEVICE_UNIQUE_CODE,"");
        }

        public static void putDeviceIP(String ip){
            spCache.saveString(DEVICE_IP,ip);
        }

        public static String getDeviceIP(){
            return spCache.getString(DEVICE_IP,"");
        }

        public static void putIsMirror(String is){
            spCache.saveString(DEVICE_ISMIRROR,is);
        }
        public static String getIsMirror(){
            return spCache.getString(DEVICE_ISMIRROR,"");
        }
    }

    public static class FILE{
        public static void putLayoutData(String data){
            fileCache.put(LAYOUT_DATA,data);
        }

        public static String getLayoutData(){
            return fileCache.getAsString(LAYOUT_DATA);
        }

        public static void clearLayout() {
            fileCache.clear();
        }

        public static void removeLayout() {
            fileCache.remove(LAYOUT_DATA);
        }

        public static void putInsertType(Integer insertType){
            fileCache.put(INSERT_TYPE,String.valueOf(insertType));
        }

        public static Integer getInsertType(){
            String insertType = fileCache.getAsString(INSERT_TYPE);
            if(TextUtils.isEmpty(insertType)){
                return 0;
            }
            return Integer.valueOf(insertType);
        }

        public static void putInsertAds(String insertContent){
            fileCache.put(ADSINFO_TEMP,insertContent);
        }

        public static String getInsertAds(){
            return fileCache.getAsString(ADSINFO_TEMP);
        }
    }

}



