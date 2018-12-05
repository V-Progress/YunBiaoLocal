package com.yunbiao.yunbiaolocal.netcore;

import android.os.Handler;
import android.os.Message;


import com.yunbiao.yunbiaolocal.cache.ResConstants;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.ThreadUtil;
import com.yunbiao.yunbiaolocal.xmpp.XmppManager;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/1/30.
 */

public class CheckXmppOnline {
    private static final String TAG = "CheckXmppOnline";

    private Timer checkTimer;
    private XmppManager xmppManager;
    private static CheckXmppOnline checkXmppOnline;

    public static CheckXmppOnline getInstance(XmppManager xmppManager) {
        if (checkXmppOnline == null) {
            checkXmppOnline = new CheckXmppOnline(xmppManager);
        }
        return checkXmppOnline;
    }

    private CheckXmppOnline(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    public void start() {
        ThreadUtil.getInstance().runInUIThread(uploadThreadRun);
    }

    /**
     * 设备在后台是否在线
     */
    private Runnable uploadThreadRun = new Runnable() {
        public void run() {
            checkTimer = new Timer();
            checkTimer.schedule(new checkTimerTask(), 30 * 60 * 1000, 30 * 60 * 1000); // s后执行task，经过30 * 60 * 1000秒再次执行
        }
    };

    private class checkTimerTask extends TimerTask {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            timerHandler.sendMessage(message);
        }
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Map<String, String> map = new HashMap<>();
                map.put("deviceNo", HeartBeatClient.getDeviceNo());

                NetUtil.getInstance().post(ResConstants.DEVICE_ONLINE_STATUS, map, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            if (!response.equals("faile")) {
                                JSONObject mesJson = new JSONObject(response);
                                int deviceStatus = mesJson.getInt("status");
                                if (deviceStatus != 1) {
                                    xmppManager.startReconnectionThread();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            super.handleMessage(msg);
        }
    };

    public void cancel() {
        if (checkTimer != null) {
            checkTimer.cancel();
        }
    }
}
