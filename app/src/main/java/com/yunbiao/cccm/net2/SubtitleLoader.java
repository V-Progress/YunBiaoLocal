package com.yunbiao.cccm.net2;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;

import com.yunbiao.cccm.net2.cache.CacheManager;
import com.yunbiao.cccm.net2.model.InsertTextModel;
import com.yunbiao.cccm.net2.view.MyScrollTextView2;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2019/11/22.
 */

public class SubtitleLoader {
    private static SubtitleLoader subtitleLoader = new SubtitleLoader();
    private boolean hasSubtitle = false;
    private Activity mAct;
    private boolean isShown = false;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private MyScrollTextView2 scrollTextView2;
    private Timer timer;
    public static SubtitleLoader instance() {
        return subtitleLoader;
    }

    private SubtitleLoader() {
        isShown = false;
    }

    /**
     * 设置flag
     * FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
     * 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
     * 不设置这个flag的话，home页的划屏会有问题
     * | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
     * 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
     */
    public void init(Activity act) {
        mAct = act;
        scrollTextView2 = new MyScrollTextView2(mAct);
        windowManager = mAct.getWindowManager();

        InsertTextModel txtAds = CacheManager.FILE.getTXTAds();
        setTXT(txtAds);
    }

    private void show() {
        if (!isShown) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        windowManager.addView(scrollTextView2, params);
                    } catch (Exception e){}
                    isShown = true;
                }
            });
        }
    }

    private void dismiss() {
        if (isShown) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        windowManager.removeViewImmediate(scrollTextView2);
                    } catch (Exception e){}
                    isShown = false;
                }
            });
        }
    }

    public void onStop() {
        dismiss();
    }

    public void onResume() {
        if(hasSubtitle){
            show();
        }
    }

    /***
     * 显示滚动文字
     * @param insertTextModel
     */
    public void setTXT(final InsertTextModel insertTextModel) {
        dismiss();

        if (insertTextModel == null) {
            CacheManager.FILE.putTXTAds(null);
            hasSubtitle = false;
            return;
        }

        if (TextUtils.equals("2", insertTextModel.getPlayType())) {
            CacheManager.FILE.putTXTAds(null);
            return;
        }
        hasSubtitle = true;
        CacheManager.FILE.putTXTAds(insertTextModel);

        InsertTextModel.Content textDetail = insertTextModel.getContent();
        String text = insertTextModel.getText();
        int fontSize = textDetail.getFontSize();
        int backColor = Color.parseColor(textDetail.getBackground());
        int direction = Integer.valueOf(textDetail.getPlayType());
        int gravity = TextUtils.equals("0", textDetail.getLocation()) ? Gravity.TOP|Gravity.LEFT : Gravity.BOTTOM|Gravity.LEFT  ;

        scrollTextView2.setText(text);
        scrollTextView2.setBackgroundColor(backColor);
        scrollTextView2.setTextSize(fontSize);
        scrollTextView2.setDirection(direction == 0 ? 3 : (direction == 1 ? 0 : direction));
        scrollTextView2.setScrollSpeed(textDetail.getPlaySpeed());
        scrollTextView2.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色

        params = getParams();
        params.gravity = gravity;
        params.height = (int) (fontSize * 2.5);

        setTime(insertTextModel);
    }

    private void setTime(final InsertTextModel itm){
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

        if (dates != null && dates.length > 0) {
            if(timer != null){
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    show();
                }
            }, dates[0]);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dismiss();
                }
            }, dates[1]);
        }
    }

    private WindowManager.LayoutParams getParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;// 类型
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        return params;
    }
}
