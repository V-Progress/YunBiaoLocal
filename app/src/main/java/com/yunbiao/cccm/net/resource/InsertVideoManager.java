package com.yunbiao.cccm.net.resource;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.net.process.Retryer;
import com.yunbiao.cccm.net.resolve.InsertResolver;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.utils.ConsoleUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.net.model.InsertVideoModel;
import com.yunbiao.cccm.net.download.BPDownloadManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertVideoManager {
    private String TAG = this.getClass().getSimpleName();

    private static InsertVideoManager insertManager;
    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private BPDownloadManager bpDownloadManager;

    private final int TYPE_VIDEO = 1;
    private final Date todayDate;

    public static InsertVideoManager getInstance() {
        if (insertManager == null) {
            insertManager = new InsertVideoManager();
        }
        return insertManager;
    }

    public InsertVideoManager() {
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());
    }

    public void clearTimer(){
        InsertResolver.instance().clearTimer();
    }

    /***
     * 初始化插播数据
     */
    public void initData() {

        ThreadUtil.getInstance().runInRemoteThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtil.D(TAG, "开始请求插播资源");
                    String url = ResourceConst.REMOTE_RES.INSERT_CONTENT;
                    Map<String, String> params = new HashMap<>();
                    params.put("deviceNo", HeartBeatClient.getDeviceNo());
                    Response response = NetUtil.getInstance().postSync(url, params);
                    if (response == null) {
                        throw new Exception("request Insert Data Error");
                    }
                    String jsonStr = response.body().string();
                    if (TextUtils.isEmpty(jsonStr)) {
                        throw new Exception("Json String is NULL : " + url);
                    }
                    LogUtil.D(TAG, "插播资源：" + jsonStr);
                    InsertVideoModel insertVideo = new Gson().fromJson(jsonStr, InsertVideoModel.class);
                    if (insertVideo == null) {
                        throw new Exception("Resolve ConfigResponse failed");
                    }

                    if (insertVideo.getResult() != 1) {
                        throw new Exception(insertVideo.getMessage());
                    }

                    insertPlay(insertVideo);
                } catch (Exception e) {
                    LogUtil.E(TAG, "处理插播资源出现异常：" + e.getMessage());
                }
            }
        });
    }

    /***
     * 插播类
     * ==================================================================================
     * @param ivm
     */
    List<String> urlList = new ArrayList<>();
    public void insertPlay(InsertVideoModel ivm) {
        if (ivm == null) {
            return;
        }

        urlList.clear();

        CacheManager.FILE.putInsertData(new Gson().toJson(ivm));
        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();

        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();
        if (insertArray == null || insertArray.size() <= 0) {
            MainController.getInstance().setHasInsert(false);
            InsertResolver.instance().stopInsert();
            return;
        }

        Date today = null;
        try {
            today = yyyyMMddHH_mm_ss.parse(yyyyMMddHH_mm_ss.format(todayDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (InsertVideoModel.Data data : insertArray) {
            if (data == null) {
                continue;
            }
            String content = data.getContent();
            String startTime = data.getStartTime();
            String endTime = data.getEndTime();
            Integer playType = data.getPlayType();

            Date[] dateArray = TimeResolver.resolve(startTime,endTime);
            if (dateArray == null) {
                continue;
            }

            if (today.after(dateArray[1])) {
                continue;
            }

            if(playType == TYPE_VIDEO){
                String[] playArray = content.split(",");//取出播放列表
                //拼装播放列表
                for (String s : playArray) {
                    if(TextUtils.isEmpty(s)){
                        continue;
                    }

                    String downloadUrl = ftpUrl + s;
                    if(!urlList.contains(downloadUrl)){
                        urlList.add(downloadUrl);
                    }
                }
            } else {
                InsertResolver.instance().init();
            }
        }

        if(urlList.size() <= 0){
            return;
        }

        download(urlList);
    }

    /*=======视频处理流程================================================================*/
    Queue<String> faileQueue = new LinkedList<>();
    public void download(final List<String> urlList) {
        if(bpDownloadManager != null){
            bpDownloadManager.cancel();
        }
        bpDownloadManager = new BPDownloadManager(getClass(),new FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
            }

            @Override
            public void onStart(int currNum) {
                LogUtil.D(TAG, "开始下载");
                MainController.getInstance().openInsertConsole();
                MainController.getInstance().updateInsertName("第"+currNum+"个文件");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "进度：" + progress);
                MainController.getInstance().updateInsertPb(String.valueOf(progress));
            }

            @Override
            public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
                LogUtil.D(TAG, "下载出错：" + e.getMessage());
                for (String fileUrl : urlList) {
                    boolean contains = fileUrl.contains(fileName);
                    if(contains){
                        faileQueue.offer(fileUrl);
                    }
                }
            }

            @Override
            public void onSuccess(int currFileNum, int totalNum, String fileName) {
                super.onSuccess(currFileNum, totalNum, fileName);
            }

            @Override
            public void onCancel() {
                MainController.getInstance().closeInsertConsole();
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "onFinish: 1111111111111111");
                if(faileQueue.size() <= 0){
                    MainController.getInstance().closeInsertConsole();
                }
                InsertResolver.instance().init();
                retry();
            }
        });
        bpDownloadManager.startDownload(urlList);
    }

    private void retry(){
        if(!(faileQueue.size() > 0)){
            return;
        }
        Retryer retryer = new Retryer(faileQueue);
        retryer.start();
    }


    public class Retryer extends FileDownloadListener {

        private BPDownloadManager downloadManager;
        private String currUrl;
        private Queue<String> mQueue;

        public Retryer(Queue<String> failedQueue){
            mQueue = failedQueue;
        }

        public void start(){
            cancel();
            go();
        }

        private void go(){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(mQueue == null || mQueue.size()<=0){
                onFinish();
                return;
            }
            currUrl = mQueue.poll();
            downloadManager = new BPDownloadManager(getClass(), this);
            downloadManager.downloadSingle(currUrl);
        }

        private void cancel(){
            if(downloadManager != null){
                downloadManager.cancel();
            }
        }

        @Override
        public void onStart(int currNum) {
            super.onStart(currNum);
            String name = currUrl.substring(currUrl.lastIndexOf("/")).substring(1);
            MainController.getInstance().updateInsertName("重试"+name);
        }

        @Override
        public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
            super.onError(e, currFileNum, totalNum, fileName);
            for (String fileUrl : urlList) {
                boolean contains = fileUrl.contains(fileName);
                if(contains){
                    faileQueue.offer(fileUrl);
                }
            }
            go();
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            MainController.getInstance().updateInsertPb(String.valueOf(progress));
        }

        @Override
        public void onSuccess(int currFileNum, int totalNum, String fileName) {
            super.onSuccess(currFileNum, totalNum, fileName);
            go();
        }

        @Override
        public void onFinish() {
            MainController.getInstance().closeInsertConsole();
            InsertResolver.instance().init();
        }
    }
}
