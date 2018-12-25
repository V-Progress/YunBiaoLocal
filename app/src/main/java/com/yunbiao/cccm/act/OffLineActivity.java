package com.yunbiao.cccm.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.cccm.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OffLineActivity extends Activity {

    @BindView(R.id.iv_sys_set)
    ImageView ivSysSet;
    @BindView(R.id.perRel_offline_1)
    PercentRelativeLayout perRelOffline1;
    @BindView(R.id.ibtn_layout_two)
    ImageButton ibtnLayoutTwo;
    @BindView(R.id.prl_position_two)
    PercentRelativeLayout prlPositionTwo;
    @BindView(R.id.ibtn_layout_one)
    ImageButton ibtnLayoutOne;
    @BindView(R.id.ibtn_layout_three)
    ImageButton ibtnLayoutThree;
    @BindView(R.id.perRel_offline_2)
    PercentRelativeLayout perRelOffline2;
    @BindView(R.id.cb_off_selectHead)
    CheckBox cbOffSelectHead;
    @BindView(R.id.cb_off_date)
    CheckBox cbOffDate;
    @BindView(R.id.cb_off_time)
    CheckBox cbOffTime;
    @BindView(R.id.cb_off_selectFooter)
    CheckBox cbOffSelectFooter;
    @BindView(R.id.ibtn_off_plan1)
    ImageButton ibtnOffPlan1;
    @BindView(R.id.ibtn_off_plan2)
    ImageButton ibtnOffPlan2;
    @BindView(R.id.ibtn_off_plan3)
    ImageButton ibtnOffPlan3;
    @BindView(R.id.ibtn_off_plan4)
    ImageButton ibtnOffPlan4;
    @BindView(R.id.tv_menu_offline_times)
    TextView tvMenuOfflineTimes;
    @BindView(R.id.tv_offline_rolling_time)
    TextView tvOfflineRollingTime;
    @BindView(R.id.iv_offline_times)
    ImageView ivOfflineTimes;
    @BindView(R.id.tv_menu_offline_animation_head)
    TextView tvMenuOfflineAnimationHead;
    @BindView(R.id.tv_menu_offline_animation)
    TextView tvMenuOfflineAnimation;
    @BindView(R.id.iv_offline_animation)
    ImageView ivOfflineAnimation;
    @BindView(R.id.iv_offline_music)
    ImageView ivOfflineMusic;
    @BindView(R.id.tv_menu_offline_music)
    TextView tvMenuOfflineMusic;
    @BindView(R.id.btn_off_ok)
    Button btnOffOk;
    private String[] animationItems;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private AlertDialog completedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        ButterKnife.bind(this);
        initView();
    }

    private void initView(){
        String dateTime = sdf.format(new Date());
        cbOffDate.setText(dateTime.substring(0, 10));
        cbOffTime.setText(dateTime.substring(11));

        tvMenuOfflineMusic.setText(R.string.hint_bg_music);
        animationItems = new String[]{getString(R.string.effect_no),
                getString(R.string.effect_ver),
                getString(R.string.effect_enlarge),
                getString(R.string.effect_left),
                getString(R.string.effect_right),
                getString(R.string.effect_rotate),
                getString(R.string.effect_transparency),
                getString(R.string.effect_enlarge_rotate)};
    }

    @OnClick({R.id.iv_goback,R.id.tv_menu_offline_animation,R.id.tv_offline_rolling_time,R.id.btn_off_ok,R.id.ibtn_layout_two, R.id.ibtn_layout_one, R.id.ibtn_layout_three,R.id.ibtn_off_plan1, R.id.ibtn_off_plan2, R.id.ibtn_off_plan3, R.id.ibtn_off_plan4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //分屏方案
            case R.id.ibtn_layout_two:
                layoutSelect(view.getId());
                break;
            case R.id.ibtn_layout_one:
                layoutSelect(view.getId());
                break;
            case R.id.ibtn_layout_three:
                layoutSelect(view.getId());
                break;
            //字幕滚动方案
            case R.id.ibtn_off_plan1:
                planSelect(view.getId());
                break;
            case R.id.ibtn_off_plan2:
                planSelect(view.getId());
                break;
            case R.id.ibtn_off_plan3:
                planSelect(view.getId());
                break;
            case R.id.ibtn_off_plan4:
                planSelect(view.getId());
                break;
            //图片切换
            case R.id.tv_offline_rolling_time:
                imgPlaySpace();
                break;
            case R.id.tv_menu_offline_animation:
                imgPlayAnim();
                break;
            //完成
            case R.id.btn_off_ok:
                okDialog(this);
                break;
            case R.id.iv_goback:
                finish();
                break;
        }
    }

    private void okDialog(Context context){
        if(completedDialog == null){
            initOkDialog(context);
        }
        if(!isFinishing()){
            completedDialog.show();
        }
    }

    private void initOkDialog(Context context){
        Log.e("123","初始化了dialog");
        completedDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_fir_dialog, null);
        TextView titleTextView = (TextView) view.findViewById(R.id.tv_pwd_title);
        TextView contentTextView = (TextView) view.findViewById(R.id.tv_first_entry_pwd_hints);
        EditText oneEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd);
        EditText twoEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd_confirm);
        final TextView cancelBtn = (Button) view.findViewById(R.id.btn_first_pwd_cancel);
        final TextView sureBtn = (Button) view.findViewById(R.id.btn_first_pwd_sure);

        titleTextView.setText(R.string.is_sure_publish_offline);
        contentTextView.setVisibility(View.VISIBLE);
        contentTextView.setText(R.string.hint_publish_offline);
        oneEditText.setVisibility(View.GONE);
        twoEditText.setVisibility(View.GONE);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_first_pwd_cancel:
                        completedDialog.dismiss();
                        break;
                    case R.id.btn_first_pwd_sure:
                        completedDialog.dismiss();
