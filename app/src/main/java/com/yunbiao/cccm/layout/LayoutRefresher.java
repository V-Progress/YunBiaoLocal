package com.yunbiao.cccm.layout;

import android.view.View;

/**
 * Created by Administrator on 2018/12/11.
 */

public class LayoutRefresher {
    private static LayoutRefresher layoutRefresher;
    private OnRefreshIner mOnRefreshIner;

    public static synchronized LayoutRefresher getInstance() {
        if (layoutRefresher == null) {
            layoutRefresher = new LayoutRefresher();
        }
        return layoutRefresher;
    }

    public void registerActivity(OnRefreshIner onRefreshIner) {
        if (onRefreshIner == null) {
            new Exception("onRefreshIner can not null!").printStackTrace();
            return;
        }
        mOnRefreshIner = onRefreshIner;
        mOnRefreshIner.layoutInit();
    }

    public void unRegisterActivity(){
        mOnRefreshIner = null;
    }

    public void addView(View view){
        if(view == null){
            new Exception("view can not be null!").printStackTrace();
            return;
        }
        if(mOnRefreshIner == null){
            return;
        }
        mOnRefreshIner.addView(view);
    }

    public void removeAllView(){
        if(mOnRefreshIner == null){
            return;
        }
        mOnRefreshIner.removeAllView();
    }

    public void refreshLayout(){
        removeAllView();
        LayoutDataHandle.getInstance().handleLayoutData();
    }

    public void removeView(View view){
        mOnRefreshIner.removeView(view);
    }

    public interface OnRefreshIner{
        void layoutInit();
        void addView(View view);
        void removeView(View view);
        void removeAllView();
    }

}
