package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AbsoluteLayout;

import com.yunbiao.yunbiaolocal.PnServerActivity;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layouthandle.LayoutRefresher;
import com.yunbiao.yunbiaolocal.xmpp.NotificationService;

public class Main2Activity extends Activity {

    public AbsoluteLayout absoluteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("123", "onCreate--------------Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        absoluteLayout = new AbsoluteLayout(this);
        setContentView(absoluteLayout);
        LayoutRefresher.getInstance().registerAct(this);//将本act注册


        new Thread(new Runnable() {
            @Override
            public void run() {
                PnServerActivity.startXMPP(Main2Activity.this);

            }
        }).start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {//检测到菜单键点击事件
            startActivity(new Intent(this, MenuActivity.class));
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }
}
