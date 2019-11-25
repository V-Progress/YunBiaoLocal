package com.yunbiao.cccm.net2.common;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class YunBiaoException extends Exception {

    public static final int SUCCESS = 0;//下载成功
    public static final int FAILED_REQ_CONFIG = 1;//请求config失败
    public static final int FAILED_DOWNLOAD_CONFIG = 2;//下载config失败
    public static final int FAILED_RESOLVE_CONFIG = 3;//解析config失败
    public static final int FAILED_CONTENT_LENGTH = 4;//获取下载文件大小失败
    public static final int ERROR_DOWNLOAD_NET_EXCEPTION = 5;//下载网络超时
    public static final int ERROR_FILE_PERMISSION = 6;//文件权限错误
    public static final int ERROR_STORAGE = 7;//存储设备被移除

    public static final int ERROR_COMMON = 100;//通用异常

    private static Map<Integer, String> errMsgMap = new HashMap<>();

    static {
        errMsgMap.put(SUCCESS, "成功");
        errMsgMap.put(FAILED_REQ_CONFIG, "请求config文件失败");
        errMsgMap.put(FAILED_DOWNLOAD_CONFIG, "下载config文件失败");
        errMsgMap.put(FAILED_RESOLVE_CONFIG, "解析config文件失败");
        errMsgMap.put(FAILED_CONTENT_LENGTH, "获取远程文件大小失败");
        errMsgMap.put(ERROR_DOWNLOAD_NET_EXCEPTION, "下载超时/网络异常");
        errMsgMap.put(ERROR_FILE_PERMISSION, "文件权限错误，请检查文件写入权限");
        errMsgMap.put(ERROR_STORAGE, "存储设备异常，请检查SD卡是否可用");
        errMsgMap.put(ERROR_COMMON, "设备出现异常，请检查日志文件");
    }

    private static int currErrCode = ERROR_COMMON;
    private static Exception currException = null;

    public YunBiaoException(int errCode, Exception e) {
        currErrCode = errCode;
        currException = e;
    }

    @Override
    public String getMessage() {
        boolean isUnknownCode = true;
        for (Map.Entry<Integer, String> integerStringEntry : errMsgMap.entrySet()) {
            Integer key = integerStringEntry.getKey();
            if (key == currErrCode) {
                isUnknownCode = false;
            }
        }
        if (isUnknownCode) {
            currErrCode = ERROR_COMMON;
        }

        String exception = "";
        if (currException != null) {
            if (!TextUtils.isEmpty(currException.getMessage())) {
                exception = ":" + currException.getMessage();
            } else {
                exception = ":" + currException.getClass().getSimpleName();
            }
        }
        return errMsgMap.get(currErrCode) + exception;
    }

    public int getErrCode() {
        return currErrCode;
    }
}
