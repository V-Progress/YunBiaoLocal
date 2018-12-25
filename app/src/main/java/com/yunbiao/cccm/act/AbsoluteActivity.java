package com.yunbiao.cccm.act;

import android.view.View;
import android.widget.AbsoluteLayout;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.layout.LayoutController;

public class AbsoluteActivity extends BaseActivity implements LayoutController.OnRefreshIner {

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
        LayoutController.getInstance().registerActivity(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LayoutController.getInstance().unRegisterActivity();
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

    @Override
    public void update() {

    }
}
