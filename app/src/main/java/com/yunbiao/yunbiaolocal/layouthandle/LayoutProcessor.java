package com.yunbiao.yunbiaolocal.layouthandle;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.layouthandle.bean.LayoutModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Administrator on 2018/11/27.
 */

public class LayoutProcessor {

    private final int LAYOUT_MUSIC = -3;//音乐
    private final int LAYOUT_TEXT = 2;//音乐
    private final int LAYOUT_BG = 0;//大背景处理,纯色或图片
    private final int LAYOUT_IMG_VIDEO = 1;//图片或视频
    private final int LAYOUT_BGM = 6;//背景音乐
    private final int LAYOUT_VIDEO = 3;//视频处理 目前没用跟图片合并
    private final int LAYOUT_WECHAT = 4;// 微信处理
    private final int LAYOUT_WEB = 5;//网页

    private final int LAYOUT_LCOALREC = 8;//本地资源
    private final int LAYOUT_CAMERA = 12;//摄像头
    private final int LAYOUT_QUEUE = 13;//排队叫好
    private final int LAYOUT_BASE_ = 14;//基础控件
    private final int LAYOUT_TOUCH_QUERY = 15;//触摸查询
    private final int LAYOUT_BAIDU_ADS = 17;//百度广告联盟
    private final int LAYOUT_SELF_ADS = 18;//自运营广告
    private BufferedReader br;
    private HashMap<Object, Object> map;
    private boolean land;

    public void handleLayout(){
        // 初始化数据
        map = new HashMap<>();
        map.put("layout", "1");
        map.put("sytle", "1");
        try {
            InputStream is = APP.getContext().getAssets().open("layout/layout" + map.get("layout") + (!map.get("layout").equals("1") && land ? "_land" : "") + ".txt");

            BufferedReader br;
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(is));
            String s;

            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }

            String jsonObject = JSONArray.parseArray(sb.toString()).getJSONObject(0).toString();
            Log.e("123","1111-----"+jsonObject);

            LayoutModel layoutModel = new Gson().fromJson(jsonObject, LayoutModel.class);
            Log.e("123","2222-----"+layoutModel.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

}
