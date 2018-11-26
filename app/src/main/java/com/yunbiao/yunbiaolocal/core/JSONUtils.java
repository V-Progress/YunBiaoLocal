package com.yunbiao.yunbiaolocal.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/11/26.
 */

public class JSONUtils {

    public void getInstance(){

    }

    public static Object get(String jsonStr, String name){
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject.get(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
