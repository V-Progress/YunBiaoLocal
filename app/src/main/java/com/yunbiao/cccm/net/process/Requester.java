package com.yunbiao.cccm.net.process;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.model.ConfigResponse;
import com.yunbiao.cccm.net.model.VideoDataModel;
import com.yunbiao.cccm.net.resolve.XMLParse;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.NetUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * Created by Administrator on 2019/4/26.
 */

public class Requester {
    private static final String TAG = "Requester";

    private static final String REQ_FAILED_TAG = "-1";
    private static final String FAILED_REQ_CONFIG = "1";//请求config失败
    private static final String FAILED_DOWNLOAD_CONFIG = "2";//下载config失败
    private static final String FAILED_RESOLVE_CONFIG = "3";//解析config失败
    private static String ftpServiceUrl;

    public interface RequestConfigListener {
        void noData(Date reqDate);

        void onError(Date reqDate, String errCode);

        void onSuccess(Date reqDate, String ftpServiceUrl, String playUrl, VideoDataModel videoDataModel);

        void onFinish();
    }

    public static void requestConfig(List<String> reqDatelist,@NonNull RequestConfigListener listener) {
        long l = System.currentTimeMillis();
        LogUtil.D(TAG, "总体请求开始：" + l);

        if (reqDatelist == null || reqDatelist.size() <= 0) {
            LogUtil.D(TAG, "请求日期列表为空，异常");
            return;
        }

        for (String date : reqDatelist) {
            long ol = System.currentTimeMillis();
            LogUtil.D(TAG, "单日请求开始：" + ol);
            //开始请求
            String reCode = request(date);
            Date reqDate = DateUtil.yyyy_MM_dd_Parse(date);

            switch (reCode) {
                case REQ_FAILED_TAG://没有数据
                    listener.noData(reqDate);
                    break;
                case FAILED_REQ_CONFIG://请求失败
                case FAILED_DOWNLOAD_CONFIG:
                case FAILED_RESOLVE_CONFIG:
                    listener.onError(reqDate, reCode);
                    break;
                default://请求成功
                    VideoDataModel videoDataModel = null;
                    try {
                        videoDataModel = new Gson().fromJson(reCode, VideoDataModel.class);
                    } catch (Exception e) {
                        listener.onError(reqDate, FAILED_RESOLVE_CONFIG);
                        continue;
                    }

                    if (videoDataModel == null) {
                        listener.onError(reqDate, FAILED_RESOLVE_CONFIG);
                        continue;
                    }

                    VideoDataModel.Config config = videoDataModel.getConfig();
                    if (config == null) {
                        listener.onError(reqDate, FAILED_RESOLVE_CONFIG);
                        continue;
                    }

                    String playUrl = config.getPlayurl();
                    if (TextUtils.isEmpty(playUrl)) {
                        listener.onError(reqDate, FAILED_RESOLVE_CONFIG); // TODO: 2019/9/18 此处为解析失败
                        continue;
                    }

                    listener.onSuccess(reqDate,ftpServiceUrl, playUrl, videoDataModel);
                    break;
            }

            long oe = System.currentTimeMillis();
            LogUtil.D(TAG, "单日请求结束：" + oe + "---耗时：" + (oe - ol));
        }
        listener.onFinish();

        long e = System.currentTimeMillis();
        LogUtil.D(TAG, "总体请求结束：" + e + "---耗时：" + (e - l));
    }

    /***
     * 请求config文件
     * @param date 需要获取的日期
     * @return 返回数字请求失败，返回Json请求成功
     */
    private static String request(String date) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("playDate", date);
        LogUtil.D(TAG, "开始获取" + date + "的数据：" + map.toString());

        //请求获取资源
        Response response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
        if (response == null) {
            LogUtil.E(TAG, "获取Config地址失败，无响应");
            return FAILED_REQ_CONFIG;
        }
        //取出响应
        String responseStr = null;
        try {
            responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                LogUtil.E(TAG, "获取Config地址失败：response为null");
                return FAILED_REQ_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "获取Config地址失败：获取响应体失败" + e.getMessage());
            return FAILED_REQ_CONFIG;
        }
        LogUtil.D(TAG, "获取Config地址响应：" + responseStr);

        ConfigResponse configResponse;
        try {
            //转换成bean
            configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.D(TAG, date + "没有Config文件");
                return REQ_FAILED_TAG;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.E(TAG, "解析config地址响应失败：" + e.getClass().getSimpleName() + " " + e.getMessage());
            return FAILED_REQ_CONFIG;
        }

        //获取两种地址
        ConfigResponse.Data dataJson = configResponse.getDataJson();
        ftpServiceUrl = dataJson.getFtpServiceUrl();
        String configUrl = dataJson.getConfigUrl();

        //下载config文件
        Response configXml;
        try {
            configXml = NetUtil.getInstance().downloadSync(configUrl);
            if (configXml == null) {
                LogUtil.E(TAG, "下载config文件失败：下载结果为null");
                return FAILED_DOWNLOAD_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "下载config文件失败：IOException " + e.getMessage());
            return FAILED_DOWNLOAD_CONFIG;
        }

        //取出config内容
        String configStr;
        try {
            configStr = configXml.body().string();
            if (TextUtils.isEmpty(configStr)) {
                LogUtil.E(TAG, "下载config文件失败：空串");
                return FAILED_DOWNLOAD_CONFIG;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "下载config文件失败：IOException " + e.getMessage());
            return FAILED_DOWNLOAD_CONFIG;
        }

        //转换成bean
        VideoDataModel videoDataModel;
        try {
            videoDataModel = new XMLParse().parseVideoModel(configStr);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "config文件解析失败：IOException " + e.getMessage());
            return FAILED_RESOLVE_CONFIG;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            LogUtil.E(TAG, "config文件解析失败：XmlPullParserException " + e.getMessage());
            return FAILED_RESOLVE_CONFIG;
        }

        String videoDataStr = new Gson().toJson(videoDataModel);
        LogUtil.D(TAG, date + "的config文件下载成功：" + videoDataStr);
        return videoDataStr;
    }

}