//                        setComplete();
                        finish();
                        break;
                }
            }
        };
        cancelBtn.setOnClickListener(clickListener);
        sureBtn.setOnClickListener(clickListener);
        completedDialog.setView(view, 0, 0, 0, 0);
    }

    /*
     * 选择图片播放效果
     */
    private void imgPlayAnim(){
        AlertDialog.Builder builder = new AlertDialog.Builder(OffLineActivity.this);
        builder.setTitle(R.string.please_select_animation)
                .setItems(animationItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvMenuOfflineAnimation.setText(animationItems[which]);
                    }
                }).create().show();
    }

    /*
     * 选择图片轮播时间
     */
    private String[] items = new String[]{"5", "8", "10", "15", "20", "30", "60"};
    private void imgPlaySpace(){
        AlertDialog.Builder builder = new AlertDialog.Builder(OffLineActivity.this);
        builder.setTitle(R.string.please_select_time)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvOfflineRollingTime.setText(items[which]);
                    }
                }).create().show();
    }

    /*
     * 选择分屏方案
     */
    int[] ibtns = {R.id.ibtn_layout_one,R.id.ibtn_layout_two,R.id.ibtn_layout_three};
    private void layoutSelect(int vid){
        for (int ibtn : ibtns) {
            if(ibtn == vid){
                ((ImageButton)findViewById(ibtn)).setImageDrawable(getResources().getDrawable(R.mipmap.screen_active));
            }else{
                ((ImageButton)findViewById(ibtn)).setImageDrawable(null);
            }
        }
    }

    /*
     * 选择字幕滚动方案
     */
    int[] plans = {R.id.ibtn_off_plan1,R.id.ibtn_off_plan2,R.id.ibtn_off_plan3,R.id.ibtn_off_plan4};
    private void planSelect(int vid){
        for (int plan : plans) {
            if(plan == vid){
                ((ImageButton)findViewById(plan)).setImageDrawable(getResources().getDrawable(R.mipmap.subtitle_active));
            }else{
                ((ImageButton)findViewById(plan)).setImageDrawable(null);
            }
        }
    }

//    private void setComplete() {
//        LayoutModel layoutModel = new LayoutModel();
//
//        layoutModel.setRunType("1");
//        layoutModel.setStart("00:00");
//        layoutModel.setResClearModel("0");
//        layoutModel.setEnd("00:00");
//
//        List<Center> centerList = new ArrayList<>();
//        Center center = new Center();
//        Container container = new Container();
//        container.setHeight("100%");
//        container.setWidth("100%");
//        container.setLeft("0%");
//        container.setTop("0%");
//        center.setContainer(container);
//        center.setId("row1_col1");
//        center.setType("8");
//        ImageDetail imageDetail = new ImageDetail();
//        imageDetail.setImagePlayType("0");
//        imageDetail.setIsAutoPlay("true");
//        imageDetail.setPlayTime("5");
//        center.setImageDetail(imageDetail);
//        centerList.add(center);
//        layoutModel.setCenter(centerList);
//
//        Footer footer = new Footer();
//        footer.setEnabled("false");
//        footer.setBackground("#ffffff");
//        footer.setFontColor("#000000");
//        footer.setFooterText("");
//        footer.setPlayTime("1");
//        footer.setIsPlay("true");
//        footer.setFontSize("12");
//        footer.setFontFamily("2");
//        layoutModel.setFooter(footer);
//
//        Header header = new Header();
//        header.setEnabled("false");
//        header.setLogoimg("");
//        header.setFontColor("#ffffff");
//        header.setTimeShow("1");
//        header.setAddress("110108:北京市");
//        header.setBackground("#000000");
//        header.setTimeFormat("yyyy年MM月dd日");
//        header.setFontFamily("2");
//        header.setWeatherShow("0");
//        header.setFontSize("12");
//        layoutModel.setHeader(header);
//
//        List<Move> moves = new ArrayList<>();
//        Move move = new Move();
//        Move.BGM bgm = new Move().new BGM();
//        bgm.setMusic("播放本地背景音乐");
//        move.setType("move_3");
//        move.setBgmusic(bgm);
//        moves.add(move);
//        layoutModel.setMove(moves);
//    }
}
