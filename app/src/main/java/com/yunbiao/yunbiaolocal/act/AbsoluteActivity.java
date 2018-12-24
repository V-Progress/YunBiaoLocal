package com.yunbiao.yunbiaolocal.act;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AbsoluteLayout;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layout.LayoutRefresher;

import java.util.ArrayList;
import java.util.List;

public class AbsoluteActivity extends BaseActivity implements LayoutRefresher.OnRefreshIner {

    private AbsoluteLayout absLayout;

    @Override
    protected int setLayout() {
        APP.setAbsAct(this);
        return R.layout.activity_absolute;
    }

    @Override
    protected void initView() {
        absLayout = findViewById(R.id.abs_layout);
        //在布局刷新器中注册，完全由其来控制布局的刷新
        LayoutRefresher.getInstance().registerActivity(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LayoutRefresher.getInstance().unRegisterActivity();
    }

    @Override
    public void layoutInit() {
        // TODO: 2018/12/11 初始化头部和脚部
    }

    @Override
    public void addView(View view) {
        absLayout.addView(view);
    }

    @Override
    public void removeView(View view) {

    }

    @Override
    public void removeAllView() {
        absLayout.removeAllViews();
    }
}
