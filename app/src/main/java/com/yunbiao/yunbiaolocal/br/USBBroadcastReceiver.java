package com.yunbiao.yunbiaolocal.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.Const;
import com.yunbiao.yunbiaolocal.copy.CopyUtil;
import com.yunbiao.yunbiaolocal.copy.inter.copyFileListener;

public class USBBroadcastReceiver extends BroadcastReceiver implements copyFileListener {
    private String dataString;

    @Override
    public void onReceive(Context context, Intent intent) {
        dataString = intent.getDataString().substring(7);
        if (!dataString.matches("/mnt/usbhost\\d") && !dataString.matches("/storage/usbhost\\d")) {
            Toast.makeText(context, "请插入SD卡或者U盘" + dataString, Toast.LENGTH_SHORT).show();
//            EventMessage.sendMsg(Const.CONTROL_EVENT.INIT_PLAYER, null);
            return;
        }
        CopyUtil.getInstance().USB2Local(dataString, this);
    }

    @Override
    public void onCopyStart(String usbFilePath) {
        EventMessage.sendMsg(Const.CONTROL_EVENT.OPEN_CONSOLE, null);
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, usbFilePath);
    }

    @Override
    public void onFileCount(int count) {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "文件数量" + count);
        EventMessage.sendMsg(Const.CONTROL_EVENT.INIT_PROGRESS, String.valueOf(count));
    }

    @Override
    public void onNoFile() {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "U盘中没有云标目录");
        EventMessage.sendMsg(Const.CONTROL_EVENT.CLOSE_CONSOLE, null);
    }

    @Override
    public void onCopying(int i) {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_PROGRESS, String.valueOf(i));
    }

    @Override
    public void onCopyComplete() {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "复制完成");
    }

    @Override
    public void onFinish() {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "加载视频");
        EventMessage.sendMsg(Const.CONTROL_EVENT.INIT_PLAYER, null);
    }

    @Override
    public void onDeleteFile(String path) {
        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "删除：" + path);
    }

}
