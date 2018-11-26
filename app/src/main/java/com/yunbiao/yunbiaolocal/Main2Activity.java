package com.yunbiao.yunbiaolocal;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.viewfactory.ViewFactory;
import com.yunbiao.yunbiaolocal.viewfactory.bean.Container;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.TextDetail;

public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        AbsoluteLayout absoluteLayout = new AbsoluteLayout(this);
        setContentView(absoluteLayout);
        LayoutInfo layoutInfo1 = getLayout("row1_col1", "0%", "0%", "我是第一。。。");
        LayoutInfo layoutInfo3 = getLayout("row2_col1", "0%", "66%", "我是第三。。。");
        LayoutInfo layoutInfo2 = getLayout("row1_col2", "50%", "0%", "我是第二。。。");
        LayoutInfo layoutInfo4 = getLayout("row2_col2", "50%", "50%", "我是第四。。。");

        absoluteLayout.addView(ViewFactory.createView(ViewFactory.VIEW_TEXT_SCROLL,this,layoutInfo1,getWindowManager()));
        absoluteLayout.addView(ViewFactory.createView(ViewFactory.VIEW_TEXT_SCROLL,this,layoutInfo2,getWindowManager()));
        absoluteLayout.addView(ViewFactory.createView(ViewFactory.VIEW_TEXT_SCROLL,this,layoutInfo3,getWindowManager()));
        absoluteLayout.addView(ViewFactory.createView(ViewFactory.VIEW_TEXT_SCROLL,this,layoutInfo4,getWindowManager()));
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

        startActivity(new Intent(this,MenuActivity.class));

        return false;
    }
}
