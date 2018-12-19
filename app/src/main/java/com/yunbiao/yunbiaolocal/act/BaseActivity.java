package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/11.
 */

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        APP.addActivity(this);
        setContentView(setLayout());
        ButterKnife.bind(this);

        initView();
        initData();
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(null != BaseActivity.this.getCurrentFocus()){
                /**
                 * 点击空白位置 隐藏软键盘
                 */
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                return mInputMethodManager.hideSoftInputFromWindow(BaseActivity.this.getCurrentFocus().getWindowToken(), 0);
            }
            return false;
        }
    };

    protected abstract void initData();

    protected abstract void initView();

    protected abstract int setLayout();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        APP.removeActivity(this);
    }

    /**
     * 判断某个界面是否在前台
     *
     * @return 是否在前台显示
     */
    public boolean isForeground() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getShortClassName().contains(this.getClass().getSimpleName())) { // 说明它已经启动了
                return true;
            }
        }
        return false;
    }
}
