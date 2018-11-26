package com.yunbiao.yunbiaolocal.core;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 核心类：Xmpp消息处理
 * 分发后台消息
 * Created by ZhangWei on 2018/11/26.
 */

public class XmppMessageProcessor {
    private static final String TAG = "XmppMessageProcessor";

    private static final int ONLINE_TYPE = 1;// 上线
    private static final int CONTENT_TYPE = 2;// 内容修改
    private static final int VOICE_TYPE = 3;// 声音
    private static final int CUTSCREN_TYPE = 4;// 截屏
    private static final int RUNSET_TYPE = 5;// 设备开关机设置
    private static final int SHOW_SERNUM = 6;// 显示设备编号
    private static final int SHOW_VERSION = 7;// 显示版本号
    private static final int SHOW_DISK_IFNO = 8;// 获取磁盘容量
    private static final int POWER_RELOAD = 9;// 设备 开机 重启
    private static final int PUSH_TO_UPDATE = 10;//软件升级
    private final static int HARDWARE_UPDATE = 11;//通知设备硬件更新,上传设备信息
    private final static int HARDWARESCREENROTATE_UPDATE = 12;//屏幕旋转
    private final static int SET_CLEAR_LAYOUT = 13;//一键删除布局
    private final static int PUSH_MESSAGE = 14;//推送广告消息，快发字幕
    private final static int REFERSH_RENEWAL_STATUS = 15;//欠费停机设备支付
    private final static int CHANNEL_TYPE = 16;//输入源选择
    private final static int PUSH_IMAGE = 17;//手机端快发图片
    private final static int FACE_DETECT = 18;//开通人脸识别
    private final static int EARTH_CINEMA = 19;//大地影院
    private final static int UNICOM_SCREEN = 20;//联屏
    private final static int IMAGE_PUSH = 21;//推送的图片
    private final static int VIDEO_PUSH = 22;//推送的视频
    private final static int ADSINFO_PUSH = 23;//推送的自运营广告
    private final static int SHARESTATUS_UPDATE = 24;//是否是广告机状态更改

    /**
     * 消息分发
     */
    public static void dispatchMsg(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            Integer type = jsonObject.getInt("type");
            switch (type.intValue()) {
                case ONLINE_TYPE:
                    break;
                case CONTENT_TYPE:// 内容更新
                    break;
                case VOICE_TYPE:// 声音控制
                    break;
                case CUTSCREN_TYPE:
                    break;
                case RUNSET_TYPE:
                    break;
                case SHOW_SERNUM:
                    break;
                case SHOW_VERSION:// 版本信息
                    break;
                case SHOW_DISK_IFNO:
                    break;
                case POWER_RELOAD:// 机器重启
                    break;
                case PUSH_TO_UPDATE:
                    break;
                case HARDWARE_UPDATE:
                    break;
                case HARDWARESCREENROTATE_UPDATE://屏幕旋转
                    break;
                case SET_CLEAR_LAYOUT:
                    break;
                case PUSH_MESSAGE:
                    break;
                case REFERSH_RENEWAL_STATUS://欠费停机设备支付
                    break;
                case CHANNEL_TYPE://输入信号源选择
                    break;
                case PUSH_IMAGE:
                    break;
                case FACE_DETECT:
                    break;
                case EARTH_CINEMA:
                    break;
                case IMAGE_PUSH:
                    break;
                case VIDEO_PUSH:
                    break;
                case UNICOM_SCREEN:
                    break;

                case ADSINFO_PUSH://自运营广告推送
                    break;
                case SHARESTATUS_UPDATE://是否是广告机状态更改
                    break;

                default:
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
