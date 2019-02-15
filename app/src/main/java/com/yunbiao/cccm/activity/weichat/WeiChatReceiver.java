package com.yunbiao.cccm.activity.weichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.yunbiao.cccm.common.utils.LogUtil;
import com.yunbiao.cccm.xmpp.Constants;

import org.json.JSONObject;

public class WeiChatReceiver extends BroadcastReceiver {
    private static final String TAG = "WeiChatReceiver";
    private OnWeChatMsgListener msgListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String notifiTitleString = intent.getStringExtra(Constants.NOTIFICATION_TITLE);

        if (!TextUtils.isEmpty(notifiTitleString) && notifiTitleString.equals("weixin")) {
            String notificationMessage = intent.getStringExtra(Constants.NOTIFICATION_MESSAGE);
            LogUtil.E("weiChatReceive: " + notificationMessage);
            try {
                JSONObject jsonObject = new JSONObject(notificationMessage);
                if (!jsonObject.isNull("type")) {
                    if (msgListener != null) {
                        msgListener.onMsgReceived(notificationMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnWeChatMsgListener{
        void onMsgReceived(String notificationMessage);
    }


    public void setMsgListener(OnWeChatMsgListener onWeChatMsgListener){
        msgListener = onWeChatMsgListener;
    }
}

