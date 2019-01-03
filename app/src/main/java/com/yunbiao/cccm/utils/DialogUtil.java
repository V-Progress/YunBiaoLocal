package com.yunbiao.cccm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.resolve.VideoDataResolver;
import com.yunbiao.cccm.view.InsertPlayDialog;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.view.MyScrollTextView;
import com.yunbiao.cccm.view.model.InsertTextModel;

import android.widget.Toast;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/3.
 */

public class DialogUtil {

    public static final int INSERT_VIDEO = 0;
    public static final int INSERT_TEXT = 1;
    public static final int INSERT_LIVE = 2;

    private static InsertPlayDialog insertPlayDialog;
    private static AlertDialog playlistDialog;

    private static DialogUtil instance;
    private MyScrollTextView myScrollTextView;

    public static synchronized DialogUtil getInstance() {
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }


    /**
     * 插播广告Dialog
     *
     * @param content
     */
    private void showInsertDialog(Activity activity, String content) {
        insertPlayDialog = InsertPlayDialog.build(activity);
        insertPlayDialog.showInsert(content);
    }

    private MyScrollTextView showInsertScrTxt(Activity activity, InsertTextModel.Content textDetail, String content) {
        Integer fontSize = textDetail.getFontSize();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (fontSize * 2.5));
        layoutParams.gravity = Integer.valueOf(textDetail.getLocation());

        MyScrollTextView myScrollTextView = new MyScrollTextView(activity);
        myScrollTextView.setLayoutParams(layoutParams);
        myScrollTextView.setTextSize(fontSize);//字号
        myScrollTextView.setTextColor(Color.parseColor(textDetail.getFontColor()));//字体颜色
        myScrollTextView.setScrollSpeed(textDetail.getPlaySpeed());
        myScrollTextView.setDirection(Integer.valueOf(textDetail.getPlayType()));
        myScrollTextView.setBackColor(Color.parseColor(textDetail.getBackground()));//背景色
        myScrollTextView.setText(content);//内容

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            myScrollTextView.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            myScrollTextView.setDirection(0);
        }

        return myScrollTextView;
    }

    public void showProgressDialog(Activity activity, String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public static boolean isShowing() {
        return (insertPlayDialog != null && insertPlayDialog.isShowing()) || (playlistDialog != null && playlistDialog.isShowing());
    }

    public static Bitmap screenShot() {
        View decorView = null;
        if (insertPlayDialog != null && insertPlayDialog.isShowing()) {
//            decorView = insertPlayDialog.getWindow().getDecorView();
            return insertPlayDialog.screenShot();
        } else if (playlistDialog != null && playlistDialog.isShowing()) {
            decorView = playlistDialog.getWindow().getDecorView();
        }
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        return Bitmap.createBitmap(decorView.getDrawingCache());

    }

    private SimpleDateFormat yyyyMMddHH_mm = new SimpleDateFormat("yyyyMMddHH:mm");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

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


}
