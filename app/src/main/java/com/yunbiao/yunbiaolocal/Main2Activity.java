package com.yunbiao.yunbiaolocal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.viewfactory.ViewFactory;
import com.yunbiao.yunbiaolocal.viewfactory.bean.Container;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.TextDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main2Activity extends Activity {

    private AbsoluteLayout absoluteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("123","onCreate--------------Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        absoluteLayout = new AbsoluteLayout(this);
        setContentView(absoluteLayout);
        LayoutInfo layoutInfo1 = getLayout("row1_col1", "0%", "0%", "我是第一。。。");
        LayoutInfo layoutInfo3 = getLayout("row2_col1", "0%", "50%", "我是第三。。。");
        LayoutInfo layoutInfo2 = getLayout("row1_col2", "50%", "0%", "我是第二。。。");
        LayoutInfo layoutInfo4 = getLayout("row2_col2", "50%", "50%", "我是第四。。。");

        LayoutInfo[] layoutInfos = {layoutInfo1,layoutInfo2,layoutInfo3,layoutInfo4};
        ArrayList<LayoutInfo> liList = new ArrayList<>();
        liList.add(layoutInfo1);
        liList.add(layoutInfo2);
        liList.add(layoutInfo3);
        liList.add(layoutInfo4);

        absoluteLayout.addView(ViewFactory.createScrollText(Main2Activity.this,layoutInfo1,Main2Activity.this.getWindowManager()));
        absoluteLayout.addView(ViewFactory.createScrollText(Main2Activity.this,layoutInfo2,Main2Activity.this.getWindowManager()));
        absoluteLayout.addView(ViewFactory.createScrollText(Main2Activity.this,layoutInfo3,Main2Activity.this.getWindowManager()));
        absoluteLayout.addView(ViewFactory.createScrollText(Main2Activity.this,layoutInfo4,Main2Activity.this.getWindowManager()));
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
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (TYTool.pwdIsEmpty()) {
//                BaseActivity.finishAll();
//            } else {
//                MenuDialog.showNormalEntryDialog(MainActivity.this, null, null, null, "2");
//            }
        }

        return super.onKeyDown(keyCode,event);
    }
}
