package com.yunbiao.cccm.act;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.TimerExecutor;
import com.yunbiao.cccm.view.MyScrollTextView;
import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018/12/26.
 */

public class InsertFragment extends Fragment implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {

    @BindView(R.id.mstv_insert_fragment)
    MyScrollTextView mstvInsertFragment;
    @BindView(R.id.vv_insert_fragment)
    VideoView vvInsertFragment;
    @BindView(R.id.fl_insert_root)
    FrameLayout flInsertRoot;
    Unbinder unbinder;

    private SimpleDateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    public static final String KEY_INSERT_TXT = "insertTxt";
    public static final String KEY_INSERT_VIDEO = "insertVideo";

    private InsertTextModel insertTxtModel;
    private InsertVideoModel insertVideoModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_insert, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        insertTxtModel = (InsertTextModel) arguments.getSerializable(KEY_INSERT_TXT);
        insertVideoModel = (InsertVideoModel) arguments.getSerializable(KEY_INSERT_VIDEO);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    //初始化控件
    private void initView() {
        vvInsertFragment.setOnCompletionListener(this);
        vvInsertFragment.setOnInfoListener(this);
        vvInsertFragment.setOnPreparedListener(this);
    }

    //页面返回的时候重置显示控件
    @Override
    public void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(0, 2000);
    }

    //页面被遮挡时暂停所有
    @Override
    public void onPause() {
        super.onPause();
        vvInsertFragment.pause();
        mstvInsertFragment.setVisibility(View.GONE);
    }

    //延迟启动广告
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updatePlayData(insertTxtModel, insertVideoModel);
        }
    };

    /***
     * 总处理方法
     * @param itm 文字插播数据
     * @param ivm 视频插播数据
     */
    public void updatePlayData(final InsertTextModel itm, InsertVideoModel ivm) {
        handleTXT(itm);
        handleVideo(ivm);
    }

    /*==========文字处理=================================================================*/
    private final String CLEAR_TXT = "2";

    /***
     * 处理文字插播
     * @param itm
     */
    public void handleTXT(final InsertTextModel itm) {
        if (itm == null) {
            return;
        }

        if (TextUtils.equals(CLEAR_TXT, itm.getPlayType())) {
            insertTxtModel = null;
            closeTXT();
            return;
        }

        insertTxtModel = itm;
        //取出内部的数据
        String playDate1 = itm.getContent().getPlayDate();
        String playCurTime1 = itm.getContent().getPlayCurTime();

        try {
            final Date[] dates1 = resolveTime(playDate1, playCurTime1);
            if (dates1 != null && dates1.length > 0) {
                TimerExecutor.getInstance().addInTimerQueue(dates1[0], new TimerExecutor.OnTimeOutListener() {
                    @Override
                    public void execute() {
                        showTXT(itm);
                    }
                });
                TimerExecutor.getInstance().addInTimerQueue(dates1[1], new TimerExecutor.OnTimeOutListener() {
                    @Override
                    public void execute() {
                        closeTXT();
                    }
                });
            } else {
                LogUtil.E("播放时间已过！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * 显示滚动文字
     * @param insertTextModel
     */
    private void showTXT(InsertTextModel insertTextModel) {
        InsertTextModel.Content textDetail = insertTextModel.getContent();
        Integer fontSize = textDetail.getFontSize();
        String text = insertTextModel.getText();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (fontSize * 2.5));
        if (TextUtils.equals("0", textDetail.getLocation())) {//顶部
            layoutParams.gravity = Gravity.TOP;
        } else {
            layoutParams.gravity = Gravity.BOTTOM;
        }

        mstvInsertFragment.setLayoutParams(layoutParams);
        mstvInsertFragment.setTextSize(fontSize);//字号
        mstvInsertFragment.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色
        mstvInsertFragment.setScrollSpeed(textDetail.getPlaySpeed());
        mstvInsertFragment.setDirection(Integer.valueOf(textDetail.getPlayType()));
        mstvInsertFragment.setBackColor(Color.parseColor(textDetail.getBackground()));//背景色
        mstvInsertFragment.setText(text);//内容

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            mstvInsertFragment.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            mstvInsertFragment.setDirection(0);
        }

        mstvInsertFragment.setVisibility(View.VISIBLE);
    }

    //关闭滚动文字（如果视频没有在播放，则关掉本页面）
    private void closeTXT() {
        mstvInsertFragment.setVisibility(View.GONE);
        if (vvInsertFragment.isShown() && vvInsertFragment.isPlaying()) {
            return;
        }
        MainController.getInstance().closeInsertPlay();
    }

    /*==========视频处理=================================================================*/

    /***
     * 处理视频插播
     * @param ivm
     */
    private void handleVideo(InsertVideoModel ivm) {
        if (ivm == null) {
            return;
        }

        insertVideoModel = ivm;
        try {
            String playDate = ivm.getPlayDate();
            String playCurTime = ivm.getPlayCurTime();
            final Date[] dates = resolveTime(playDate, playCurTime);
            if (dates != null && dates.length > 0) {
                final String fileUrl = ivm.getFileurl();
                setVideo(dates[0], dates[1], fileUrl);
            } else {
                LogUtil.E("播放时间已过！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 设置播放视频
     * @param startDate
     * @param endDate
     * @param fileurl
     */
    private void setVideo(final Date startDate, final Date endDate, String fileurl) {
        if (fileurl.endsWith(".avi") || fileurl.endsWith(".mp4") || fileurl.endsWith(".3gp")) {
            NetUtil.getInstance().downloadFile(fileurl, new NetUtil.OnDownLoadListener() {
                @Override
                public void onStart(String fileName) {
                    LogUtil.E("开始下载视频");
                }

                @Override
                public void onProgress(int progress) {
                    LogUtil.E("正在下载-" + progress + "%");
                }

                @Override
                public void onComplete(final File response) {

                    TimerExecutor.getInstance().addInTimerQueue(startDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {

                        }
                    });
                    TimerExecutor.getInstance().addInTimerQueue(endDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {
                            closeVideo();
                        }
                    });
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onError(Exception e) {
                    LogUtil.E("下载失败：" + e.getMessage());
                }
            });
        } else if ((fileurl.startsWith("http://") && fileurl.endsWith(".m3u8"))) {

        } else {

        }
    }

    /*
     * 显示视频
     * todo 暂未实现，数据不完整
     */
    private void showVideo() {
        vvInsertFragment.setVideoPath(ResourceConst.LOCAL_RES.RES_SAVE_PATH + "/爱奇艺陈伟霆代言人g180829001501.avi");
        vvInsertFragment.start();
        vvInsertFragment.setVisibility(View.VISIBLE);
        MainController.getInstance().stopPlay();
    }

    //关闭视频（如果滚动文字没有在播放则关掉本页面）
    private void closeVideo() {
        vvInsertFragment.stopPlayback();
        vvInsertFragment.setVisibility(View.GONE);
        if (mstvInsertFragment.isShown()) {
            return;
        }
        MainController.getInstance().closeInsertPlay();
    }

    /*==========视频监听=================================================================*/
    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
    }

    /*==========数据处理=================================================================*/
    //解析播放时间
    private Date[] resolveTime(String playDate, String playTime) throws Exception {
        if (!playDate.contains("-")) {
            throw new Exception("playDate formal error!");
        }
        if (!playTime.contains("-")) {
            throw new Exception("playTime formal error!");
        }
        //切割开始结束时间
        String[] dates = playDate.split("-");
        String[] times = playTime.split("-");
        //获取当年月日
        Date currDateTime = new Date(System.currentTimeMillis());
        String currDateStr = yyyyMMdd.format(currDateTime);
        //转换成date格式
        Date currDate = yyyyMMdd.parse(currDateStr);
        Date beginDate = yyyyMMdd.parse(dates[0]);
        Date endDate = yyyyMMdd.parse(dates[1]);
        //对比
        if (currDate.getTime() < beginDate.getTime() || currDate.getTime() > endDate.getTime()) {
            return null;
        }

        //修正时间字符串
        String sTime = currDateStr + correctTime(times[0]);
        String eTime = currDateStr + correctTime(times[1]);
        LogUtil.E("开始时间-----" + sTime);
        LogUtil.E("结束时间-----" + eTime);
        //转换成date格式
        final Date beginTime = yyyyMMddHH_mm.parse(sTime);
        final Date endTime = yyyyMMddHH_mm.parse(eTime);

        LogUtil.E("开始毫秒：" + beginTime.getTime());
        LogUtil.E("结束毫秒：" + endTime.getTime());
        if (endTime.getTime() < yyyyMMddHH_mm.parse(yyyyMMddHH_mm.format(currDateTime)).getTime()) {
            return null;
        }

        return new Date[]{beginTime, endTime};
    }

    //修正播放时间
    private String correctTime(String time) {
        String[] beginTimes = time.split(":");
        for (int i = 0; i < beginTimes.length; i++) {
            String temp = beginTimes[i];
            if (temp.length() <= 1) {
                temp = "0" + temp;
            }
            beginTimes[i] = temp;
        }
        return beginTimes[0] + ":" + beginTimes[1];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
