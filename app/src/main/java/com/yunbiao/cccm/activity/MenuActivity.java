package com.yunbiao.cccm.activity;

import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.base.BaseActivity;
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.common.Const;
import com.yunbiao.cccm.utils.TimerUtil;
import com.yunbiao.cccm.utils.ToastUtil;

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
    @BindView(R.id.tv_menu_setting_hints)
    TextView tvMenuSettingHints;
    @BindView(R.id.tv_menu_setting_hints_2)
    TextView tvMenuSettingHints2;
    @BindView(R.id.tv_menu_setting)
    TextView tvMenuSetting;
    @BindView(R.id.prl_root)
    PercentRelativeLayout prlRoot;
    @BindView(R.id.btn_select_res_menu)
    Button btnSelect;
    @BindView(R.id.iv_menu_icon_start)
    ImageView ivMenuIconStart;

    private SoundPool soundPool;//用来管理和播放音频文件
    private int music;
    private TimerUtil timerUtil;
    private PlayListFragment playListFragment;

    protected int setLayout() {
        APP.setMenuActivity(this);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        return R.layout.activity_menu;
    }

    protected void initData() {
        timerUtil = TimerUtil.getInstance(onTimerListener);
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(this, R.raw.di, 1);
    }

    protected void initView() {
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

//        tvMenuSettingHints.setText("微信消息查看");
//        tvMenuSettingHints2.setText("微信上墙消息查看");
        tvMenuSettingHints2.setText("即将开放");

        tvShowOnscreenTime.setText(String.valueOf(Const.SYSTEM_CONFIG.MENU_STAY_DURATION));
        btnSelect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int white = getResources().getColor(android.R.color.white);
                        btnSelect.setTextColor(white);
                        break;
                    case MotionEvent.ACTION_UP:
                        int yellow = getResources().getColor(R.color.menu_bottom_num);
                        btnSelect.setTextColor(yellow);
                        break;
                }

                return false;
            }
        });
        updateDeviceNo();
    }

    private boolean isTimerRuning = false;

    @Override
    protected void onResume() {
        super.onResume();
        updatePlayButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerPause();
    }

    protected void timerStart(){
        if (timerUtil != null) {
            timerUtil.start(60);//开始计时
            isTimerRuning = true;
        }
    }

    protected void timerPause(){
        if (timerUtil != null) {
            timerUtil.pause();//pause时停掉计时
            isTimerRuning = false;
        }
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

    @OnClick({R.id.btn_select_res_menu, R.id.btn_menu_start, R.id.btn_menu_playlist, R.id.btn_menu_wechat})
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
                if (isFastClick()) {
                    ToastUtil.showShort(this, "请不要重复点击");
                } else {
                    onPause();
                    playListFragment = new PlayListFragment();
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.dialog_top_enter, R.anim.dialog_top_exit);
                    fragmentTransaction.add(R.id.prl_root, playListFragment).commit();
                }
                break;
            case R.id.btn_menu_wechat:
//                startActivity(new Intent(this, WeichatActivity.class));
                ToastUtil.showShort(this, "即将开放");
                break;
            case R.id.btn_select_res_menu:
                if (isFastClick()) {
                    ToastUtil.showShort(this, "请不要重复点击");
                } else {
                    MainController.getInstance().initPlayData(false);
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
        if (keyCode == KeyEvent.KEYCODE_BACK && playListFragment != null && playListFragment.isVisible()) {
            backFragment(playListFragment);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /***
     * 回退Fragment
     * @param fragment
     */
    public void backFragment(Fragment fragment) {
        onResume();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.dialog_top_enter, R.anim.dialog_top_exit);
        fragmentTransaction.remove(fragment).commit();
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
        APP.setMenuActivity(null);
    }

    //更新播放按钮
    public void updatePlayButton() {
        boolean isHasPlay = CacheManager.SP.getPlayTag();
        if (!isHasPlay) {
            if(isTimerRuning){
                timerPause();
            }
            btnMenuStart.setEnabled(false);
            Drawable drawable = getResources().getDrawable(R.mipmap.menu_nostart);
            drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
            ivMenuIconStart.setImageDrawable(drawable);
            tvMenuStartHints.setText("");
            tvMenuStartHints2.setText("暂无播放资源");
        } else {
            if(!isTimerRuning){
                timerStart();
            }
            btnMenuStart.setEnabled(true);
            Drawable drawable = getResources().getDrawable(R.mipmap.menu_start);
            drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
            ivMenuIconStart.setImageDrawable(drawable);
            tvMenuStartHints.setText(R.string.play);
            tvMenuStartHints2.setText(R.string.auto_play);
        }
    }

    public void updateDeviceNo(){
        tvMenuInfoEquipmentMum.setText(CacheManager.SP.getDeviceNum());
        tvMenuInfoAccessCode.setText(CacheManager.SP.getAccessCode());
    }

    public void updatePlayList(){
        if(playListFragment != null){
            playListFragment.updateList();
        }
    }
}