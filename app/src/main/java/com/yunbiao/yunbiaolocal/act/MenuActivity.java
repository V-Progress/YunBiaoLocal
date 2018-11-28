package com.yunbiao.yunbiaolocal.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.HeartBeatClient;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layouthandle.LayoutRefresher;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.TimerUtil;
import com.yunbiao.yunbiaolocal.viewfactory.ViewFactory;
import com.yunbiao.yunbiaolocal.viewfactory.bean.Container;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.TextDetail;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

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
    private AlertDialog bindDecDialog;
    private TimerUtil timerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);
        timerUtil = TimerUtil.getInstance(this).listen(onTimerListener);

        initView();
    }

    private void initView() {
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
        tvMenuInfoPrompt.setText(R.string.hint_click_play);

        menuInfoBindBtn.setText("未绑定");
        menuInfoBindBtn.setTextColor(Color.parseColor("#ADADAD"));
        menuInfoBindBtn.setBackgroundResource(R.drawable.no_service_btn);

    }

    @Override
    protected void onResume() {
        super.onResume();
        timerUtil.start(60);//开始计时
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerUtil.pause();//pause时停掉计时
    }

    /***
     * 倒计时监听
     */
    TimerUtil.OnTimerListener onTimerListener = new TimerUtil.OnTimerListener() {
        @Override
        public void onTimeStart() {
            tvShowOnscreenTime.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTiming(int recLen) {
            tvShowOnscreenTime.setText("" + recLen);
        }

        @Override
        public void onTimeFinish() {
            tvShowOnscreenTime.setVisibility(View.GONE);
            finish();
        }
    };

    @OnClick({R.id.menu_info_bind_btn, R.id.btn_weiChat_page, R.id.btn_menu_start, R.id.btn_menu_offline, R.id.btn_menu_offline2, R.id.btn_menu_service, R.id.btn_menu_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_menu_start:
                if (false/*LayoutCache.getLayoutCacheAsArray() == null*/) {
//                    startActivity(new Intent(this, SwitchLayout.class));
                } else {
                    finish();
                }
                break;
            case R.id.btn_menu_offline:
                jumpAct(OffLineActivity.class);
                break;
            case R.id.btn_menu_offline2:
                break;
            case R.id.btn_menu_service:
                LayoutInfo info1 = getLayout("123", "0%", "0%", "hahahahahhaha");
                View view1 = ViewFactory.createView(ViewFactory.VIEW_TEXT, this, info1, getWindowManager());
                LayoutRefresher.getInstance().updateLayout(view1);
                break;
            case R.id.btn_menu_setting:
                LayoutInfo info = getLayout("123", "50%", "0%", "hahahahahhaha");
                View view2 = ViewFactory.createView(ViewFactory.VIEW_TEXT,this, info, getWindowManager());
                LayoutRefresher.getInstance().updateLayout(view2);
                finish();

                break;
            case R.id.btn_weiChat_page:
                break;
            case R.id.menu_info_bind_btn:
                showBindDialog(this);
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            soundPool.play(music, 1, 1, 0, 0, 1);
        }
    }

    public void jumpAct(Class clz) {
        startActivity(new Intent(this, clz));
    }

    /**
     * 显示绑定设备dialog
     * @param context
     */
    private void showBindDialog(Context context) {
        timerUtil.pause();
        bindDecDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.menu_bind_dialog, null);
        TextView bindTitleTextView = (TextView) view.findViewById(R.id.tv_bind_title);
        final EditText bindNameEditText = (EditText) view.findViewById(R.id.et_bind_name);
        final EditText bindNumEditText = (EditText) view.findViewById(R.id.et_bind_num);
        Button bindCancelBtn = (Button) view.findViewById(R.id.btn_bind_cancel);
        Button bindSureBtn = (Button) view.findViewById(R.id.btn_bind_sure);
        final TextView bindHintsTextView = (TextView) view.findViewById(R.id.tv_bind_hints);

        bindTitleTextView.setText(R.string.bind_dev);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_bind_sure:
                        String deviceNo = HeartBeatClient.getDeviceNo();
                        String userName = bindNameEditText.getText().toString();
                        String userRand = bindNumEditText.getText().toString();
                        if (!TextUtils.isEmpty(deviceNo)) {
                            Map map = new HashMap();
                            map.put("deviceNo", deviceNo);
                            map.put("userName", userName);
                            map.put("userRand", userRand);

                            NetUtil.getInstance().post("", map, new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {

                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    if (response.startsWith("\"")) {
                                        response = response.substring(1, response.length() - 1);
                                    }
                                    if (!response.equals("faile")) {
                                        String[] split = response.split("\"");
                                        String result1 = split[split.length - 2];
                                        switch (result1) {
                                            case "1":
//                                            if (btn != null) {
//                                                btn.setText(R.string.menu_bind_user);
//                                                btn.setTextColor(Color.parseColor("#95e546"));
//                                                btn.setBackgroundResource(R.drawable.is_service_btn);
//                                            }
//                                            if (btn2 != null) {
//                                                btn2.setText(R.string.bind_user);
//                                                btn2.setBackgroundResource(R.drawable.wei_chat_btn);
//                                            }
                                                Toast.makeText(APP.getContext(), R.string.bind_dev_ok, Toast.LENGTH_SHORT).show();
                                                bindDecDialog.dismiss();
                                                break;
                                            case "2":
                                                bindHintsTextView.setText(R.string.bind_code_no);
                                                break;
                                            default:
                                                bindHintsTextView.setText(R.string.bind_code_transfer_error);
                                                break;
                                        }
                                    }
                                }
                            });


                        }
                        break;
                    case R.id.btn_bind_cancel:

                        bindDecDialog.dismiss();
                        break;
                }
                timerUtil.start(60);
            }
        };
        bindCancelBtn.setOnClickListener(clickListener);
        bindSureBtn.setOnClickListener(clickListener);

        bindDecDialog.setView(view, 0, 0, 0, 0);
        bindDecDialog.show();
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

}
