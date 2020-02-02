package com.yunbiao.cccm;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.yunbiao.cccm.net2.ConsoleDialog;
import com.yunbiao.cccm.net2.SystemVersion;
import com.yunbiao.cccm.net2.activity.MainActivity;
import com.yunbiao.cccm.net2.receiver.MyProtectService;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.net2.cache.CacheManager;
import com.yunbiao.cccm.net2.common.Const;
import com.yunbiao.cccm.xmpp.PnServerController;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    @Override
    protected int setLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        TextView tvMessage = findViewById(R.id.tv_message);
        TextView tvTips = findViewById(R.id.tv_tips);

        String type = CacheManager.SP.get(CacheManager.STORAGE_TYPE, Const.TYPE_SD_CARD + "");
        Const.STORAGE_TYPE = Integer.parseInt(type);

        tvTips.setText("请" + (Const.STORAGE_TYPE == Const.TYPE_ENVIRONMENT_STORAGE ? "" : Const.STORAGE_TYPE == Const.TYPE_SD_CARD ? "插入内存卡后" : "请插入U盘后") + "等待");
        tvMessage.setText("正在检测" + Const.getStorageType() + "，请稍等...");

        //初始化系统版本
        SystemVersion.initVersionTag();
    }

    @Override
    protected void initData() {

        //开启软件守护服务
        startService(new Intent(this, MyProtectService.class));

        //连接XMPP
        PnServerController.startXMPP(this);

        //开始检查SD卡
        SDChecker.instance().check(this, callback);
    }

    private SDChecker.Callback callback = new SDChecker.Callback() {
        @Override
        public void start() {
            Log.e(TAG, "start: 开始检测" + Const.getStorageType());
            ConsoleDialog.addTextLog("正在检测" + Const.getStorageType());
        }

        @Override
        public void waitSD() {
            Log.e(TAG, "waitSD: 等待SD卡");
            ConsoleDialog.addTextLog("等待插入" + Const.getStorageType());
        }

        @Override
        public void ready(String path) {
            Log.e(TAG, "ready: 准备就绪：" + path);
            ConsoleDialog.addTextLog(Const.getStorageType() + "已准备就绪");

            PathManager.savePath(path);
            PathManager.instance().initPath();

            int mode = CacheManager.SP.getMode();
            if(mode == 1){
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, com.yunbiao.cccm.yunbiaolocal.MainActivity.class));
            }
            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        SDChecker.instance().onActivityResult(requestCode, resultCode, data);
    }

}
