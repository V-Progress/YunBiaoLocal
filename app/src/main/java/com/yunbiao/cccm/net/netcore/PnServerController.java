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
package com.yunbiao.cccm.net.netcore;

import android.app.Activity;

import com.yunbiao.cccm.common.utils.ThreadUtil;
import com.yunbiao.cccm.xmpp.ServiceManager;

/**
 * This is an androidpn client demo application.
 * @author xiongcheng
 */
public class PnServerController {
	private static ServiceManager serverManager ;

	public static void startXMPP(final Activity mainActivity) {
		if (serverManager == null) {
			ThreadUtil.getInstance().runInCommonThread(new Runnable() {
				@Override
				public void run() {
					serverManager = new ServiceManager(mainActivity);
					serverManager.startService();
				}
			});
		}
	}

	public static void stopXMPP() {
		if (serverManager != null) {
			serverManager.stopService();
			serverManager = null;
		}
	}
}