package com.yunbiao.yunbiaolocal.cache;

import android.os.Environment;
import android.text.TextUtils;

import com.yunbiao.yunbiaolocal.common.ResourceConst;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class CacheManager {
    private static final String LAYOUT_CACHE_CONSTANT = "layoutJson";
    private static final String LAYOUT_POSITION = "layoutPosition";
    private static final String DEVICE_SERNUM_CONSTANT = "serNum";
    private static final String DEVICE_RUNKEY_CONSTANT = "RUNKEY";
    private static final String DEVICE_TYPE_CONSTANT = "DTYPE";
    private static final String LAYOUT_CURRENT_CONSTANT = "CURRENT";
    private static final String DEVICE_ACTIVE_NUMBER = "pwd";
    private static final String MAC_INFO_CONSTANT = "MAC";
    private static final String SCREEN_ROTATE = "rotate";

    private static final String MECHINE_IP = "machine_ip";
    private static final String USED_SD = "usedSd";//用户存储用的内置sd卡或者外置sd卡

    private static final String SAVE_SOUND_MUSIC = "save_sound_music";
    private static final String DEVICE_RUNSTATUS = "runStatus";
    private static final String DEVICE_BINDSTATUS = "bindStatus";
    private static final String EXPIRE_DATE = "expireDate";
    private static final String DECIVENAME = "deciveName";
    private static final String DEVICEQRCODE = "deviceQrCode";
    private static final String FACEDETECT = "faceDetect";
    private static final String FACESHOW = "faceShow";
    private static final String ISMIRROR = "isMirror ";

    private static final String BROAD_INFO = "broad_info";//主板信息

    private static final String ADSINFO_TEMP="adsinfo_temp";//广告信息缓存

    private static final String CACHE_SAVE_PATH = ResourceConst.LOCAL_RES.PROPERTY_CACHE_PATH;

    private static FileCache acache = FileCache.get(new File(CACHE_SAVE_PATH));

    public static void putAdsInfoTemp(String adsInfo){
        acache.put(ADSINFO_TEMP,adsInfo);
    }

    public static String getAdsinfoTemp(){
        return acache.getAsString(ADSINFO_TEMP);
    }

    public static JSONObject getLayoutCacheAsObject() {
        return acache.getAsJSONObject(LAYOUT_CACHE_CONSTANT);
    }

    public static void putLayoutCache(String layoutJson) {
        acache.put(LAYOUT_CACHE_CONSTANT, layoutJson);
    }

    public static String getLayoutCacheAsString() {
        return acache.getAsString(LAYOUT_CACHE_CONSTANT);
    }

    public static void putMachineIp(String machineip) {
        acache.put(MECHINE_IP, machineip);
    }

    public static String getMechineIp() {
        return acache.getAsString(MECHINE_IP);
    }

    public static JSONArray getLayoutCacheAsArray() {
        return acache.getAsJsonArray(LAYOUT_CACHE_CONSTANT);
    }

    public static void putSerNumber(String serNum) {
        acache.put(DEVICE_SERNUM_CONSTANT, serNum);
    }

    public static void putRunStatus(String runStatus) {
        acache.put(DEVICE_RUNSTATUS, runStatus);
    }

    public static String getRunStatus() {
        return acache.getAsString(DEVICE_RUNSTATUS);
    }

    public static void putCodePayStatus(String codePayStatus) {
        acache.put(DEVICEQRCODE, codePayStatus);
    }

    public static String getCodePayStatus() {
        return acache.getAsString(DEVICEQRCODE);
    }

    public static void putBindStatus(String bindStatus) {
        acache.put(DEVICE_BINDSTATUS, bindStatus);
    }

    public static String getBindStatus() {
        return acache.getAsString(DEVICE_BINDSTATUS);
    }

    public static void putExpireDate(String expireDate) {
        acache.put(EXPIRE_DATE, expireDate);
    }

    public static String getExpireDate() {
        return acache.getAsString(EXPIRE_DATE);
    }

    public static void putPwd(String pwd) {
        acache.put(DEVICE_ACTIVE_NUMBER, pwd);
    }

    public static String getPwd() {
        return acache.getAsString(DEVICE_ACTIVE_NUMBER);
    }

    public static void putDeviceQrCode(String pwd) {
        acache.put(DEVICEQRCODE, pwd);
    }

    public static String getDeviceQrCode() {
        return acache.getAsString(DEVICEQRCODE);
    }

    public static void putDecName(String name) {
        acache.put(DECIVENAME, name);
    }

    public static String getDecName() {
        return acache.getAsString(DECIVENAME);
    }

    public static void putRotate(String rotate) {
        acache.put(SCREEN_ROTATE, rotate);
    }

    public static String getRotate() {
        String rotate = acache.getAsString(SCREEN_ROTATE);
        if (!TextUtils.isEmpty(rotate)) {
            return rotate;
        } else {
            return "";
        }
    }

    public static void putSdPath(String sdPath) {
        acache.put(USED_SD, sdPath);
    }

    public static String getSdPath() {
        return acache.getAsString(USED_SD);
    }

    public static void putRunKey(String runKey) {
        acache.put(DEVICE_RUNKEY_CONSTANT, runKey);
    }

    public static String getSerNumber() {
        return acache.getAsString(DEVICE_SERNUM_CONSTANT);
    }

    public static String getRunKey() {
        return acache.getAsString(DEVICE_RUNKEY_CONSTANT);
    }

    public static void putDTypeKey(String dType) {
        acache.put(DEVICE_TYPE_CONSTANT, dType);
    }

    public static void putCurrentLayout(String json) {
        acache.put(LAYOUT_CURRENT_CONSTANT, json);
    }

    public static JSONObject getCurrentLayout() {
        return acache.getAsJSONObject(LAYOUT_CURRENT_CONSTANT);
    }

    public static String getDType() {
        String s = acache.getAsString(DEVICE_TYPE_CONSTANT);
        if (s == null || s.equals("")) {
            s = "-1";
        }
        return s;
    }

    public static void putDeviceNo(String mac) {
        acache.put(MAC_INFO_CONSTANT, mac);
    }

    public static String getDeviceNo() {
        return acache.getAsString(MAC_INFO_CONSTANT);
    }

    public static void clearLayout() {
        acache.clear();
    }

    public static void removeLayout() {
        acache.remove(LAYOUT_CACHE_CONSTANT);
    }

    /*中恒板子开关机用的是屏幕休眠，所以得设置关机的时候同时关闭声音*/
    public static void putCurrentVolume(String soundNum) {
        acache.put(SAVE_SOUND_MUSIC, soundNum);
    }

    public static String getCurrentVolume() {
        String currentVolume = acache.getAsString(SAVE_SOUND_MUSIC);
        if (TextUtils.isEmpty(currentVolume)) {
            return "0";
        }
        return currentVolume;
    }

    public static void putFaceDetect(String detect) {
        acache.put(FACEDETECT, detect);
    }

    public static String getFaceDetect() {
        return acache.getAsString(FACEDETECT);
    }

    public static void putFaceShow(String show) {
        acache.put(FACESHOW, show);
    }

    public static String getFaceShow() {
        return acache.getAsString(FACESHOW);
    }

    public static void putBroadInfo(String broadInfo) {
        acache.put(BROAD_INFO, broadInfo);
    }

    public static String getBroadInfo() {
        return acache.getAsString(BROAD_INFO);
    }

    public static void putLayoutPosition(String layoutPosition) {//0网络  1本地
        acache.put(LAYOUT_POSITION, layoutPosition);
    }

    public static String getLayoutPosition() {
        return acache.getAsString(LAYOUT_POSITION);
    }

    public static void putIsMirror(String isMirror) {
        acache.put(ISMIRROR, isMirror);
    }

    public static String getIsMirror() {
        return acache.getAsString(ISMIRROR);
    }

    public static void putInsertAds(String insertContent){
        acache.put(ADSINFO_TEMP,insertContent);
    }

    public static String getInsertAds(){
        return acache.getAsString(ADSINFO_TEMP);
    }

    public static final String INSERT_TYPE = "insert_type";
    public static Integer getInsertType(){
        String insertType = acache.getAsString(INSERT_TYPE);
        if(TextUtils.isEmpty(insertType)){
            return 0;
        }
        return Integer.valueOf(insertType);
    }

    public static void putInsertType(int insertType){
        acache.put(INSERT_TYPE,String.valueOf(insertType));
    }
}



