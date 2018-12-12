package com.yunbiao.yunbiaolocal.layout.view.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layout.bean.Center;
import com.yunbiao.yunbiaolocal.layout.bean.WebDetail;

/**
 * Created by Administrator on 2017/9/18.
 */

public class MyWebView {
    private static final String TAG = "MyWebView";
    private Context context;
    private Center mCenter;

    public MyWebView(Context context, Center center) {
        this.context = context;
        this.mCenter = center;

        initView();
        setView();
    }

    private View view;

    public View getView() {
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private static WebView mWebView;

    private void initView() {
        view = View.inflate(context, R.layout.my_web_view, null);

        mWebView = (WebView) view.findViewById(R.id.WebView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setView() {
        final String url = mCenter.getContent()[0];

        mWebView.requestFocus();
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new TYWebViewClient());

        final WebDetail webDetail = mCenter.getWebDetail();
        if (webDetail.getAutoFlus()) {// 如果网页需要刷新
            final int time = Integer.parseInt(webDetail.getFlusTime());

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebView.reload();
                    handler.postDelayed(this, time * 1000);
                }
            }, time * 1000);
        }
    }

    public static void onDestory() {
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView = null;
        }
    }
}
