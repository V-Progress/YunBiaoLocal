package com.yunbiao.cccm.net2.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.SplashActivity;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.net2.cache.CacheManager;
import com.yunbiao.cccm.net2.common.Const;
import com.yunbiao.cccm.net2.event.DataLoadFinishedEvent;
import com.yunbiao.cccm.net2.event.HasDataEvent;
import com.yunbiao.cccm.net2.utils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

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
    @BindView(R.id.tv_menu_offline)
    TextView tvMenuOffline;
    @BindView(R.id.btn_menu_wechat)
    Button btnMenuWechat;

    @BindView(R.id.iv_wifi_icon)
    ImageView ivWifiIcon;
    @BindView(R.id.tv_wifi_name)
    TextView tvWifiName;
    @BindView(R.id.iv_conn_state)
    ImageView ivConnState;
    @BindView(R.id.pb_conn_state)
    ProgressBar pbConnState;
    @BindView(R.id.tv_server_connect_state)
    TextView tvServerConnState;

    @BindView(R.id.tv_menu_setting)
    TextView tvMenuSetting;
    @BindView(R.id.prl_root)
    PercentRelativeLayout prlRoot;
    @BindView(R.id.iv_menu_icon_start)
    ImageView ivMenuIconStart;

    @BindView(R.id.spn_storage_mode)
    Spinner spnStorage;

    @BindView(R.id.btn_offline)
    TextView btnOffline;

    private SoundPool soundPool;//用来管理和播放音频文件
    private int music;
    private WifiManager wifiManager;
    private ConnectivityManager connectManager;
    private final String NETWORK_STATE_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private boolean isDataLoadFinished = false;

    public static boolean isServerConnected = false;

    protected int setLayout() {
        APP.setMenuActivity(this);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        return R.layout.activity_menu;
    }

    private int time = 60;
    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tvShowOnscreenTime.setText("" + time);
            if (time <= 0) {
                time = 60;
                finish();
            }
            time--;
            timerHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    protected void initData() {
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        registerBroadcast();

        updatePlayButton();
        timerHandler.sendEmptyMessage(0);
    }

    protected void initView() {
        isDataLoadFinished = MainActivity.isDataLoadFinished;
        btnMenuStart.setOnFocusChangeListener(this);
        btnMenuOffline.setOnFocusChangeListener(this);
        btnMenuWechat.setOnFocusChangeListener(this);

        tvMenuOfflineHints2.setText(R.string.use_usb_play);
        tvMenuInfoPrompt.setText(R.string.hint_click_play);

        tvMenuOfflineHints3.setText("节目列表");
        tvMenuOfflineHints.setText("本地资源");
        tvMenuOfflineHints2.setText("查看本地已保存的节目");

        tvMenuStartHints.setText(R.string.play);
        tvMenuStartHints2.setText(R.string.auto_play);

        tvShowOnscreenTime.setText(String.valueOf(Const.SYSTEM_CONFIG.MENU_STAY_DURATION));

        btnMenuStart.requestFocus();

        initVersionSpinner();

        initStorageSpinner();

        updateDeviceNo();
    }

    private void initVersionSpinner(){
        btnOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CacheManager.SP.putMode(0);

                APP.restart();
//
//                startActivity(new Intent(MenuActivity.this, SplashActivity.class));
//
//                APP.exit();
            }
        });
    }

    private void initStorageSpinner() {
        List<String> storageList = new ArrayList<>();
        storageList.add("SD卡模式");
        storageList.add("U盘模式");
        storageList.add("本地存储模式");
        StorageAdapter storageAdapter = new StorageAdapter(this, storageList);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_employ_button);
        spnStorage.setPopupBackgroundDrawable(drawable);
        spnStorage.setAdapter(storageAdapter);
        spnStorage.setSelection(Const.STORAGE_TYPE);
        spnStorage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long l) {
                if(Const.STORAGE_TYPE == position){
                    return;
                }

                showExitDialog(MenuActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        spnStorage.setSelection(Const.STORAGE_TYPE);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CacheManager.SP.put(CacheManager.STORAGE_TYPE, position + "");
                        APP.restart();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private boolean hasConfig = false;
    private boolean hasInsert = false;

    private boolean hasData = false;
    private HasDataEvent mEvent;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(HasDataEvent event) {
        if (event.getType() == 0) {
            hasConfig = event.isHasData();
        } else {
            hasInsert = event.isHasData();
        }

        hasData = hasConfig || hasInsert;
        updatePlayButton();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(DataLoadFinishedEvent event) {
        isDataLoadFinished = true;
    }

    //注册接收器
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(NETWORK_STATE_CHANGE);
        registerReceiver(wifiReceiver, filter);
    }

    //注销广播
    private void unRegister() {
        unregisterReceiver(wifiReceiver);
    }

    //网络监听
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, NETWORK_STATE_CHANGE)
                    || TextUtils.equals(action, WifiManager.WIFI_STATE_CHANGED_ACTION)
                    || TextUtils.equals(action, WifiManager.RSSI_CHANGED_ACTION)) {
                setNetState();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        time = 60;
    }

    @Override
    protected void onPause() {
        super.onPause();
        time = 60;
    }

    @OnClick({R.id.btn_menu_start, R.id.btn_menu_playlist})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_menu_start:
                if (isFastClick()) {
                    ToastUtil.showShort(this, "请不要重复点击");
                } else {
                    finish();
                }
                break;
            case R.id.btn_menu_playlist:
                if (isDataLoadFinished) {
                    if (isFastClick()) {
                        ToastUtil.showShort(this, "请不要重复点击");
                    } else {
                        startActivity(new Intent(this, PlayListActivity.class));
                    }
                } else {
                    ToastUtil.showShort(this, "数据未加载完毕，请等待");
                }
                break;
        }
    }

    private static final int MIN_DELAY_TIME = 1500;  // 两次点击间隔不能少于1000ms
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            soundPool.play(music, 1, 1, 0, 0, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeMessages(0);
        unRegister();
        APP.setMenuActivity(null);
    }

    private boolean mCacheHasData = false;

    //更新播放按钮
    public void updatePlayButton() {
        if (mCacheHasData == hasData) {
            return;
        }
        mCacheHasData = hasData;

        if (mCacheHasData) {
            hasData();
        } else {
            noData();
        }
    }

    private void hasData() {
        time = 60;
        timerHandler.sendEmptyMessage(0);
        btnMenuStart.setEnabled(true);
        Drawable drawable = getResources().getDrawable(R.mipmap.menu_start);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        ivMenuIconStart.setImageDrawable(drawable);
        tvMenuStartHints.setText(R.string.play);
        tvMenuStartHints2.setText(R.string.auto_play);
    }

    private void noData() {
        time = 60;
        timerHandler.removeMessages(0);
        btnMenuStart.setEnabled(false);
        Drawable drawable = getResources().getDrawable(R.mipmap.menu_nostart);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        ivMenuIconStart.setImageDrawable(drawable);
        tvMenuStartHints.setText("");
        tvMenuStartHints2.setText("暂无播放资源");
    }

    public void updateDeviceNo() {
        tvMenuInfoEquipmentMum.setText(CacheManager.SP.getDeviceNum());
        tvMenuInfoAccessCode.setText(CacheManager.SP.getAccessCode());
    }

    //设置界面显示状态
    private void setNetState() {
        if (!isWifiEnabled()) {//wifi未开，设置wifi图标为disable，连接状态为未连接
            isServerConnected = false;
            ivWifiIcon.setImageResource(R.mipmap.wifi_disable);
            ivConnState.setImageResource(R.mipmap.conn_error);
            tvWifiName.setText("WIFI不可用");
            tvServerConnState.setText("请检查网络");
            return;
        }

        if (!isWifiConnected()) {//wifi未联网，设置wifi图标为noConnected，连接状态为未连接
            isServerConnected = false;
            ivWifiIcon.setImageResource(R.mipmap.wifi_disconn);
            ivConnState.setImageResource(R.mipmap.conn_error);
            tvWifiName.setText("网络未连接");
            tvServerConnState.setText("请检查网络");
            return;
        }

        //已开启，已联网，设置wifi信号强度，wifi名称设置连接状态
        String wifiName = getWifiName();
        tvWifiName.setText(wifiName);
        switch (getWifiStrength()) {
            case 0:
                ivWifiIcon.setImageResource(R.mipmap.wifi_0);
                break;
            case 1:
                ivWifiIcon.setImageResource(R.mipmap.wifi_1);
                break;
            case 2:
                ivWifiIcon.setImageResource(R.mipmap.wifi_2);
                break;
            case 3:
                ivWifiIcon.setImageResource(R.mipmap.wifi_3);
                break;
        }

        //连接成功
        if (isServerConnected) {
            ivConnState.setVisibility(View.VISIBLE);
            pbConnState.setVisibility(View.GONE);
            ivConnState.setImageResource(R.mipmap.conn_succ);
            tvServerConnState.setText("连接成功");
            return;
        }

        ivConnState.setVisibility(View.GONE);
        pbConnState.setVisibility(View.VISIBLE);
        tvServerConnState.setText("连接中...");
    }

    //获取wifi状态
    public boolean isWifiEnabled() {
        int wifiState = wifiManager.getWifiState();
        return wifiState == WifiManager.WIFI_STATE_ENABLED;
    }

    public boolean isWifiConnected() {
        //wifi连接
        NetworkInfo info = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info.isConnected();
    }

    //获取wifi名称
    public String getWifiName() {
        WifiInfo info = wifiManager.getConnectionInfo();
        if ((info != null) && (!TextUtils.isEmpty(info.getSSID()))) {
            return info.getSSID();
        }
        return "NULL";
    }

    //获取信号强度
    public int getWifiStrength() {
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getBSSID() != null) {
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 4);
            return strength;
        }
        return 0;
    }

}
