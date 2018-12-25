package com.yunbiao.cccm.download;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.resolve.VideoDataModel;
import com.yunbiao.cccm.resolve.XMLParse;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/12/21.
 */

public class DownloadManager {
    private final String TAG = getClass().getSimpleName();
    private final long downloadDelay = 60000;//失败后延迟下载时间
    private final String REQ_FAILED_TAG = "-1";
    private final int TYPE_TODAY = 1;
    private final int TYPE_TOMMO = 2;
    private boolean isInit = true;//是否init，只有在是的情况下，会从1开始请求，请求到2的时候会将isInit置为false
    private String ftpServiceUrl;//FTP文件地址

    private static DownloadManager instance;

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initResData() {
        isInit = true;
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(String.valueOf(TYPE_TODAY));
            }
        });
    }

    /***
     * 单项请求configXML，将isInit置为false，请求完毕后不会重复请求。
     * @param type
     */
    public void requestConfigXML(final String type) {
        isInit = false;
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(type);
            }
        });

    }

    private void requestRes(final String type) {
        LogUtil.E("日期类型是："+type);
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("type", type);
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.GET_RESOURCE, map, new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                try {
                    Thread.sleep(downloadDelay);
                    requestRes(type);//请求异常之后再次请求
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                LogUtil.E(TAG, e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtil.D(TAG, "------------" + response);
                JSONObject jsonObject = JSON.parseObject(response);
                String result = jsonObject.getString("result");
                String message = jsonObject.getString("message");
                if (TextUtils.equals(REQ_FAILED_TAG, result)) {
                    LogUtil.E(TAG, message);
                    return;
                }

                JSONObject dateJson = jsonObject.getJSONObject("dateJson");
                ftpServiceUrl = dateJson.getString("ftpServiceUrl");

                String configUrl = dateJson.getString("configUrl");
                downloadConfigXML(configUrl);
            }
        });
    }

    //下载config文件
    private void downloadConfigXML(final String url) {
        NetUtil.getInstance().downloadFile(url, ResourceConst.LOCAL_RES.APP_MAIN_DIR, new NetUtil.OnDownLoadListener() {
            @Override
            public void onStart(String fileName) {
                LogUtil.D(TAG, "开始下载config文件");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "下载中---" + progress);
            }

            @Override
            public void onComplete(File response) {
                LogUtil.D(TAG, "下载完成：" + response.getAbsolutePath());

                List<String> strings = null;
                try {
                    strings = resolveDownloadList(response);

                    download(strings);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onError(Exception e) {
                LogUtil.E(TAG, "下载错误：" + e.getMessage() + ",60秒后重新下载");
                try {
                    Thread.sleep(downloadDelay);
                    LogUtil.D(TAG, "重新开始下载config文件");
                    downloadConfigXML(url);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    //解析XML文件
    private List<String> resolveDownloadList(File response) throws UnsupportedEncodingException {
        List<String> urlList = new ArrayList<>();
        VideoDataModel videoDataModel = new XMLParse().parseJsonModel(response);
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        for (VideoDataModel.Play play : playlist) {
            String today = DateUtil.yyyyMMdd_Format(new Date());
            //判断是否有今天的数据
            if (TextUtils.equals(play.getPlayday(), today)) {
                List<VideoDataModel.Play.Rule> rules = play.getRules();
                for (VideoDataModel.Play.Rule rule : rules) {
                    String res = rule.getRes();
                    String[] split = res.split(",");
                    for (String rPath : split) {
                        String urlStr = rPath.replace("\n", "").trim().replace("%20", "");
                        if (!TextUtils.isEmpty(urlStr)) {
                            String url = null;
                            url = URLEncoder.encode(urlStr, "UTF-8");
                            if (!urlList.contains(url)) {
                                urlList.add(ftpServiceUrl+url);
                            }
                        }
                    }
                }
            }
        }
        return urlList;
    }

    //开始多文件下载
    private void download(List<String> urlList) {
        BPDownloadUtil.getInstance().breakPointDownload(urlList, new MutiFileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                LogUtil.E(TAG, "共有：" + totalNum);
            }

            @Override
            public void onStart(int currNum) {
                LogUtil.E(TAG, "第" + currNum + "文件开始");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.E(TAG, "进度：" + progress);
            }

            @Override
            public void onDownloadSpeed(long speed) {
                LogUtil.E(TAG, "速度：" + speed);
            }

            @Override
            public void onSuccess(int currFileNum) {
                LogUtil.E(TAG, "第 " + currFileNum + " 个文件下载完成");
            }

            @Override
            public void onError(Exception e) {
                LogUtil.E(TAG, "下载出现错误：" + e.getMessage());
            }

            @Override
            public void onFinish() {
                LogUtil.E(TAG, "资源全部下载结束");
                if (isInit) {
                    LogUtil.D("dataTag不为2，继续开始请求");
                    requestConfigXML(String.valueOf(TYPE_TOMMO));//请求明天时会将isInit置为false
                }
            }

            @Override
            public void onFailed(Exception e) {
                LogUtil.E(TAG, "下载失败：" + e.getMessage());
            }
        });
    }
}
