package com.yunbiao.yunbiaolocal.act;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.act.weichat.WeichatActivity;
import com.yunbiao.yunbiaolocal.cache.CacheManager;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.common.HeartBeatClient;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.resolve.VideoDataResolver;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.TimerUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

public class MenuActivity extends BaseActivity implements View.OnFocusChangeListener {

    @BindView(R.id.tv_menu_info_prompt)
    TextView tvMenuInfoPrompt;
    @BindView(R.id.tv_menu_info_equipmentMum)
    TextView tvMenuInfoEquipmentMum;
    @BindView(R.id.tv_menu_info_accessCode)
    TextView tvMenuInfoAccessCode;
    @BindView(R.id.btn_menu_start)
    Button btnMenuStart;
    @BindView(R.id.tv_show_onscreen_time)
    TextView tvShowOnscreenTime;
    @BindView(R.id.tv_menu_start_hints)
    TextView tvMenuStartHints;
    @BindView(R.id.tv_menu_start_hints_2)
    TextView tvMenuStartHints2;
    @BindView(R.id.tv_menu_start)
    TextView tvMenuStart;
    @BindView(R.id.btn_menu_playlist)
    Button btnMenuOffline;
    @BindView(R.id.tv_menu_offline_hints)
    TextView tvMenuOfflineHints;
    @BindView(R.id.tv_menu_offline_hints_2)
    TextView tvMenuOfflineHints2;
    @BindView(R.id.tv_menu_offline_hints_3)
    TextView tvMenuOfflineHints3;
    @BindView(R.id.prl_bind_area)
    PercentRelativeLayout prlBindArea;
    @BindView(R.id.tv_menu_offline)
    TextView tvMenuOffline;
    @BindView(R.id.btn_menu_bind)
    Button btnMenuService;
    @BindView(R.id.iv_menu_icon_service)
    ImageView ivMenuIconService;
    @BindView(R.id.tv_menu_service_hints)
    TextView tvMenuServiceHints;
    @BindView(R.id.tv_menu_service_hints_2)
    TextView tvMenuServiceHints2;
    @BindView(R.id.tv_menu_info_ser)
    TextView tvMenuInfoSer;
    @BindView(R.id.tv_menu_info_conn)
    TextView tvMenuInfoConn;
    @BindView(R.id.tv_menu_info_decName)
    TextView tvMenuInfoDecName;
    @BindView(R.id.tv_menu_service)
    TextView tvMenuService;
    @BindView(R.id.btn_menu_setting)
    Button btnMenuSetting;
    @BindView(R.id.tv_menu_setting_hints)
    TextView tvMenuSettingHints;
    @BindView(R.id.tv_menu_setting_hints_2)
    TextView tvMenuSettingHints2;
    @BindView(R.id.tv_menu_setting)
    TextView tvMenuSetting;
    @BindView(R.id.edt_bind_username)
    EditText edtBindUsername;
    @BindView(R.id.edt_bind_code)
    EditText edtBindCode;
    @BindView(R.id.tv_menu_info_bindstate)
    TextView tvBindStatus;
    @BindView(R.id.tv_bind_hints)
    TextView tvBindHints;

    private SoundPool soundPool;//用来管理和播放音频文件
    private int music;
    private TimerUtil timerUtil;

    protected int setLayout() {
        APP.setMenuActivity(this);
        return R.layout.activity_menu;
    }

