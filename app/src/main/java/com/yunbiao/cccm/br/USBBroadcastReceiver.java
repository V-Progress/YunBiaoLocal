package com.yunbiao.cccm.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.act.MainActivity;
import com.yunbiao.cccm.act.MainController;
import com.yunbiao.cccm.utils.CopyUtil;
import com.yunbiao.cccm.utils.copyFileListener;
import com.yunbiao.cccm.utils.ThreadUtil;

public class USBBroadcastReceiver extends BroadcastReceiver implements copyFileListener {
    private String dataString;

    public USBBroadcastReceiver() {
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
                MainController.getInstance().openConsole();
                MainController.getInstance().updateConsole(usbFilePath);
            }
        });
    }

    @Override
    public void onFileCount(final int count) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().updateConsole("文件数量" + count);
                MainController.getInstance().initProgress(count);
            }
        });
    }

    @Override
    public void onNoFile() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().updateConsole("U盘中没有云标目录");
                MainController.getInstance().closeConsole();
            }
        });
    }

    @Override
    public void onCopying(final int i) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().updateProgress(i);
            }
        });
    }

    @Override
    public void onCopyComplete() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().updateConsole("复制完成\n加载视频...");
                MainController.getInstance().initPlayer();
                MainController.getInstance().initPlayData(false);
            }
        });
    }

    @Override
    public void onFinish() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().closeConsole();
            }
        });
    }

    @Override
    public void onDeleteFile(final String path) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MainController.getInstance().updateConsole("删除：" + path);
            }
        });
    }

}
