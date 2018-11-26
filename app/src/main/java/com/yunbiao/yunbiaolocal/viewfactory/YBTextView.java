package com.yunbiao.yunbiaolocal.viewfactory;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;

/**
 * Created by Administrator on 2018/11/26.
 */

public class YBTextView extends TextView {
    LayoutInfo layoutInfo;
    WindowManager wm;
    public YBTextView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        super(context);
        this.layoutInfo = layoutInfo;
        this.wm = wm;
    }

    public YBTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public YBTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
    }
}
