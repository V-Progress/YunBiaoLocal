package com.yunbiao.cccm.act;

import android.view.View;

import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

/**
 * 主界面刷新监听
 * 主界面实现此接口，在ContentController中对其进行统一管理
 * 主资源下载的进度监听在MainActivity中实现
 * Created by Wei.Zhang on 2018/12/25.
 */

public interface MainRefreshListener {

    //开始播放
    void startPlay(String videoString);
    //停止播放
    void stopPlay();
    //初始化数据
    void initPlayData(boolean isRemote);
    //初始化播放器
    void initPlayer();

    //打开控制台
    void openConsole();
    //关闭控制台
    void closeConsole();
    //更新控制台
    void updateConsole(String msg);

    void initProgress(int max);
    void updateProgress(int progress);

    void removeView(View view);

    void addView(View view);
}
