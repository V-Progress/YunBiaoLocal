package com.yunbiao.cccm.download;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.InsertManager;
import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.netcore.bean.ConfigResponse;
import com.yunbiao.cccm.resolve.VideoDataModel;
import com.yunbiao.cccm.resolve.XMLParse;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * 现请求获取config，下载config，解析config，下载资源
 * 为了便于管理，请求全部为同步请求
 * <p>
 * Created by Administrator on 2018/12/21.
 */

public class ResourceManager {
    private final String TAG = getClass().getSimpleName();
    private final long downloadDelay = 60000;//失败后延迟下载时间
    private final String REQ_FAILED_TAG = "-1";
    private final int TYPE_TODAY = 1;
    private final int TYPE_TOMMO = 2;
    private boolean isInit = false;
    private String ftpServiceUrl;//FTP文件地址

    private static ResourceManager instance;
    private final List<String> urlList;

    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public ResourceManager() {
        urlList = new ArrayList<>();
    }

    /***
     * 初始化，按顺序请求两天的数据
     * dateTag 初始化dateTag，为0时只有单项请求，为1时候才会重复请求
     */
    public void initResData() {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(TYPE_TODAY);
            }
        });
    }

    /***
     * 单项请求configXML，将isInit置为false，请求完毕后不会重复请求。
     * @param type
     */
    public void requestConfigXML(final int type) {
        if (type == TYPE_TOMMO) {
            isInit = false;
        }
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                requestRes(type);
            }
        });
    }

    private void requestRes(final int type) {
        if (type == TYPE_TODAY) {
            isInit = true;
        }

        LogUtil.E("日期类型是：" + type);
        Map<String, String> map = new HashMap<>();
        map.put("deviceNo", HeartBeatClient.getDeviceNo());
        map.put("type", String.valueOf(type));

        Response response = null;
        try {
            //请求获取config
            response = NetUtil.getInstance().postSync(ResourceConst.REMOTE_RES.GET_RESOURCE, map);
            if (response == null) {
                throw new IOException("response is null");
            }
            //取出响应
            String responseStr = response.body().string();
            if (TextUtils.isEmpty(responseStr)) {
                throw new IOException("response's body is null");
            }

            //转换成bean
            ConfigResponse configResponse = new Gson().fromJson(responseStr, ConfigResponse.class);
            if (TextUtils.equals(REQ_FAILED_TAG, configResponse.getResult())) {
                LogUtil.E(TAG, configResponse.getMessage());
                if (isInit) {
                    requestConfigXML(TYPE_TOMMO);//请求明天时会将isInit置为false
                }
                return;
            }

            //获取两种地址
            ConfigResponse.Data dataJson = configResponse.getDataJson();
            ftpServiceUrl = dataJson.getFtpServiceUrl();
            String configUrl = dataJson.getConfigUrl();

            //下载config文件
            Response configXml = NetUtil.getInstance().downloadSync(configUrl);
            if (configXml == null) {
                throw new IOException("download response's body is null");
            }

            //取出config内容
            String configStr = configXml.body().string();
            if (TextUtils.isEmpty(configStr)) {
                throw new IOException("config.xml is null");
            }

            //转换成bean
            VideoDataModel videoDataModel = new XMLParse().parseVideoModel(configStr);
            String videoDataStr = new Gson().toJson(videoDataModel);

            //缓存
            if (type == TYPE_TODAY) {
                //判断如果是今天的数据再进行缓存
                CacheManager.FILE.putTodayResource(videoDataStr);
            }

            //解析下载地址
            List<String> urlList = resolveDownloadList(videoDataModel);

            //开始下载
            download(urlList);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    //解析XML文件
    private List<String> resolveDownloadList(VideoDataModel videoDataModel) {
        List<VideoDataModel.Play> playlist = videoDataModel.getPlaylist();
        String today = DateUtil.yyyyMMdd_Format(new Date());

        for (VideoDataModel.Play play : playlist) {
            //判断是否有今天的数据
            if (TextUtils.equals(play.getPlayday(), today)) {
                List<VideoDataModel.Play.Rule> rules = play.getRules();
                for (VideoDataModel.Play.Rule rule : rules) {
                    String res = rule.getRes();
                    String[] split = res.split(",");
                    for (String rPath : split) {
                        String urlStr = rPath.replace("\n", "").trim().replace("%20", "");
                        if (!TextUtils.isEmpty(urlStr)) {
                            if (!urlList.contains(urlStr)) {
                                urlList.add(ftpServiceUrl + urlStr);
                            }

                        }
                    }
                }
            }
        }
        return urlList;
    }

    public void download(final List<String> urlList, final MultiFileDownloadListener listener) {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                BPDownloadUtil.getInstance().breakPointDownload(urlList, listener);
            }
        });
    }

    //开始多文件下载
    private void download(final List<String> urlList) {
        BPDownloadUtil.getInstance().breakPointDownload(urlList, new FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                if (isInit) {
                    //开启控制台
                    MainController.getInstance().openConsole();
                    MainController.getInstance().updateConsole("准备下载资源...共有：" + totalNum + "个文件");
                    MainController.getInstance().initProgress(totalNum);
                }
            }

            @Override
            public void onStart(int currNum) {
                MainController.getInstance().updateConsole("开始下载第" + currNum + 1 + "个文件");
            }

            @Override
            public void onSuccess(int currFileNum) {
                MainController.getInstance().updateProgress(currFileNum + 1);
                MainController.getInstance().updateConsole("下载完成");
                if (isInit) {
                    MainController.getInstance().initPlayData(true);
                }
            }

            @Override
            public void onError(Exception e) {
                MainController.getInstance().updateConsole("下载错误:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onFinish() {
                urlList.clear();
                if (isInit) {
                    MainController.getInstance().initPlayData(true);
                    MainController.getInstance().updateProgress(urlList.size());
                    MainController.getInstance().updateConsole("已全部下载结束");
                    MainController.getInstance().closeConsole();
                    LogUtil.D("dataTag不为2，继续开始请求");
                    requestConfigXML(TYPE_TOMMO);//请求明天时会将isInit置为false
                }
            }

            @Override
            public void onFailed(Exception e) {
                MainController.getInstance().updateConsole("下载失败，请检查网络或Config:" + e.getMessage());
            }
        });

    }

    /***
     * 初始化插播数据
     */
    public void initInsertData() {
        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = ResourceConst.REMOTE_RES.INSERT_CONTENT;
                    Map<String, String> params = new HashMap<>();
                    params.put("deviceNo", HeartBeatClient.getDeviceNo());
                    Response response = NetUtil.getInstance().postSync(url, params);
                    if (response == null) {
                        throw new Exception("GET response is NULL : " + url);
                    }
                    String jsonStr = response.body().string();
                    if (TextUtils.isEmpty(jsonStr)) {
                        throw new Exception("Json String is NULL : " + url);
                    }
                    LogUtil.E("请求结果：" + jsonStr);
                    InsertVideoModel insertVideo = new Gson().fromJson(jsonStr, InsertVideoModel.class);
                    if (insertVideo == null) {
                        throw new Exception("Resolve ConfigResponse failed");
                    }

                    if (insertVideo.getResult() != 1) {
                        throw new Exception(insertVideo.getMessage());
                    }

                    InsertManager.getInstance(APP.getMainActivity()).insertVideo(insertVideo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static abstract class FileDownloadListener implements MultiFileDownloadListener {
        @Override
        public void onBefore(int totalNum) {

        }

        @Override
        public void onStart(int currNum) {

        }

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onDownloadSpeed(long speed) {

        }

        @Override
        public void onSuccess(int currFileNum) {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onFailed(Exception e) {

        }
    }
}
