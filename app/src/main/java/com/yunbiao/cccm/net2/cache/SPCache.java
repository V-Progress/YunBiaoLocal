package com.yunbiao.cccm.net2.cache;

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
public class SPCache {

    private static SharedPreferences sp;
    private static final String SP_NAME = "service";

    public SPCache(Context context) {
        if (sp == null){
            sp = context.getSharedPreferences(SP_NAME, 0);
        }
    }

    public static boolean saveString(String key, String value) {
        return sp.edit().putString(key, value).commit();
    }

    public static String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public static boolean saveBoolean(String key,boolean value){
        return sp.edit().putBoolean(key,value).commit();
    }

    public static boolean getBoolean(String key){
        return sp.getBoolean(key,false);
    }

    public static boolean saveInt(String key,int value){
        return sp.edit().putInt(key,value).commit();
    }

    public static int getInt(String key, Integer defValue){
        return sp.getInt(key,defValue);
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
