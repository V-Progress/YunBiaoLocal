package com.yunbiao.cccm.net2.activity;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.event.HasDataEvent;
import com.yunbiao.cccm.net2.listener.MainRefreshListener;
import com.yunbiao.cccm.net2.utils.ConsoleUtil;
import com.yunbiao.cccm.net2.utils.ThreadUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 内容控制器
 * 统一管理主界面与显示有关的所有内容
 * Created by Administrator on 2018/12/11.
 */

public class MainController{
    private static MainController layoutRefresher;
    private MainRefreshListener mRefListener;

    public static synchronized MainController getInstance() {
        if (layoutRefresher == null) {
            layoutRefresher = new MainController();
        }
        return layoutRefresher;
    }

    /***
     * 注册监听到控制器，一般只能是MainActivity
     * @param refListener
     */
    public void registerActivity(MainRefreshListener refListener) {
        if (refListener == null) {
            new Exception("onRefreshIner can not null!").printStackTrace();
            return;
        }
        mRefListener = refListener;
    }

    /***
     * 设置是否有Insert资源的标签
     * @param hasInsert
     */
    public void setHasInsert(boolean hasInsert) {
        EventBus.getDefault().post(new HasDataEvent(hasInsert,1));
    }

    /***
     * 设置是否有config资源的标签
     * @param hasConfig
     */
    public void setHasConfig(boolean hasConfig) {
        EventBus.getDefault().post(new HasDataEvent(hasConfig,0));
    }

    /***
     * 更新设备编号和接入码
     */
    public void updateDeviceNo() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                MenuActivity menuActivity = APP.getMenuActivity();
                if(menuActivity != null && menuActivity.isForeground()){
                    menuActivity.updateDeviceNo();
                }
            }
        });
    }

    /***
     * 开始播放普通资源（）
     * @param videoString
     */
    public void startPlay(final List<String> videoString) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startConfigPlay(videoString);
            }
        });
    }

    /***
     * 停止播放普通资源
     */
    public void stopPlay() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.stopConfigPlay();
            }
        });
    }

    /***
     * 完全清除播放内容
     */
    public void clearPlayData(){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.clearPlayData();
            }
        });
    }

    /***
     * 开始播放插播
     * @param isCycle
     * @param videoString
     */
    public void startInsert(final boolean isCycle, final List<String> videoString, final boolean isAdd) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startInsert(isCycle, videoString,isAdd);
            }
        });
    }

    /***
     * 停止插播
     */
    public void stopInsert() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.stopInsert();
            }
        });
    }

    public void updateLayerType(final Integer layerType){
        if(mRefListener == null){
            return;
        }
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateLayerType(layerType == 2);
            }
        });
    }
}
