package com.yunbiao.yunbiaolocal.layout.view.web;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layout.bean.Center;
import com.yunbiao.yunbiaolocal.layout.bean.WebDetail;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class MyXWalkView {
    private static final String TAG = "MyXWalkView";
    private Context context;
    private Center mCenter;

    public MyXWalkView(Context context, Center center) {
        this.context = context;
        this.mCenter = center;

        initView();
        setView();
    }

    private View view;

    public View getView() {
        return view;
    }

    public static XWalkView mXWalkView;

    private void initView() {
        view = View.inflate(context, R.layout.my_xwalk_view, null);

        mXWalkView = view.findViewById(R.id.xWalkView);
    }

    private void setView() {
        final String url = mCenter.getContent()[0];

        mXWalkView.requestFocus();
        mXWalkView.setResourceClient(new MyResourceClient(mXWalkView));
        mXWalkView.setUIClient(new MyUIClient(mXWalkView));
        mXWalkView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//        mXWalkView.setDrawingCacheEnabled(false);//不使用缓存

        mXWalkView.load(url, null);


        //添加对javascript支持
        XWalkPreferences.setValue("enable-javascript", true);
        //开启调式,支持谷歌浏览器调式
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        //置是否允许通过file url加载的Javascript可以访问其他的源,包括其他的文件和http,https等其他的源
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        //JAVASCRIPT_CAN_OPEN_WINDOW
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
        // enable multiple windows.
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);

        final WebDetail webDetail = mCenter.getWebDetail();
        if (webDetail.getAutoFlus()) {// 如果网页需要刷新
            final int time = Integer.parseInt(webDetail.getFlusTime());

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mXWalkView.reload(0);
                    handler.postDelayed(this, time * 1000);
                }
            }, time * 1000);
        }
    }

    class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }
    }

    class MyUIClient extends XWalkUIClient {
        MyUIClient(XWalkView view) {
            super(view);
        }
    }


    public static void onDestory() {
        if (mXWalkView != null) {
            mXWalkView.onDestroy();
        }
    }
}
