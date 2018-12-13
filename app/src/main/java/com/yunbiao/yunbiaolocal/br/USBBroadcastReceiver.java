package com.yunbiao.yunbiaolocal.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.act.MainActivity;
import com.yunbiao.yunbiaolocal.utils.CopyUtil;
import com.yunbiao.yunbiaolocal.utils.copyFileListener;
import com.yunbiao.yunbiaolocal.utils.ThreadUtil;

public class USBBroadcastReceiver extends BroadcastReceiver implements copyFileListener {
    private String dataString;
    private MainActivity mainActivity;

    public USBBroadcastReceiver() {
        mainActivity = APP.getMainActivity();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        dataString = intent.getDataString().substring(7);
        if (!dataString.matches("/mnt/usbhost\\d") && !dataString.matches("/storage/usbhost\\d")) {
            Toast.makeText(context, "请插入SD卡或者U盘" + dataString, Toast.LENGTH_SHORT).show();
            return;
        }
        CopyUtil.getInstance().USB2Local(dataString, this);
    }

    @Override
    public void onCopyStart(final String usbFilePath) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.openConsole();
                mainActivity.updateConsole(usbFilePath);
            }
        });
    }

    @Override
    public void onFileCount(final int count) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateConsole("文件数量" + count);
                mainActivity.initProgress(count);
            }
        });
    }

    @Override
    public void onNoFile() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateConsole("U盘中没有云标目录");
                mainActivity.closeConsole();
            }
        });
    }

    @Override
    public void onCopying(final int i) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateProgress(i);
            }
        });
    }

    @Override
    public void onCopyComplete() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateConsole("复制完成\n加载视频...");
                mainActivity.initVTMPlayer();
            }
        });
    }

    @Override
    public void onFinish() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.closeConsole();
            }
        });
    }

    @Override
    public void onDeleteFile(final String path) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateConsole("删除：" + path);
            }
        });
    }

}
