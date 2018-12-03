package com.yunbiao.yunbiaolocal.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.Const;
import com.yunbiao.yunbiaolocal.act.MainActivity;
import com.yunbiao.yunbiaolocal.copy.CopyUtil;
import com.yunbiao.yunbiaolocal.copy.inter.copyFileListener;

public class USBBroadcastReceiver extends BroadcastReceiver implements copyFileListener {
    private String dataString;
    private MainActivity mainActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        mainActivity = APP.getMainActivity();
        dataString = intent.getDataString().substring(7);
        if (!dataString.matches("/mnt/usbhost\\d") && !dataString.matches("/storage/usbhost\\d")) {
            Toast.makeText(context, "请插入SD卡或者U盘" + dataString, Toast.LENGTH_SHORT).show();
            return;
        }
        CopyUtil.getInstance().USB2Local(dataString, this);
    }

    @Override
    public void onCopyStart(String usbFilePath) {
        mainActivity.openConsole();
        mainActivity.updateConsole(usbFilePath);
//        EventMessage.sendMsg(Const.CONTROL_EVENT.OPEN_CONSOLE, null);
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, usbFilePath);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.OPEN_CONSOLE,null);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE,usbFilePath);
    }

    @Override
    public void onFileCount(int count) {
        mainActivity.updateConsole("文件数量" + count);
        mainActivity.initProgress(count);
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "文件数量" + count);
//        EventMessage.sendMsg(Const.CONTROL_EVENT.INIT_PROGRESS, String.valueOf(count));
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE,"文件数量" + count);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.INIT_PROGRESS,String.valueOf(count));
    }

    @Override
    public void onNoFile() {
        mainActivity.updateConsole("U盘中没有云标目录");
        mainActivity.closeConsole();
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "U盘中没有云标目录");
//        EventMessage.sendMsg(Const.CONTROL_EVENT.CLOSE_CONSOLE, null);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE,"U盘中没有云标目录");
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.CLOSE_CONSOLE,null);
    }

    @Override
    public void onCopying(int i) {
        mainActivity.updateProgress(String.valueOf(i));
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_PROGRESS, String.valueOf(i));
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_PROGRESS,String.valueOf(i));
    }

    @Override
    public void onCopyComplete() {
        mainActivity.updateConsole("复制完成\n加载视频...");
        mainActivity.initVTMPlayer();
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "复制完成\n加载视频...");
//        EventMessage.sendMsg(Const.CONTROL_EVENT.INIT_PLAYER,null);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_PROGRESS,"复制完成");
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.INIT_PLAYER,null);
    }

    @Override
    public void onFinish() {
        mainActivity.closeConsole();
//        EventMessage.sendMsg(Const.CONTROL_EVENT.CLOSE_CONSOLE, null);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.CLOSE_CONSOLE,null);
    }

    @Override
    public void onDeleteFile(String path) {
        mainActivity.updateConsole("删除：" + path);
//        EventMessage.sendMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE, "删除：" + path);
//        MainActivity.sendControlMsg(Const.CONTROL_EVENT.UPDATE_CONSOLE,"删除：" + path);
    }

}
