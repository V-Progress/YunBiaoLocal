package com.yunbiao.cccm.yunbiaolocal.sd;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2019/2/27.
 */

public interface SDController {

    String APP_ROOT_DIR = "yunbiao";


    /***
     * 初始化SD卡路径
     * @param pathOrUri SD卡根路径
     * @return SD卡是否可用
     */
    boolean init(@NonNull Context context, @NonNull String pathOrUri);

    boolean isSDCanUsed();

    <T> T getAppRootDir();

    <T> T getSDRootDir();
}
