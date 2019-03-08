package com.yunbiao.cccm.cache;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.net.resource.model.InsertTextModel;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;

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

    //SP缓存----------------------------------------------------------
    //登录信息
    private static final String DEVICE_NAME = "deciveName";//设备名称
    private static final String DEVICE_SETTING_PWD = "deviceSettingPwd";//系统设置密码
    private static final String DEVICE_ACCESS_CODE = "deviceAccessCode";//设备接入码
    private static final String WECHAT_TICKET = "wechatTicket";//微信二维码
    private static final String DEVICE_NUMBER = "deviceNum";//设备编号
    private static final String DEVICE_STATUS = "deviceStatus";//设备编号
    //设备信息
    private static final String DEVICE_UNIQUE_CODE = "MAC";//设备唯一码
    private static final String DEVICE_IP = "machine_ip";//设备IP地址
    private static final String DEVICE_RUNSTATUS = "runStatus";//运行状态
    private static final String DEVICE_BINDSTATUS = "bindStatus";//绑定状态
    private static final String DEVICE_BROAD_INFO = "broad_info";//主板信息

    public static final String LATITUDE = "latitude";//定位城市s
    public static final String LONGITUDE = "longitude";//定位城市s
    public static final String ALTITUDE = "altitude";//定位城市s
    public static final String CITY_NAME = "city_name";//定位城市s
    public static final String ADDRESS = "address";//定位城市s

    //文件缓存----------------------------------------------------------
    //布局资源
    private static final String TODAY_RES_DATA = "todayResData";//今天的资源数据
    private static final String TOMMOROW_RES_DATA = "tommResData";//明天的资源数据

    private static final String INSERT_DATA = "insertData";//明天的资源数据

    private static final String PLAY_TAG = "isHasPlayData";

    private static final String SAVE_SOUND_MUSIC = "save_sound_music";
    private static final String ADSINFO_TEMP = "adsinfo_temp";//广告信息缓存
    private static final String KEY_EXT_SD = "extSDPath";
    private static final String KEY_LAYER_TYPE = "layerType";
    private static final String KEY_APP_MODE = "appMode";

    public static class SP {

        public static boolean put(String key, String value){
            return spCache.saveString(key,value);
        }

        public static String get(String key, String defValue){
            return spCache.getString(key,defValue);
        }

        public static boolean putLayerType(Integer layerType){
            return spCache.saveInt(KEY_LAYER_TYPE,layerType);
        }

        public static int getLaterType(){
            return spCache.getInt(KEY_LAYER_TYPE,1);
        }

        public static boolean putPlayTag(boolean tag){
            return spCache.saveBoolean(PLAY_TAG,tag);
        }
        public static boolean getPlayTag(){
            return spCache.getBoolean(PLAY_TAG);
        }

        public static boolean putLatitude(String la){
            return spCache.saveString(LATITUDE, la);
        }
        public static String getLatitude(){
            return spCache.getString(LATITUDE,"");
        }

        public static boolean putLongitude(String lo){
            return spCache.saveString(LONGITUDE, lo);
        }
        public static String getLongitude(){
            return spCache.getString(LONGITUDE,"");
        }

        public static boolean putAltitude(String al){
            return spCache.saveString(ALTITUDE, al);
        }
        public static String getAltitude(){
            return spCache.getString(ALTITUDE,"");
        }

        public static boolean putCityName(String cn){
            return spCache.saveString(CITY_NAME, cn);
        }
        public static String getCityName(){
            return spCache.getString(CITY_NAME,"");
        }

        public static boolean putAddress(String add){
            return spCache.saveString(ADDRESS, add);
        }
        public static String getAddress(){
            return spCache.getString(ADDRESS,"");
        }


        /*中恒板子开关机用的是屏幕休眠，所以得设置关机的时候同时关闭声音*/
        public static boolean putCurrentVolume(String soundNum) {
            return spCache.saveString(SAVE_SOUND_MUSIC, soundNum);
        }

        public static String getCurrentVolume() {
            return spCache.getString(SAVE_SOUND_MUSIC, "0");
        }

        public static boolean putWechatTicket(String ticket) {
            return spCache.saveString(WECHAT_TICKET, ticket);
        }

        public static String getWechatTicket() {
            return spCache.getString(WECHAT_TICKET, "");
        }

        public static boolean putAccessCode(String ticket) {
            return spCache.saveString(DEVICE_ACCESS_CODE, ticket);
        }

        public static String getAccessCode() {
            return spCache.getString(DEVICE_ACCESS_CODE, "");
        }

        public static boolean putStatus(String status) {
            return spCache.saveString(DEVICE_STATUS, status);
        }

        public static String getStatus() {
            return spCache.getString(DEVICE_STATUS, "0");
        }

        public static void putBroadInfo(String info) {
            spCache.saveString(DEVICE_BROAD_INFO, info);
        }

        public static String getBroadInfo() {
            return spCache.getString(DEVICE_BROAD_INFO, "");
        }

        public static String getBindStatus() {
            return spCache.getString(DEVICE_BINDSTATUS, "");
        }

        public static void putRunStatus(String status) {
            spCache.saveString(DEVICE_RUNSTATUS, status);
        }

        public static String getRunStatus() {
            return spCache.getString(DEVICE_RUNSTATUS, "");
        }

        public static void putDeviceName(String name) {
            spCache.saveString(DEVICE_NAME, name);
        }

        public static String getDeviceName() {
            return spCache.getString(DEVICE_NAME, "");
        }

        public static void putDeviceNum(String deviceNum) {
            spCache.saveString(DEVICE_NUMBER, deviceNum);
        }

        public static String getDeviceNum() {
            return spCache.getString(DEVICE_NUMBER, "");
        }

        public static void putSettingPwd(String settingPwd) {
            spCache.saveString(DEVICE_SETTING_PWD, settingPwd);
        }

        public static String getSettingPwd() {
            return spCache.getString(DEVICE_SETTING_PWD, "");
        }

        public static void putDeviceUniCode(String code) {
            spCache.saveString(DEVICE_UNIQUE_CODE, code);
        }


        public static String getDeviceUniCode() {
            return spCache.getString(DEVICE_UNIQUE_CODE, "");
        }

        public static void putDeviceIP(String ip) {
            spCache.saveString(DEVICE_IP, ip);
        }

        public static String getDeviceIP() {
            return spCache.getString(DEVICE_IP, "");
        }

        public static boolean putExtSDPath(String path) {
            return spCache.saveString(KEY_EXT_SD,path);
        }
        public static String getExtSDPath(){
            return spCache.getString(KEY_EXT_SD,"");
        }

        public static boolean putMode(int mode) {
            return spCache.saveInt(KEY_APP_MODE,mode);
        }

        public static int getMode(){
            return spCache.getInt(KEY_APP_MODE,0);
        }
    }

    public static class FILE {

        public static void putInsertData(String insertData){
            spCache.saveString(INSERT_DATA,insertData);
        }

        public static String getInsertData(){
            return spCache.getString(INSERT_DATA,"");
        }

        public static void putTodayResource(String dataJson){
            spCache.saveString(TODAY_RES_DATA,dataJson);
        }

        public static String getTodayResource(){

            return spCache.getString(TODAY_RES_DATA,"");
        }

        public static void putTommResource(String dataJson){
            spCache.saveString(TOMMOROW_RES_DATA,dataJson);
        }

        public static String getTommResource(){
            return spCache.getString(TOMMOROW_RES_DATA,"");
        }

        public static boolean putTXTAds(InsertTextModel insertTextModel){
            String adsStr = null;
            if(insertTextModel != null){
                adsStr = new Gson().toJson(insertTextModel);
            }
            return spCache.saveString(ADSINFO_TEMP,adsStr);
        }

        public static InsertTextModel getTXTAds(){
            String string = spCache.getString(ADSINFO_TEMP, null);
            if(!TextUtils.isEmpty(string)){
                return new Gson().fromJson(string,InsertTextModel.class);
            }
            return null;
        }
    }

}



