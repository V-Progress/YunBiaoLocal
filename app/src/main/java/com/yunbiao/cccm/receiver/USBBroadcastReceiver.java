package com.yunbiao.cccm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.utils.SDUtil;
import com.yunbiao.cccm.activity.MainController;
import com.yunbiao.cccm.utils.CopyUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.TimerUtil;
import com.yunbiao.cccm.utils.ToastUtil;
import com.yunbiao.cccm.net.listener.copyFileListener;

public class USBBroadcastReceiver extends BroadcastReceiver implements copyFileListener {
    private String tempAction;

    public USBBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String path = intent.getDataString();
        if (TextUtils.equals(action, tempAction)) {
            return;
        }
        tempAction = action;

        //判断插拔动作
        if (TextUtils.equals(Intent.ACTION_MEDIA_MOUNTED, action)) {//U盘插入
            if (TextUtils.isEmpty(path)) {
                return;
            }

            if (path.contains("file://")) {
                path = path.replace("file://", "");
            }

            //检测是U盘/SD卡
            if (SDUtil.isUSBDisk(path)) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    ToastUtil.showShort(context,"Android 5.0或以上版本暂不支持U盘拷贝");
                    return;
                }

                if (CacheManager.SP.getMode() == 0) {
                    LogUtil.E("网络模式下不响应U盘拷贝");
                    return;
                }

                ToastUtil.showShort(context,"U盘已插入" + path);
                CopyUtil.getInstance().USB2Local(path, this);
            } else if (SDUtil.isSDCard(path)) {
                ToastUtil.showShort(context, "SD卡已插入" + path);
                TimerUtil.delayExecute(3000, new TimerUtil.OnTimerListener() {
                    @Override
                    public void onTimeFinish() {
                        SDUtil.instance().checkSD();
                    }
                });
            }

        } else if (TextUtils.equals(Intent.ACTION_MEDIA_UNMOUNTED, action) || TextUtils.equals(Intent.ACTION_MEDIA_REMOVED, action)) {//3288移除只发这个
            if (SDUtil.isUSBDisk(path)) {
                ToastUtil.showShort(context, "U盘已移除");
            } else if (SDUtil.isSDCard(path)) {
                ToastUtil.showShort(context, "SD卡已移除");

                SDUtil.instance().checkSD();
            }
        }
    }

    @Override
    public void onCopyStart(final String usbFilePath) {
        MainController.getInstance().openConsole();
        MainController.getInstance().updateConsole(usbFilePath);
    }

    @Override
    public void onFileCount(final int count) {
        MainController.getInstance().updateConsole("文件数量" + count);
        MainController.getInstance().initProgress(count);
    }

    @Override
    public void onNoFile() {
        ToastUtil.showShort(APP.getMainActivity(), "U盘中没有yunbiao目录");
    }

    @Override
    public void onCopying(final int i) {
        MainController.getInstance().updateParentProgress(i);
    }

    @Override
    public void onCopyComplete() {
        MainController.getInstance().updateConsole("复制完成\n加载视频...");
        MainController.getInstance().initPlayData();
    }

    @Override
    public void onFinish() {
        MainController.getInstance().closeConsole();
    }

    @Override
    public void onDeleteFile(final String path) {
        MainController.getInstance().updateConsole("删除：" + path);
    }

}
