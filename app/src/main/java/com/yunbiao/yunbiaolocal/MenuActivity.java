package com.yunbiao.yunbiaolocal;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuActivity extends Activity implements View.OnFocusChangeListener {

    @BindView(R.id.menu_info_logo)
    ImageView menuInfoLogo;
    @BindView(R.id.tv_menu_info_yb)
    TextView tvMenuInfoYb;
    @BindView(R.id.tv_slogan)
    TextView tvSlogan;
    @BindView(R.id.btn_weiChat_page)
    Button btnWeiChatPage;
    @BindView(R.id.menu_info_bind_btn)
    Button menuInfoBindBtn;
    @BindView(R.id.tv_menu_info_time)
    TextClock tvMenuInfoTime;
    @BindView(R.id.tv_menu_info_date)
    TextClock tvMenuInfoDate;
    @BindView(R.id.tv_menu_info_temper)
    TextView tvMenuInfoTemper;
    @BindView(R.id.iv_menu_info_temper)
    ImageView ivMenuInfoTemper;
    @BindView(R.id.ll_menu_above)
    PercentRelativeLayout llMenuAbove;
    @BindView(R.id.iv_menu_info_prompt)
    ImageView ivMenuInfoPrompt;
    @BindView(R.id.tv_menu_info_prompt)
    TextView tvMenuInfoPrompt;
    @BindView(R.id.menu_info_ellipse_btn)
    Button menuInfoEllipseBtn;
    @BindView(R.id.tv_menu_info_equipmentMum)
    TextView tvMenuInfoEquipmentMum;
    @BindView(R.id.tv_menu_info_accessCode)
    TextView tvMenuInfoAccessCode;
    @BindView(R.id.ll_menu_bottom)
    PercentRelativeLayout llMenuBottom;
    @BindView(R.id.btn_menu_start)
    Button btnMenuStart;
    @BindView(R.id.iv_menu_icon_start)
    ImageView ivMenuIconStart;
    @BindView(R.id.tv_show_onscreen_time)
    TextView tvShowOnscreenTime;
    @BindView(R.id.tv_menu_start_hints)
    TextView tvMenuStartHints;
    @BindView(R.id.tv_menu_start_hints_2)
    TextView tvMenuStartHints2;
    @BindView(R.id.perRe_position_01)
    PercentRelativeLayout perRePosition01;
    @BindView(R.id.tv_menu_start)
    TextView tvMenuStart;
    @BindView(R.id.btn_menu_offline)
    Button btnMenuOffline;
    @BindView(R.id.iv_menu_icon_offline)
    ImageView ivMenuIconOffline;
    @BindView(R.id.tv_menu_offline_hints)
    TextView tvMenuOfflineHints;
    @BindView(R.id.tv_menu_offline_hints_2)
    TextView tvMenuOfflineHints2;
    @BindView(R.id.tv_menu_offline_hints_3)
    TextView tvMenuOfflineHints3;
    @BindView(R.id.perRe_position_02_1)
    PercentRelativeLayout perRePosition021;
    @BindView(R.id.btn_menu_offline2)
    Button btnMenuOffline2;
    @BindView(R.id.iv_menu_icon_offline2)
    ImageView ivMenuIconOffline2;
    @BindView(R.id.tv_menu_offline2_hints)
    TextView tvMenuOffline2Hints;
    @BindView(R.id.tv_menu_offline2_hints_2)
    TextView tvMenuOffline2Hints2;
    @BindView(R.id.perRe_position_02_2)
    PercentRelativeLayout perRePosition022;
    @BindView(R.id.perRe_position_02)
    PercentRelativeLayout perRePosition02;
    @BindView(R.id.tv_menu_offline)
    TextView tvMenuOffline;
    @BindView(R.id.btn_menu_service)
    Button btnMenuService;
    @BindView(R.id.iv_menu_icon_service)
    ImageView ivMenuIconService;
    @BindView(R.id.tv_menu_service_hints)
    TextView tvMenuServiceHints;
    @BindView(R.id.tv_menu_service_hints_2)
    TextView tvMenuServiceHints2;
    @BindView(R.id.tv_menu_info_ser_head)
    TextView tvMenuInfoSerHead;
    @BindView(R.id.tv_menu_info_ser)
    TextView tvMenuInfoSer;
    @BindView(R.id.tv_menu_info_conn_head)
    TextView tvMenuInfoConnHead;
    @BindView(R.id.tv_menu_info_conn)
    TextView tvMenuInfoConn;
    @BindView(R.id.tv_menu_info_decName_head)
    TextView tvMenuInfoDecNameHead;
    @BindView(R.id.tv_menu_info_decName)
    TextView tvMenuInfoDecName;
    @BindView(R.id.prl_ser_one)
    PercentRelativeLayout prlSerOne;
    @BindView(R.id.perRe_position_03)
    PercentRelativeLayout perRePosition03;
    @BindView(R.id.tv_menu_service)
    TextView tvMenuService;
    @BindView(R.id.btn_menu_setting)
    Button btnMenuSetting;
    @BindView(R.id.iv_menu_icon_setting)
    ImageView ivMenuIconSetting;
    @BindView(R.id.tv_menu_setting_hints)
    TextView tvMenuSettingHints;
    @BindView(R.id.tv_menu_setting_hints_2)
    TextView tvMenuSettingHints2;
    @BindView(R.id.tv_menu_ip)
    TextView tvMenuIp;
    @BindView(R.id.iv_menu_is_network)
    ImageView ivMenuIsNetwork;
    @BindView(R.id.perRe_position_04)
    PercentRelativeLayout perRePosition04;
    @BindView(R.id.tv_menu_setting)
    TextView tvMenuSetting;

    private SoundPool soundPool;//用来管理和播放音频文件
    private int music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);

        initView();
    }

    private void initView(){
        btnMenuStart.setOnFocusChangeListener(this);
        btnMenuOffline.setOnFocusChangeListener(this);
        btnMenuOffline2.setOnFocusChangeListener(this);
        btnMenuService.setOnFocusChangeListener(this);
        btnMenuSetting.setOnFocusChangeListener(this);

        tvMenuStartHints.setText(R.string.play);
        tvMenuStartHints2.setText(R.string.auto_play);
        tvMenuOfflineHints.setText(R.string.local_program);
        tvMenuOfflineHints2.setText(R.string.use_usb_play);
        tvMenuServiceHints.setText(R.string.yun_or_local);
        tvMenuServiceHints2.setText(R.string.delete_current_layout);
        tvMenuSettingHints.setText(R.string.system_base_setting);
        tvMenuSettingHints2.setText(R.string.pwd_on_off);
        tvMenuOffline2Hints.setText(R.string.import_layout);
        tvMenuOffline2Hints2.setText(R.string.use_yun_import_layout);
        tvMenuOfflineHints3.setText(R.string.set_layout);
//        TYTool.deviceNumber(equipmentMumTextView, accessCodeTextView);
        tvMenuInfoPrompt.setText(R.string.hint_click_play);

    }

    @OnClick({R.id.btn_weiChat_page, R.id.btn_menu_start, R.id.btn_menu_offline, R.id.btn_menu_offline2, R.id.btn_menu_service, R.id.btn_menu_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_weiChat_page:
                break;
            case R.id.btn_menu_start:
                break;
            case R.id.btn_menu_offline:
                break;
            case R.id.btn_menu_offline2:
                break;
            case R.id.btn_menu_service:
                break;
            case R.id.btn_menu_setting:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            soundPool.play(music, 1, 1, 0, 0, 1);
        }
    }
}
