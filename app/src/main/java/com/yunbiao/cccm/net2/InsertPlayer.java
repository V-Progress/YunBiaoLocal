package com.yunbiao.cccm.net2;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import com.janev.easyijkplayer.EasyPlayer;
import com.yunbiao.cccm.R;

import java.util.List;

/**
 * Created by Administrator on 2019/11/25.
 */

public class InsertPlayer {
    private static InsertPlayer insertController = new InsertPlayer();
    private WindowManager windowManager;
    private EasyPlayer easyPlayer;
    private Activity mAct;
    private View inflate;

    public static InsertPlayer getInstance() {
        return insertController;
    }

    public void init(Activity activity) {
        mAct = activity;

        inflate = View.inflate(mAct, R.layout.layout_insert, null);
        easyPlayer = inflate.findViewById(R.id.easy_player);
        easyPlayer.setPlayCallback(new EasyPlayer.PlayCallback() {
            @Override
            public void playComplete() {
                dismiss();
            }
        });
        windowManager = mAct.getWindowManager();
    }

    private InsertCallback insertCallback;
    public interface InsertCallback{
        void onShow();
        void onHide();
    }

    public void setInsertCallback(InsertCallback insertCallback) {
        this.insertCallback = insertCallback;
    }

    private boolean isShown = false;

    public void setPlayData(final boolean isCycle, final List<String> playList){
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                easyPlayer.setCycle(isCycle);
                easyPlayer.setVideos(playList);
                show();
            }
        });
    }

    public void show() {
        if (SystemVersion.isInsertFirst()) {
            if (!isShown) {
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            windowManager.addView(inflate, getParams());
                        } catch (Exception e) {
                        }
                        isShown = true;
                        if(insertCallback != null){
                            insertCallback.onShow();
                        }
                    }
                });
            }
        }
    }

    public void dismiss() {
        if (isShown) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stop();
                    try {
                        windowManager.removeViewImmediate(inflate);
                    } catch (Exception e) {
                    }
                    isShown = false;
                    if(insertCallback != null){
                        insertCallback.onHide();
                    }
                }
            });
        }
    }

    public void resume() {
        if(SystemVersion.isInsertFirst()){
            show();
            easyPlayer.resume();
        }

    }

    public void pause() {
        dismiss();
        easyPlayer.pause();
    }

    public void stop() {
        easyPlayer.stop();
    }

    private WindowManager.LayoutParams getParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_TOAST;// 类型
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        return params;
    }

}
