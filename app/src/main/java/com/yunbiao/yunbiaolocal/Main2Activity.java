package com.yunbiao.yunbiaolocal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AbsoluteLayout;

import com.yunbiao.yunbiaolocal.layouthandle.LayoutProcessor;
import com.yunbiao.yunbiaolocal.viewfactory.ViewFactory;
import com.yunbiao.yunbiaolocal.viewfactory.bean.Container;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.TextDetail;

import java.util.ArrayList;

public class Main2Activity extends Activity {

    private AbsoluteLayout absoluteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("123","onCreate--------------Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        absoluteLayout = new AbsoluteLayout(this);
        setContentView(absoluteLayout);

        LayoutProcessor layoutProcessor = new LayoutProcessor();
        layoutProcessor.handleLayout();
    }

    private LayoutInfo getLayout(String id, String left, String top, String content){

        Container container = new Container();
        container.setHeight("50%");
        container.setWidth("50%");
        container.setLeft(left);
        container.setTop(top);

        TextDetail textDetail = new TextDetail();
        textDetail.setBackground("#ffffff");
        textDetail.setDataType(0);
        textDetail.setFontColor("#FF0000");
        textDetail.setFontFamily("1");
        textDetail.setFontSize(52);
        textDetail.setIsPlay(true);
        textDetail.setPlayTime(0.1f);
        textDetail.setPlayType("0");
        textDetail.setTextAlign("");

        LayoutInfo layoutInfo = new LayoutInfo();
        layoutInfo.setType(2);
        layoutInfo.setTextDetail(textDetail);
        layoutInfo.setContainer(container);
        layoutInfo.setId(id);
        layoutInfo.setContent(new String[]{content});
        return layoutInfo;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {//检测到菜单键点击事件
            startActivity(new Intent(this,MenuActivity.class));
            return false;
        }

        return false;
    }
}
