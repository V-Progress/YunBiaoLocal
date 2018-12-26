package com.yunbiao.cccm.act;

import com.yunbiao.cccm.utils.ThreadUtil;

/**
 * 内容控制器
 * 统一管理主界面与显示有关的所有内容
 * Created by Administrator on 2018/12/11.
 */

public class MainController {
    private static MainController layoutRefresher;
    private MainRefreshListener mRefListener;

    public static synchronized MainController getInstance() {
        if (layoutRefresher == null) {
            layoutRefresher = new MainController();
        }
        return layoutRefresher;
    }

    public void registerActivity(MainRefreshListener refListener) {
        if (refListener == null) {
            new Exception("onRefreshIner can not null!").printStackTrace();
            return;
        }
        mRefListener = refListener;
    }

    public void unRegisterActivity() {
        mRefListener = null;
    }


    public void startPlay(final String videoString) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startPlay(videoString);
            }
        });
    }

    public void stopPlay() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.stopPlay();
            }
        });
    }

    public void initPlayData() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initPlayData();
            }
        });

    }

    public void initPlayer() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initPlayer();
            }
        });

    }

    public void openConsole() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.openConsole();
            }
        });

    }

    public void closeConsole() {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.closeConsole();
            }
        });

    }

    public void updateConsole(final String msg) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateConsole(msg);
            }
        });

    }

    public void initProgress(final int max){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initProgress(max);
            }
        });

    }

    public void updateProgress(final int progress){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateProgress(progress);
            }
        });

    }

    public void insertPlay() {
        mRefListener.insertPlay();
    }

    public void closeInsertPlay() {
        mRefListener.closeInsertPlay();
    }

    public void noRemoteFile() {

    }
}
