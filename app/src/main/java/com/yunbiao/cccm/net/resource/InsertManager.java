package com.yunbiao.cccm.net.resource;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.net.control.actions.XBHActions;
import com.yunbiao.cccm.net.listener.FileDownloadListener;
import com.yunbiao.cccm.sdOperator.HighVerSDOperator;
import com.yunbiao.cccm.utils.CommonUtils;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.net.view.MyScrollTextView;
import com.yunbiao.cccm.net.view.TipToast;
import com.yunbiao.cccm.net.resource.model.InsertTextModel;
import com.yunbiao.cccm.net.resource.model.InsertVideoModel;
import com.yunbiao.cccm.net.download.BPDownloadManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/29.
 */

public class InsertManager {
    private String TAG = this.getClass().getSimpleName();

    private static InsertManager insertManager;
    private static Activity mActivity;
    private SimpleDateFormat yyyyMMddHH_mm_ss = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    private BPDownloadManager bpDownloadManager;

    private final int TYPE_VIDEO = 1;
    private final int TYPE_LIVE = 2;
    private final int TYPE_INPUT = 3;
    private final Date todayDate;

    private List<String> playList = new ArrayList<>();
    private List<Timer> timerList = new ArrayList<>();

    public static InsertManager getInstance(Activity activity) {
        mActivity = activity;
        if (insertManager == null) {
            insertManager = new InsertManager();
        }
        return insertManager;
    }

    public InsertManager() {
        //获取当年月日
        todayDate = new Date(System.currentTimeMillis());
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
    public void insertPlay(InsertVideoModel ivm) {
        if (ivm == null) {
            return;
        }

        InsertVideoModel.InsertData dateJson = ivm.getDateJson();
        String ftpUrl = dateJson.getHsdresourceUrl();
        List<InsertVideoModel.Data> insertArray = dateJson.getInsertArray();

        playList.clear();
        clearTimer();
        MainController.getInstance().stopInsert();

        if (insertArray == null || insertArray.size() <= 0) {
            MainController.getInstance().setHasInsert(false);
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

            Integer playType = data.getPlayType();
            if (playType == null) {
                continue;
            }

            Date[] dateArray = TimeResolver.resolve(data.getStartTime(), data.getEndTime());
            if (dateArray == null) {
                continue;
            }

            if (today.after(dateArray[1])) {
                continue;
            }

            //是否轮播
            boolean isCycle = data.getIsCycle() == 1;
            switch (playType) {
                case TYPE_VIDEO:
                    String content = data.getContent();
                    String[] playArray = content.split(",");

                    List<String> urlList = new ArrayList<>();
                    List<String> playList = new ArrayList<>();
                    //拼装播放列表
                    for (String s : playArray) {
                        String[] split = s.split("/");
                        // TODO: 2019/2/25
//                        File insertVideo = LowVerSDOperator.instance().findResource(split[split.length - 1]);
//                        playList.add(insertVideo.getPath());
                        playList.add(split[split.length - 1]);
                        urlList.add(ftpUrl + s);
                    }
                    download(isCycle, urlList, playList, dateArray);
                    break;
                case TYPE_LIVE://直播类
                    String liveUrl = data.getContent();
                    handleLive(isCycle, liveUrl, dateArray[0], dateArray[1]);
                    break;
                case TYPE_INPUT:
                    handleInput(dateArray[0], dateArray[1]);
                    break;
            }
        }
    }

    /*=======视频处理流程================================================================*/
    public void download(final boolean isCycle, final List<String> urlList, final List<String> playList, final Date[] dateArray) {
        if(bpDownloadManager != null){
            bpDownloadManager.cancel();
        }
        bpDownloadManager = new BPDownloadManager(getClass(),new FileDownloadListener() {
            @Override
            public void onBefore(int totalNum) {
                MainController.getInstance().openLoading("插播资源下载");
            }

            @Override
            public void onStart(int currNum) {
                LogUtil.D(TAG, "开始下载");
            }

            @Override
            public void onProgress(int progress) {
                LogUtil.D(TAG, "进度：" + progress);
            }

            @Override
            public void onError(Exception e, int currFileNum, int totalNum, String fileName) {
                LogUtil.D(TAG, "下载出错：" + e.getMessage());
            }

            @Override
            public void onCancel() {
                MainController.getInstance().closeLoading();
            }

            @Override
            public void onFinish() {
                MainController.getInstance().closeLoading();

                List<String> uriList = new ArrayList<>();

                for (int i = 0; i < playList.size(); i++) {
                    String playName = playList.get(i);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        DocumentFile insertResource = HighVerSDOperator.instance().findResource(playName);
                        if(insertResource != null && insertResource.exists()){
                            uriList.add(insertResource.getUri().toString());
                        }
                    } else {
                        File file = new File(playList.get(i));
                        if(file.exists()){
                            uriList.add(Uri.fromFile(file).toString());
                        }
                    }
                }

                if (uriList.size() <= 0) {
                    return;
                }
                handleVideo(isCycle, uriList, dateArray[0], dateArray[1]);
            }

        });
        bpDownloadManager.startDownload(urlList);
    }

    //处理视频
    private void handleVideo(final boolean isCycle, final List<String> playList, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, playList);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }

    //处理直播数据
    private void handleLive(final boolean isCycle, final String liveUrl, Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                list.add(liveUrl);
                MainController.getInstance().setHasInsert(true);
                MainController.getInstance().startInsert(isCycle, list);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                MainController.getInstance().stopInsert();
            }
        });
    }

    //处理插播信号源流程
    private void handleInput(Date startTime, Date endTime) {
        timeExecute(startTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E(TAG, "切换到HDMI信号");
                MainController.getInstance().setHasInsert(true);
                checkHDMI(true);
            }
        }, endTime, new TimerTask() {
            @Override
            public void run() {
                LogUtil.E(TAG, "结束HDMI信号");
                checkHDMI(false);
            }
        });
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

    /***定时执行开关任务
     * ==============================================================================
     */
    private void timeExecute(Date startTime, final TimerTask startTask, Date endTime, final TimerTask endTask) {
        Timer startTimer = new Timer();
        Timer endTimer = new Timer();

        startTimer.schedule(startTask, startTime);
        endTimer.schedule(endTask, endTime);

        timerList.add(startTimer);
        timerList.add(endTimer);
    }

    public void clearTimer() {
        if (timerList.size() > 0) {
            for (Timer timer : timerList) {
                timer.cancel();
            }
            timerList.clear();
        }
    }
}
