package com.yunbiao.yunbiaolocal.netcore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.act.MainActivity;

/**
 * Created by Administrator on 2017/3/31.
 */

public class NetChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            MainActivity mainActivity = APP.getMainActivity();
            if (mNetworkInfo != null) {//有网
                if (mainActivity.xmppConnListener != null) {
                    mainActivity.xmppConnListener.OnnetChange(true);
                }
            } else {//没网
                if (mainActivity.xmppConnListener != null) {
                    mainActivity.xmppConnListener.OnnetChange(false);
                }
            }
        }

    }
}
