package com.yunbiao.cccm.net2;

import android.content.Intent;
import android.util.Log;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.net2.activity.MainActivity;
import com.yunbiao.cccm.net2.activity.MyProtectService;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.xmpp.PnServerController;

public class SplashActivity extends BaseActivity{
    private static final String TAG = "SplashActivity";

    @Override
    protected int setLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        //初始化系统版本
        SystemVersion.initVersionTag();

        //开启软件守护服务
        startService(new Intent(this, MyProtectService.class));

        //连接XMPP
        PnServerController.startXMPP(this);

        //开始检查SD卡
        SDChecker.instance().check(this,callback);
    }

    private SDChecker.Callback callback = new SDChecker.Callback() {
        @Override
        public void start() {
            Log.e(TAG, "start: 开始检测SD卡");
        }

        @Override
        public void waitSD() {
            Log.e(TAG, "waitSD: 等待SD卡");
        }

        @Override
        public void ready(String path) {
            Log.e(TAG, "ready: 准备就绪");

            PathManager.instance().initPath();

            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDChecker.instance().onActivityResult(requestCode, resultCode, data);
    }

}
