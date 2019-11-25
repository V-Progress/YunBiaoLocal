package com.yunbiao.cccm.net2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.activity.MainController;
import com.yunbiao.cccm.net2.common.HeartBeatClient;
import com.yunbiao.cccm.net2.common.ResourceConst;
import com.yunbiao.cccm.net2.control.actions.XBHActions;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.log.LogUtil;
import com.yunbiao.cccm.net2.model.InsertVideoModel;
import com.yunbiao.cccm.net2.utils.CommonUtils;
import com.yunbiao.cccm.net2.utils.NetUtil;
import com.yunbiao.cccm.net2.view.TipToast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertLoader {
    private static InsertLoader insertManager;
    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private List<Timer> timerList = new ArrayList<>();
    private final int TYPE_VIDEO = 1;
    private final Date todayDate;
    private final ScheduledExecutorService scheduledExecutorService;

    public static InsertLoader getInstance() {
        if (insertManager == null) {
            insertManager = new InsertLoader();
        }
        return insertManager;
    }

    public InsertLoader() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());
    }

    private void clearTimer() {
        d("清除定时任务：" + timerList.size());
        for (Timer timer : timerList) {
            timer.cancel();
        }
        timerList.clear();
    }

    private static final String TAG = "InsertLoader";

    private void d(String log) {
        Log.d(TAG, log);
    }

    //请求资源
    public void loadInsert() {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                InsertPlayer.getInstance().dismiss();
                clearTimer();
                d("停止播放");

                //获取数据
                Response request = request(ResourceConst.REMOTE_RES.INSERT_CONTENT, 3);
                if (request == null) {
                    d("请求失败");
                    return;
                }

                //取出数据
                String respStr = "";
                try {
                    respStr = request.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //为空则失败
                if (TextUtils.isEmpty(respStr)) {
                    d("请求失败");
                    return;
                }

                d(respStr);

                //解析数据
                InsertVideoModel insertVideo = new Gson().fromJson(respStr, InsertVideoModel.class);
                insertPlay(insertVideo);
            }
        });
    }

    /***
     * 插播类
     * ==================================================================================
     * @param ivm
     */
    private void insertPlay(InsertVideoModel ivm) {
        if (ivm == null) {
            d("解析为空");
            return;
        }

        //没有插播
        if (ivm.getResult() != 1) {
            d("没有插播数据");
            return;
        }

        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();

        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();
        if (insertArray == null || insertArray.size() <= 0) {
            d("没有插播数据");
            return;
        }

        Date today = new Date();
        for (InsertVideoModel.Data data : insertArray) {
            if (data == null) {
                continue;
            }
            final String content = data.getContent();
            String startTime = data.getStartTime();
            String endTime = data.getEndTime();
            Integer playType = data.getPlayType();
            Integer isCycle = data.getIsCycle();

            d("播放类型：" + playType);
            d("开始时间：" + startTime);
            d("停止时间：" + endTime);

            final Date[] dateArray = TimeResolver.resolve(startTime, endTime);
            if (dateArray == null) {
                continue;
            }

            if (today.after(dateArray[1])) {
                d("已过期");
                continue;
            }

            if (playType == TYPE_VIDEO) {
                handleVideo(isCycle == 1, content, ftpUrl, dateArray[0], dateArray[1]);
            } else if (playType == 2) {//直播源
                handleLive(content, ftpUrl, dateArray[0], dateArray[1]);
            } else {//切换信号
                handleHDMI(dateArray[0], dateArray[1]);
            }
        }
    }

    /***
     * 切换HDMI信号 todo 待测试
     * @param isHdmi
     */
    private void checkHDMI(boolean isHdmi) {
        Integer broadType = CommonUtils.getBroadType();
        if (broadType == 4) {//判断是不是小百合
            Intent intent = new Intent();
            intent.setAction(isHdmi ? XBHActions.CHANGE_TO_HDMI : XBHActions.CHANGE_TO_VGA);
            APP.getContext().sendBroadcast(intent);
        } else {
            TipToast.showLongToast(APP.getContext(), "本设备不支持该功能");
        }
    }

    private void handleVideo(final boolean isCycle, String content, String ftpUrl, final Date start, final Date end) {
        d("解析视频");
        final List<DownloadBean> playList = new ArrayList<>();
        String[] playArray = content.split(",");//取出播放列表
        //拼装播放列表
        for (String name : playArray) {
            if (!TextUtils.isEmpty(name)) {
                DownloadBean downloadBean = new DownloadBean();
                String[] split = name.split("/");

                downloadBean.url = ftpUrl + name;
                downloadBean.name = split[split.length - 1];
                playList.add(downloadBean);
            }
        }

        Queue<DownloadBean> urlQueue = new LinkedList<>();
        urlQueue.addAll(playList);

        checkFile(urlQueue, new Runnable() {
            @Override
            public void run() {
                final List<String> pathList = new ArrayList<>();
                for (DownloadBean downloadBean : playList) {
                    if (SystemVersion.isLowVer()) {
                        File resFileDir = PathManager.instance().getResFileDir();
                        File file = new File(resFileDir, downloadBean.name);
                        if (file != null && file.exists()) {
                            pathList.add(file.getPath());
                        }
                    } else {
                        DocumentFile file = PathManager.instance().getResDocFileDir().findFile(downloadBean.name);
                        if (file != null && file.exists()) {
                            pathList.add(file.getUri().toString());
                        }
                    }
                }

                d("已全部下载");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        d("开始插播，数据：" + playList.size());
                        InsertPlayer.getInstance().setPlayData(isCycle, pathList);
                    }
                }, start);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        d("停止插播");
                        InsertPlayer.getInstance().dismiss();
                    }
                }, end);
                timerList.add(timer);
            }
        });
    }

    private void handleLive(final String content, String ftpUrl, final Date start, final Date end) {
        d("解析直播源");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add(content);
                InsertPlayer.getInstance().setPlayData(true, list);
            }
        }, start);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InsertPlayer.getInstance().dismiss();
            }
        }, end);
        timerList.add(timer);

    }

    private void handleHDMI(Date start, Date end) {
        d("解析HDMI");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.E("切换到HDMI信号");
                checkHDMI(true);
            }
        }, start);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.E("结束HDMI信号");
                checkHDMI(false);
            }
        }, end);

        timerList.add(timer);
    }

    class DownloadBean {
        String url;
        String name;
    }

    private Downloader.MultiDownloadListener listener = new Downloader.MultiDownloadListener() {
        @Override
        public void onReadyProgram(String date, boolean hasProgram) {

        }

        @Override
        public void onStart(String date, int total) {

        }

        @Override
        public void onSingleStart(ItemBlock itemBlock, int index) {

        }

        @Override
        public void onProgress(int progress) {
            d("下载进度：" + progress);
        }

        @Override
        public void onFailed(ItemBlock itemBlock) {

        }

        @Override
        public void onComplete(ItemBlock itemBlock) {

        }

        @Override
        public void onFinished(String date) {

        }
    };

    private void checkFile(Queue<DownloadBean> urlQueue, @NonNull Runnable runnable) {
        if (urlQueue == null || urlQueue.size() <= 0) {
            runnable.run();
            return;
        }

        DownloadBean poll = urlQueue.poll();
        d("开始下载：" + poll.url);

        int result = -1;
        if (SystemVersion.isLowVer()) {
            File file = new File(PathManager.instance().getResFileDir(), poll.name);
            if (file != null && file.exists()) {
                d("文件存在，大小：" + file.length() + "，路径：" + file.getPath());
                checkFile(urlQueue, runnable);
                return;
            }
            result = Downloader.getInstance().download_l(poll.url, poll.name, listener);
        } else {
            DocumentFile file = PathManager.instance().getResDocFileDir().findFile(poll.name);
            if (file != null && file.exists()) {
                d("文件存在，大小：" + file.length() + "，路径：" + file.getUri().toString());
                checkFile(urlQueue, runnable);
                return;
            }

            Log.e(TAG, "checkFile: " + poll.url + " ----- " + poll.name);

            result = Downloader.getInstance().download_h(poll.url, poll.name, listener);
        }

        if (result != 1) {
            urlQueue.offer(poll);
            d("下载失败：" + result);
        }

        checkFile(urlQueue, runnable);
    }

    private Response request(String url, int time) {
        Map<String, String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        d("请求地址：" + url);
        d("请求参数：" + params.toString());
        Response resp = null;
        for (int i = 0; i < time; i++) {
            d("请求第 " + i + "次");
            Response response = NetUtil.getInstance().postSync(url, params);
            if (response != null) {
                resp = response;
                break;
            }
        }
        return resp;
    }
}
