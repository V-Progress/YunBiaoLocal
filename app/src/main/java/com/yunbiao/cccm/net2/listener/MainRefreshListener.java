package com.yunbiao.cccm.net2.listener;

import java.util.List;

/**
 * 主界面刷新监听
 * 主界面实现此接口，在ContentController中对其进行统一管理
 * 主资源下载的进度监听在MainActivity中实现
 * Created by Wei.Zhang on 2018/12/25.
 */

public interface MainRefreshListener {

    //开始播放
    void startConfigPlay(List<String> videoString);
    //停止播放
    void stopConfigPlay();

    //刷新layerType
    void updateLayerType(boolean isConfigOnTop);
}
