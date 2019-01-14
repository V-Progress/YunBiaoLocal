package com.yunbiao.cccm.act;

import android.content.Intent;
import android.view.View;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.utils.ThreadUtil;
import com.yunbiao.cccm.view.model.InsertTextModel;
import com.yunbiao.cccm.view.model.InsertVideoModel;

import java.util.List;

/**
 * 内容控制器
 * 统一管理主界面与显示有关的所有内容
 * Created by Administrator on 2018/12/11.
 */

public class MainController {
    private static MainController layoutRefresher;
    private MainRefreshListener mRefListener;

    public void updateMenu(final boolean isHasPlay){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                CacheManager.SP.putPlayTag(isHasPlay);

                MainActivity mainActivity = APP.getMainActivity();
                MenuActivity menuActivity = APP.getMenuActivity();

                if(mainActivity == null){
                    return;
                }

                if(mainActivity.isForeground()  && !isHasPlay){
                    mainActivity.startActivity(new Intent(mainActivity,MenuActivity.class));
                }else if(menuActivity != null && menuActivity.isForeground()){
                    menuActivity.updatePlayButton();
                }
            }
        });
    }

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

    public void startPlay(final List<String> videoString) {
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

    public void startInsert(final boolean isCycle, final List<String> videoString){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.startInsert(isCycle,videoString);
            }
        });
    }

    public void stopInsert(){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.stopInsert();
            }
        });
    }

    public void initPlayData(final boolean isRemote) {
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.initPlayData(isRemote);
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

    public void updateChildProgress(final int pg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateChildProgress(pg);
            }
        });
    }

    public void updateParentProgress(final int pg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.updateParentProgress(pg);
            }
        });
    }

    public void openLoading(final String loadingMsg){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.openLoading(loadingMsg);
            }
        });
    }

    public void closeLoading(){
        ThreadUtil.getInstance().runInUIThread(new Runnable() {
            @Override
            public void run() {
                mRefListener.closeLoading();
            }
        });
    }
}
