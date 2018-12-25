package com.yunbiao.cccm.act;

import android.view.View;
import android.widget.AbsoluteLayout;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.layout.LayoutRefresher;

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
