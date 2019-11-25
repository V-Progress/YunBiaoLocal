package com.yunbiao.cccm.net2.activity.base;

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.umeng.analytics.MobclickAgent;
import com.yunbiao.cccm.APP;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/12/11.
 */

public abstract class BaseActivity extends FragmentActivity {

    public FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        APP.addActivity(this);
        setContentView(setLayout());
        ButterKnife.bind(this);
        mFragmentManager = getSupportFragmentManager();

        initView();
        initData();
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
