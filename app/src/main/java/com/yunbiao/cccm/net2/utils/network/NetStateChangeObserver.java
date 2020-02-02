package com.yunbiao.cccm.net2.utils.network;

/**
 * Created by Administrator on 2020/1/8.
 */

public interface NetStateChangeObserver {
    void onNetDisconnected();

    void onNetConnected(NetworkType networkType);
}