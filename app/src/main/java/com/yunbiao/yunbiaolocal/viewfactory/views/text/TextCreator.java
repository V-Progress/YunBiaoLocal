package com.yunbiao.yunbiaolocal.viewfactory.views.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutPosition;
import com.yunbiao.yunbiaolocal.viewfactory.bean.TextDetail;
import com.yunbiao.yunbiaolocal.viewfactory.tool.LayoutJsonTool;

/**
 * Created by Administrator on 2018/11/28.
 */

public class TextCreator {

    private static String[] fonts = new String[]{"song.ttf", "kai.ttf"};

    /**
     * 创建TextView
     */
    @SuppressLint("RtlHardcoded")
    public static TextView createTextView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        TextView textView = new TextView(context);
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(),lp.getHeight(),lp.getLeft(),lp.getTop());

        TextDetail textDetail = layoutInfo.getTextDetail();
        textView.setTextSize(textDetail.getFontSize());

        StringBuilder sb = new StringBuilder("");
        String[] content = layoutInfo.getContent();
        for (String aContent : content) {
            sb.append(aContent).append("  ");
        }
        textView.setText(sb.toString());

        String textAlign = textDetail.getTextAlign();
        if (TextUtils.isEmpty(textAlign) || textAlign.equals("center")) {
            textView.setGravity(Gravity.CENTER);
        } else if (textAlign.equals("left")) {
            textView.setGravity(Gravity.LEFT);
        } else if (textAlign.equals("right")) {
            textView.setGravity(Gravity.RIGHT);
        }

        textView.setLayoutParams(layoutParams);
        try {
            textView.setTextColor(Color.parseColor(textDetail.getFontColor()));
        } catch (NumberFormatException e) {
            textView.setTextColor(Color.parseColor("#000000"));
        }
        try {
            textView.setBackgroundColor(Color.parseColor(textDetail.getBackground()));
        } catch (NumberFormatException e) {
            textView.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        setTextFont(context,textView,textDetail.getFontFamily());

        return textView;
    }

    /**
     * 创建滚动文本
     */
    public static LinearLayout createScrollText(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(), lp.getTop());

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setPadding(0, 0, 0, 0);

        TextDetail textDetail = layoutInfo.getTextDetail();

        final MyScrollTextView scrollTv = new MyScrollTextView(context);
        scrollTv.setTextSize(textDetail.getFontSize());
        scrollTv.setTextColor(Color.parseColor(textDetail.getFontColor()));

        scrollTv.setLayoutParams(layoutParams);
        scrollTv.setTextFont(context, textDetail.getFontFamily());

        //判断处理文本内容
        StringBuilder scrollSb = new StringBuilder("");
        String[] content = layoutInfo.getContent();
        for (String aContent : content) {
            scrollSb.append(aContent).append("  ");
        }
        scrollTv.setText(scrollSb.toString());

        scrollTv.setScrollSpeed(textDetail.getPlayTime());

        if (Integer.parseInt(textDetail.getPlayType()) == 0) {
            scrollTv.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
        } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
            scrollTv.setDirection(0);
        }

        scrollTv.setBackColor(Color.parseColor(textDetail.getBackground()));

        linearLayout.addView(scrollTv);
        return linearLayout;
    }


    //
    /**
     * 设置文本字体
     */
    public static void setTextFont(Context context, TextView textView, String fontFamily) {
        if (!isNumeric(fontFamily)) {
            return;
        }
        int index = Integer.parseInt(fontFamily);
        if (index == 4 || index == 5) {
            index = 1;
        }
        if (index != 1) {
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fonts[index - 2]);
            textView.setTypeface(typeFace);
        }
    }

    private static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
