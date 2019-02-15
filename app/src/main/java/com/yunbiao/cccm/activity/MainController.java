package com.yunbiao.cccm.activity;

import android.content.Intent;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.common.cache.CacheManager;
import com.yunbiao.cccm.net.listener.MainRefreshListener;
import com.yunbiao.cccm.common.utils.LogUtil;
import com.yunbiao.cccm.common.utils.ThreadUtil;

import java.util.List;

/**
 * 内容控制器
 * 统一管理主界面与显示有关的所有内容
 * Created by Administrator on 2018/12/11.
 */

public class MainController {
    private static MainController layoutRefresher;
    private MainRefreshListener mRefListener;

    private boolean hasInsert = false;
    private boolean hasConfig = false;

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
        this.hasInsert = hasInsert;
        updateMenu(hasConfig || hasInsert);
    }

    /***
     * 设置是否有config资源的标签
     * @param hasConfig
     */
    public void setHasConfig(boolean hasConfig) {
        this.hasConfig = hasConfig;
        updateMenu(hasConfig || hasInsert);
    }

    /***
     * 更新菜单界面的播放按钮
     * @param isHasPlay
     */
    private void updateMenu(final boolean isHasPlay) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {

                CacheManager.SP.putPlayTag(isHasPlay);

                MainActivity mainActivity = APP.getMainActivity();
                MenuActivity menuActivity = APP.getMenuActivity();

                if (mainActivity == null) {
                    return;
                }

                LogUtil.E("123","更新菜单："+isHasPlay +"---"+hasInsert +"---"+hasConfig);

                if (mainActivity.isForeground() && !isHasPlay ) {
                    mainActivity.startActivity(new Intent(mainActivity, MenuActivity.class));
                } else if (menuActivity != null && menuActivity.isForeground()) {
                    menuActivity.updatePlayButton();
                }
            }
        });
    }

    /***
     * 更新播放列表
     */
    public void updateList() {
        final MenuActivity menuActivity = APP.getMenuActivity();
        if (menuActivity != null) {
            ThreadUtil.getInstance().runInUIThread(new Runnable() {
                @Override
                public void run() {
                    menuActivity.updatePlayList();
                }
            });
        }
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
     * 开始播放普通资源
     * @param videoString
     */
    public void startPlay(final List<String> videoString) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startPlay(videoString);
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
                mRefListener.stopPlay();
            }
        });
    }

    /***
     * 开始播放插播
     * @param isCycle
     * @param videoString
     */
    public void startInsert(final boolean isCycle, final List<String> videoString) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startInsert(isCycle, videoString);
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

    /***
     * 初始化播放数据
     */
    public void initPlayData() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initPlayData();
            }
        });

    }

    public void updateLayerType(final Integer layerType){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateLayerType(layerType);
            }
        });
    }

    /***
     * 初始化播放器
     */
    public void initPlayer() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initPlayer();
            }
        });
    }

    /***
     * 打开控制台进度条
     */
    public void openConsole() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.openConsole();
            }
        });
    }

    /***
     * 关闭控制台
     */
    public void closeConsole() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.closeConsole();
            }
        });

    }

    /***
     * 更新控制台文字
     * @param msg
     */
    public void updateConsole(final String msg) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateConsole(msg);
            }
        });

    }

    /***
     * 初始化进度条
     * @param max
     */
    public void initProgress(final int max) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initProgress(max);
            }
        });

    }

    /***
     * 更新子进度条，用作进度展示，默认max值为100
     * @param pg
     */
    public void updateChildProgress(final int pg) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateChildProgress(pg);
            }
        });
    }

    /***
     * 更新父进度条，用作范围展示
     * @param pg
     */
    public void updateParentProgress(final int pg) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateParentProgress(pg);
            }
        });
    }

    /***
     * 打开加载框，下载Insert资源时会用
     * @param loadingMsg
     */
    public void openLoading(final String loadingMsg) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.openLoading(loadingMsg);
            }
        });
    }

    /***
     * 关闭加载框
     */
    public void closeLoading() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.closeLoading();
            }
        });
    }

    public void updateSpeed(final String speed) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateDownloadSpeed(speed);
            }
        });
    }
}
