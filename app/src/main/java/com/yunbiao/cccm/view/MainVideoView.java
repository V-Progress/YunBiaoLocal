package com.yunbiao.cccm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.vov.vitamio.widget.VideoView;

/**
 * Created by Administrator on 2018/12/4.
 */

public class MainVideoView extends VideoView {
    public MainVideoView(Context context) {
        super(context);
    }

    public MainVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
