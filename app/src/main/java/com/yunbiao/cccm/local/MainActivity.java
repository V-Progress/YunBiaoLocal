package com.yunbiao.cccm.local;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.ConsoleDialog;
import com.yunbiao.cccm.net2.activity.MenuActivity;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;

/**
 * Created by Administrator on 2020/1/13.
 */

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    /*@BindView(R.id.ep)
    private EasyPlayer easyPlayer;*/

    private ConsoleDialog consoleDialog;

    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        consoleDialog = new ConsoleDialog(this);
    }

    @Override
    protected void initData() {

    }

    private long firstTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
//                easyPlayer.fastForward();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
//                easyPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER://按下中键
//                easyPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.e(TAG, "onKeyDown: 打开控制台");
                consoleDialog.show();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.e(TAG, "onKeyDown: 关闭控制台");
                consoleDialog.dismiss();
                break;
            case KeyEvent.KEYCODE_MENU:
                startActivity(new Intent(this, MenuActivity.class));
                return false;
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                } else {
                    APP.exit();
                }
                return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        easyPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        easyPlayer.pause();
    }
}
