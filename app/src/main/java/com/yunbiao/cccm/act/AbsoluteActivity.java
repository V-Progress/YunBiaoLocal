package com.yunbiao.cccm.act;

import android.widget.AbsoluteLayout;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;

public class AbsoluteActivity extends BaseActivity{

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
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainController.getInstance().unRegisterActivity();
    }
}