    protected void initData() {
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);
        timerUtil = TimerUtil.getInstance(this).listen(onTimerListener);
    }

    protected void initView() {
        btnMenuStart.setOnFocusChangeListener(this);
        btnMenuOffline.setOnFocusChangeListener(this);
        btnMenuService.setOnFocusChangeListener(this);
        btnMenuSetting.setOnFocusChangeListener(this);

        tvMenuStartHints.setText(R.string.play);
        tvMenuStartHints2.setText(R.string.auto_play);
        tvMenuOfflineHints.setText("本地资源");
        tvMenuOfflineHints2.setText(R.string.use_usb_play);
        tvMenuOfflineHints2.setText("查看本地已保存的节目");
        tvMenuServiceHints.setText("绑定已有用户");
        tvMenuServiceHints2.setText("通过管理平台控制设备");
        tvMenuSettingHints.setText("微信消息查看");
        tvMenuSettingHints2.setText("微信上墙消息查看");
        tvMenuOfflineHints3.setText("节目列表");
        tvMenuInfoPrompt.setText(R.string.hint_click_play);

        tvShowOnscreenTime.setText(String.valueOf(Const.SYSTEM_CONFIG.MENU_STAY_DURATION));

        setConnInfo(0);

        //屏蔽绑定功能
//        String status = CacheManager.SP.getStatus();//0未绑定 1绑定
//        if(TextUtils.equals("0",status)){
//            unbind();
//        }else{
//            binded();
//        }

        edtBindUsername.setOnFocusChangeListener(focusChangeListener);
        edtBindCode.setOnFocusChangeListener(focusChangeListener);
    }

    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.edt_bind_username:
                case R.id.edt_bind_code:
                    if(!hasFocus){
                        onResume();
                        return;
                    }
                    onPause();
                    break;
            }
        }
    };

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

    @OnClick({R.id.btn_bind, R.id.btn_menu_start, R.id.btn_menu_playlist, R.id.btn_menu_bind, R.id.btn_menu_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_menu_start:
                finish();
                break;
            case R.id.btn_menu_playlist:
                onPause();
                DialogUtil.showPlayListDialog(this, VideoDataResolver.playList == null
                        ? new ArrayList<String>()
                        : VideoDataResolver.playList, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onResume();
                    }
                });
                break;
            case R.id.btn_menu_bind:
                break;
            case R.id.btn_menu_setting:
                startActivity(new Intent(this, WeichatActivity.class));
//                startActivity(new Intent(this, AbsoluteActivity.class));
                break;
            case R.id.btn_bind:
                requestBindUser();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            soundPool.play(music, 1, 1, 0, 0, 1);
        }
    }

    private void requestBindUser(){
        String deviceNo = HeartBeatClient.getDeviceNo();
        String userName = edtBindUsername.getText().toString();
        String userRand = edtBindCode.getText().toString();

        if(TextUtils.isEmpty(deviceNo)){
            tvBindHints.setText(getResources().getString(R.string.bind_code_no_get));
            return;
        }
        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(userRand)){
            tvBindHints.setText(getResources().getString(R.string.bind_code_is_null));
            return;
        }

        Map map = new HashMap();
        map.put("deviceNo", deviceNo);
        map.put("userName", userName);
        map.put("userRand", userRand);
        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.DEC_NUM, map, new StringCallback() {
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
                            binded();
                            onResume();
                            CacheManager.SP.putStatus("1");
                            Toast.makeText(APP.getContext(), R.string.bind_dev_ok, Toast.LENGTH_SHORT).show();
                            break;
                        case "2":
                            tvBindHints.setText(R.string.bind_code_no);
                            break;
                        default:
                            tvBindHints.setText(R.string.bind_code_transfer_error);
                            break;
                    }
                }
            }
        });

    }

    private void binded(){
        prlBindArea.setVisibility(View.GONE);
        tvBindStatus.setText(R.string.bind_user);
        tvBindStatus.setTextColor(Color.parseColor("#95e546"));
        ivMenuIconService.setVisibility(View.VISIBLE);
    }

    private void unbind(){
        ivMenuIconService.setVisibility(View.INVISIBLE);
        prlBindArea.setVisibility(View.VISIBLE);
        tvBindStatus.setText(R.string.unbind_user);
        tvBindStatus.setTextColor(Color.parseColor("#ADADAD"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        APP.setMenuActivity(null);
    }

    public void setConnInfo(int isConn) {
        tvMenuInfoConn.setText("正在连接...");
        if(isConn == 1){
            tvMenuInfoConn.setText("连接成功");
        }
        tvMenuInfoEquipmentMum.setText(CacheManager.SP.getDeviceNum());
        tvMenuInfoAccessCode.setText(CacheManager.SP.getAccessCode());
        tvMenuInfoDecName.setText(Build.MODEL);
        tvMenuInfoSer.setText(Const.DOMAIN);

        //设备是否绑定
        String bindStatus = CacheManager.SP.getBindStatus();
        if(!TextUtils.equals("1",bindStatus)){
            unbind();
        }
    }
}
