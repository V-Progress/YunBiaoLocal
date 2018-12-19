package com.yunbiao.yunbiaolocal.act.weichat;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.act.BaseActivity;
import com.yunbiao.yunbiaolocal.act.weichat.bean.WeiMessage;
import com.yunbiao.yunbiaolocal.cache.CacheManager;
import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.view.NumberProgressBar;
import com.yunbiao.yunbiaolocal.xmpp.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WeichatActivity extends BaseActivity implements WeiChatReceiver.OnWeChatMsgListener {


    @BindView(R.id.fl_wei_chat)
    FrameLayout flWeiChat;
    @BindView(R.id.pp_weichat_pp)
    NumberProgressBar ppWeichatPp;
    @BindView(R.id.tv_er_control)
    TextView tvErControl;
    @BindView(R.id.iv_erimage)
    ImageView ivErimage;
    @BindView(R.id.ll_wei_chat_ll)
    LinearLayout llWeiChatLl;
    @BindView(R.id.iv_head_icon)
    ImageView ivHeadIcon;
    @BindView(R.id.tv_head_name)
    TextView tvHeadName;
    @BindView(R.id.ll_head_msg)
    LinearLayout llHeadMsg;
    @BindView(R.id.ll_tip)
    LinearLayout llTip;
    @BindView(R.id.tv_rem_time)
    TextView tvRemTime;

    private WeiChatReceiver weiChatReceiver;
    private int totalTime_Seconds = Const.SYSTEM_CONFIG.WEICHAT_MSG_TIME;//微信消息展示总时长
    private List<WeiMessage> weiMessageList = new ArrayList<>();//微信消息列表


    @Override
    protected int setLayout() {
        APP.addActivity(this);
        return R.layout.activity_weichat;
    }

    @Override
    protected void onDestroy() {
        APP.removeActivity(this);
        super.onDestroy();
    }

    @Override
    protected void initView() {
        weiChatReceiver = new WeiChatReceiver();
        weiChatReceiver.setMsgListener(this);
        seterweiCodeSize();
    }

    @Override
    protected void initData() {
        registerWeiBroadCast();
        getWeichatQRCode();
    }

    /*
     * 注册微信广播
     */
    private void registerWeiBroadCast() {
        registerReceiver(weiChatReceiver, new IntentFilter(Constants.WEIXIN));
    }

    /*
    * 获取二维码
    * */
    private void getWeichatQRCode() {
        String wechatTicket = CacheManager.SP.getWechatTicket();
        ImageLoader.getInstance().displayImage(WeiChatConstant.TICKET_URL + wechatTicket, ivErimage);
    }

    /***
     * 微信消息监听
     * @param notificationMessage
     */
    @Override
    public void onMsgReceived(String notificationMessage) {
        parseReceivedMsg(notificationMessage);
    }

    /**
     * 解析广播接收的数据
     * 如果开机启动的没有屏幕id,直接处理，如果有的话先比较
     *
     * @param msg
     */
    @SuppressLint("ShowToast")
    private void parseReceivedMsg(final String msg) {
        WeiMessage weiMessage = new Gson().fromJson(msg, WeiMessage.class);
        weiMessageList.add(weiMessage);//每接收到一条消息就将其添加进list中
        if(!isTiming){//如果不在计时状态才会重新发
            timeHandler.sendEmptyMessage(0);
        }
    }

    private boolean isTiming = false;

    Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isTiming = true;
            tvRemTime.setText("" + totalTime_Seconds);
            if (totalTime_Seconds == 30) {
                setView(weiMessageList.get(0));//设置显示
                weiMessageList.remove(0);//删除显示的数据
            }

            totalTime_Seconds--;//总时长递减
            if (totalTime_Seconds < 0) {//小于0后重置
                totalTime_Seconds = Const.SYSTEM_CONFIG.WEICHAT_MSG_TIME;
            }

            if (weiMessageList.size() > 0) {//list内容大于0时继续计时
                timeHandler.sendEmptyMessageDelayed(0, 1000);
            } else {//不大于0时则进行最后一步计时
                new CountDownTimer((totalTime_Seconds +1)*1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        tvRemTime.setText("" + millisUntilFinished/1000);//设置数秒
                    }

                    @Override
                    public void onFinish() {
                        isTiming = false;
                        finish();//计时结束关闭界面
                    }
                }.start();
            }
        }
    };


    private void setView(WeiMessage weiMessage) {
        Integer type = weiMessage.getType();
        if (llTip.getVisibility() == View.INVISIBLE) {
            llTip.setVisibility(View.VISIBLE);
        }
        if (type == -1) {//隐藏与显示
            if (llWeiChatLl.getVisibility() == View.VISIBLE) {
                llWeiChatLl.setVisibility(View.INVISIBLE);
//                WeiChatSave.saveBoolean(WeichatActivity.this, WeiChatSave.WEICHAT_ECODE_VISIBILE, false);
            } else {
                llWeiChatLl.setVisibility(View.VISIBLE);
//                WeiChatSave.saveBoolean(WeichatActivity.this, WeiChatSave.WEICHAT_ECODE_VISIBILE, true);
            }
            return;
        }

        ImageLoader.getInstance().displayImage(weiMessage.getHeadUrl(), ivHeadIcon);
        tvHeadName.setText(weiMessage.getUserName());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        switch (weiMessage.getType()) {
            case 0:
                flWeiChat.removeAllViews();
                break;
            case 1:
                flWeiChat.removeAllViews();
                TextView textView = new TextView(WeichatActivity.this);
                textView.setText(weiMessage.getContent());
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.parseColor("#00000000"));
                textView.setTextSize(42);
                textView.setTextColor(getResources().getColor(R.color.white));
                textView.setLayoutParams(layoutParams);
                flWeiChat.addView(textView);
                break;
            case 2:
                flWeiChat.removeAllViews();
                ImageView imageView = new ImageView(WeichatActivity.this);
                ImageLoader.getInstance().displayImage(weiMessage.getContent(), imageView);
                imageView.setLayoutParams(layoutParams);
                flWeiChat.addView(imageView);
                break;
        }
    }

    /*
     * 设置二维码的大小
     */
    private void seterweiCodeSize() {
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int size = Math.min(screenWidth, screenHeight) / 10;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 5, 0, 5);
        ivErimage.setLayoutParams(params);
    }
}
