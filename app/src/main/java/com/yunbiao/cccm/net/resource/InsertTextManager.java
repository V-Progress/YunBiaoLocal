package com.yunbiao.cccm.net.resource;

import android.app.Activity;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.net.model.InsertTextModel;
import com.yunbiao.cccm.net.view.MyScrollTextView;
import com.yunbiao.cccm.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/3/1.
 */

public class InsertTextManager implements TextToSpeech.OnInitListener {
    private static Activity mActivity;
    private List<Timer> txtTimerList = new ArrayList<>();
    private final String CLEAR_TXT = "2"; //清除字幕
    private MyScrollTextView scrollText;
    private TextToSpeech tts;
    private static InsertTextManager instance;

    public static InsertTextManager instance(Activity activity){
        mActivity = activity;
        if(instance == null){
            synchronized(InsertTextManager.class){
                if(instance == null){
                    instance = new InsertTextManager();
                }
            }
        }
        return instance;
    }

    private InsertTextManager(){
        tts = new TextToSpeech(mActivity, this);
    }

    /***
     * 插播字幕
     * ========================================================================================
     */
    public void initTXT() {
        InsertTextModel txtAds = CacheManager.FILE.getTXTAds();
        if (txtAds != null) {
            insertTXT(txtAds);
        }
    }

    public void insertTXT(final InsertTextModel itm) {
        if (itm == null) {
            return;
        }

        for (Timer timer : txtTimerList) {
            timer.cancel();
        }
        txtTimerList.clear();

        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                APP.getMainActivity().removeView(scrollText);
            }
        });

        if (TextUtils.equals(CLEAR_TXT, itm.getPlayType())) {
            CacheManager.FILE.putTXTAds(null);
            return;
        }

        MainController.getInstance().setHasInsert(true);
        CacheManager.FILE.putTXTAds(itm);

        //取出内部的数据
        String playDate = itm.getContent().getPlayDate();
        String playCurTime = itm.getContent().getPlayCurTime();
        String playTime = itm.getContent().getPlayTime();

        Date[] dates;
        //判断是播放时长还是播放时间段
        if (!TextUtils.isEmpty(playTime) && !TextUtils.equals("0", playTime)) {
            dates = TimeResolver.resolveTimeLong(playDate, playTime);
        } else {
            dates = TimeResolver.resolveTime(playDate, playCurTime);
        }

        if (dates == null) {
            return;
        }

        if (dates != null && dates.length > 0) {
            Timer startTimer = new Timer();
            Timer endTimer = new Timer();

            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            setTXT(itm);
                            MainController.getInstance().addView(scrollText);
                        }
                    });
                }
            }, dates[0]);

            endTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadUtil.getInstance().runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            MainController.getInstance().removeView(scrollText);
                        }
                    });
                }
            }, dates[1]);

            txtTimerList.add(startTimer);
            txtTimerList.add(endTimer);
        }
    }


    /***
     * 显示滚动文字
     * @param insertTextModel
     */
    private void setTXT(final InsertTextModel insertTextModel) {
        InsertTextModel.Content textDetail = insertTextModel.getContent();
        Integer fontSize = textDetail.getFontSize();
        String text = insertTextModel.getText();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (fontSize * 2.5));
        if (TextUtils.equals("0", textDetail.getLocation())) {//顶部
            layoutParams.gravity = Gravity.TOP;
        } else {
            layoutParams.gravity = Gravity.BOTTOM;
        }
        int backColor = Color.parseColor(textDetail.getBackground());
        scrollText = new MyScrollTextView(mActivity);

        scrollText.setLayoutParams(layoutParams);
        scrollText.setTextSize(fontSize);//字号
        scrollText.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色
        scrollText.setScrollSpeed(textDetail.getPlaySpeed());
        scrollText.setDirection(Integer.valueOf(textDetail.getPlayType()));
        scrollText.setBackColor(backColor);//背景色
        scrollText.setText(text);//内容

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            scrollText.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            scrollText.setDirection(0);
        }

        if (isSupportChinese) {
            if (TextUtils.equals("-1", textDetail.getSpeechCount())) {
                return;
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Toast.makeText(mActivity, "暂不支持语音报读", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSupportChinese = false;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if(tts != null){
                int result = tts.setLanguage(Locale.CHINA);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isSupportChinese = false;
                    return;
                }
                isSupportChinese = true;
            }
        }
    }
}
