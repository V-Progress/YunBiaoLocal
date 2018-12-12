package com.yunbiao.yunbiaolocal.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by LiuShao on 2016/2/21.
 */
public class SpUtils {

    private static SharedPreferences sp;
    private static final String SP_NAME = "service";
    public static final String TV_LOCATION = "location";
    public static final String TV_FONTSIZE = "fontSize";
    public static final String TV_BACKGROUD = "background";
    public static final String TV_PLAYTYPE = "playType";
    public static final String TV_FONTCOLOR = "fontColor";
    public static final String TV_PLAYSPEED = "playSpeed";
    public static final String TV_PLAYDATE = "playDate";
    public static final String TV_PLAYTIME = "playTime";
    public static final String TV_TEXT = "text";
    public static final String TV_PLAYCURTIME = "playCurTime";
    public static final String TV_TRANSPARENT = "transparent";
    public static final String TV_SPEECHCOUNT = "speechCount";


    public static final String LAYOUTTYPE = "layout_type";//布局类型
    public static final String VIDEO_LOCAL = "video_local";//本地视频地址
    public static final String IMAGE_LOCAL = "image_local";//本地图片地址
    public static final String IMAGE_NET = "image_net";//网络图片地址

    //wifi连接
    public static final String WIFI_SSID = "wifi_ssid";//SSID
    public static final String WIFI_PWD = "wifi_pwd";//pwd
    public static final String WIFI_TYPE = "wifi_type";//type

    public static final String CITY_NAME = "city_name";//定位城市
    public static final String MENU_PWD = "menu_pwd";//用户访问密码

    //联屏的本地地址
    public static final String UNICOM_VIDEO_PATH = "unicom_video_path";//本地视频地址
    public static final String UNICOM_IMG_PATH = "unicom_img_path";//本地图片地址
    public static final String UNICOM_ROW = "unicom_row";//行
    public static final String UNICOM_COL = "unicom_col";//列
    public static final String UNICOM_ISSERVICER = "unicom_isservicer";//是否是主机
    public static final String UNICOM_URL = "unicom_url";//联屏资源

    //广告播放索引
    public static final String ADVERT_INDEX="advert_index";
    //设备类型
    public static final String DEVICETYPE="deviceType";

    //外部储存路径（记录上一次识别的U盘路径，防止U盘关机未拔掉）
    public static final String OUTER_PATH="outer_path";
    public static final String EXTSD_PATH="extsd_path";


    public static void saveString(Context context, String key, String value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        return sp.getString(key, defValue);
    }

    public static void saveInt(Context context, String key, int value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        return sp.getInt(key, value);
    }

    public static void saveFloat(Context context, String key, float value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        sp.edit().putFloat(key, value).apply();
    }

    public static float getFloat(Context context, String key, float value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        return sp.getFloat(key, value);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_NAME, 0);
        return sp.getBoolean(key, defValue);
    }


    public static String SceneList2String(List SceneList) {
        try {
            // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // 然后将得到的字符数据装载到ObjectOutputStream
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
            objectOutputStream.writeObject(SceneList);
            // 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
            String SceneListString = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            // 关闭objectOutputStream
            objectOutputStream.close();

            return SceneListString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List String2SceneList(String SceneListString) {
        try {
            byte[] mobileBytes = Base64.decode(SceneListString.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            List SceneList = (List) objectInputStream.readObject();
            objectInputStream.close();
            return SceneList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
