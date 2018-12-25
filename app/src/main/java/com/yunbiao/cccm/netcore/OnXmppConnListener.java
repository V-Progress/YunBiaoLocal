package com.yunbiao.cccm.netcore;

/**
 * Created by Administrator on 2018/12/13.
 */

public interface OnXmppConnListener {
    void onConnecting();
    void onConnected();
    void onConnError();
    void onReConnecting();
    void onNetChange(boolean isConnect);
    void onLogon(String sn, String pwd, String status, String deviceQrCode);
    void onConnClosed();

    void Onreceived(String sn, String pwd, String status, String deviceQrCode);

    void OnreceivedDtype(Integer dtype);

    void OndeviceIsOnline(boolean isOnline);

    void OnnetChange(boolean isConnect);
}
