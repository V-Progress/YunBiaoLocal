/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunbiao.cccm.xmpp;

import android.util.Log;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.activity.MainActivity;

import org.jivesoftware.smack.ConnectionListener;

/**
 * A listener class for monitoring connection closing and reconnection events.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class PersistentConnectionListener implements ConnectionListener {

    private static final String LOGTAG = LogUtil.makeLogTag(PersistentConnectionListener.class);

    private XmppManager xmppManager;
    private final MainActivity mainActivity;

    public PersistentConnectionListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
        mainActivity = APP.getMainActivity();
    }

    @Override
    public void connectionClosed() {
        Log.d(LOGTAG, "connectionClosed()...");
        if (mainActivity.xmppConnListener != null) {
            mainActivity.xmppConnListener.OndeviceIsOnline(false);
            mainActivity.xmppConnListener.onConnClosed();
        }

        xmppManager.startReconnectionThread();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(LOGTAG, "connectionClosedOnError()...");
        if (mainActivity.xmppConnListener != null) {
            mainActivity.xmppConnListener.OndeviceIsOnline(false);
            mainActivity.xmppConnListener.onConnError();

        }

        if (xmppManager.getConnection() != null && xmppManager.getConnection().isConnected()) {
            xmppManager.getConnection().disconnect();
        }
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.d(LOGTAG, "reconnectingIn()...");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(LOGTAG, "reconnectionFailed()...");
        xmppManager.startReconnectionThread();
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(LOGTAG, "reconnectionSuccessful()...");
        if (mainActivity.xmppConnListener != null) {
            mainActivity.xmppConnListener.OndeviceIsOnline(true);
            mainActivity.xmppConnListener.onConnected();
        }
    }

}
