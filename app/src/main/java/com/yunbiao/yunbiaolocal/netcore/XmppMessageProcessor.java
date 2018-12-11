package com.yunbiao.yunbiaolocal.netcore;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.yunbiaolocal.cache.CacheManager;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.devicectrl.ScreenShot;
import com.yunbiao.yunbiaolocal.devicectrl.actions.XBHActions;
import com.yunbiao.yunbiaolocal.devicectrl.power.PowerOffTool;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.devicectrl.SoundControl;
import com.yunbiao.yunbiaolocal.netcore.bean.ChannelBean;
import com.yunbiao.yunbiaolocal.netcore.bean.DiskInfoBean;
import com.yunbiao.yunbiaolocal.netcore.bean.LoginModel;
import com.yunbiao.yunbiaolocal.netcore.bean.PowerCtrlBean;
import com.yunbiao.yunbiaolocal.netcore.bean.SerNumBean;
import com.yunbiao.yunbiaolocal.netcore.bean.VoiceModel;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;
import com.yunbiao.yunbiaolocal.utils.DialogUtil;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.yunbiao.yunbiaolocal.utils.SystemInfoUtil;
import com.yunbiao.yunbiaolocal.utils.ThreadUtil;
import com.yunbiao.yunbiaolocal.view.TipToast;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;

import okhttp3.Call;

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
        JSONObject jsonObject = JSON.parseObject(message);
        final String content = jsonObject.getString("content");
        String type = jsonObject.getString("type");

        switch (Integer.valueOf(type)) {
            case ONLINE_TYPE://登录
                MachineDetial.getInstance().upLoadHardWareMessage();
                LoginModel loginModel = new Gson().fromJson(content, LoginModel.class);
                LogUtil.E(loginModel.toString());
                CacheManager.putExpireDate(loginModel.getExpireDate());
                CacheManager.putBindStatus(loginModel.getBindStatus());
                CacheManager.putRunStatus(loginModel.getRunStatus());
                CacheManager.putSerNumber(loginModel.getSerNum());
                CacheManager.putPwd(loginModel.getPwd());
                CacheManager.putDecName(loginModel.getDeviceName());
                CacheManager.putDeviceQrCode(loginModel.getDeviceQrCode());
                CacheManager.putIsMirror(loginModel.getIsMirror());

                //是否有密码
                String password = loginModel.getPassword();
                LogUtil.E(TAG, "*****" + password);
                if (TextUtils.isEmpty(password) || password.equals(" ") || password.equals("null")) {
                } else {
                }


                break;
            case CONTENT_TYPE:
                HashMap<String,String> params = new HashMap<>();
                params.put("deviceId",HeartBeatClient.getDeviceNo());
                NetUtil.getInstance().post(ResourceConst.REMOTE_RES.RESOURCE_URL, params, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        // 没有返回，或者请求错误
                        if (response.startsWith("\"")) {
                            response = response.substring(1, response.length() - 1);
                        }
                        if (!response.equals("null") && !response.equals("faile") && !response.equals("[]")) {
                            Log.e(TAG, "onSuccess: result=="+response);

                        }
                    }
                });



                break;
            case RUNSET_TYPE://设备自动开关机
                ThreadUtil.getInstance().runInCommonThread(new Runnable() {
                    @Override
                    public void run() {// 开关机时间设置
                        PowerOffTool.getInstance().getPowerOffTime(HeartBeatClient.getDeviceNo());
                    }
                });
                break;
            case SHOW_SERNUM:// 显示设备编号
                SerNumBean serNumBean = new Gson().fromJson(content, SerNumBean.class);
                Integer showType = serNumBean.getShowType();
                LogUtil.E(APP.getContext().getClass().getSimpleName(), "showType = " + showType);
                if (showType != null && showType == 0) {//状态栏  视美泰主板
                    APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);

                    Integer showValue = serNumBean.getShowValue();
                    if (showValue == 0) {//显示
                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
                    } else if (showValue == 1) {//隐藏
                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
                    }
                } else {
                    TipToast.showLongToast(APP.getMainActivity(), "设备编号：" + CacheManager.getSerNumber());
                }
                break;
            case CUTSCREN_TYPE:
                ThreadUtil.getInstance().runInCommonThread(new Runnable() {
                    @Override
                    public void run() {
                        ScreenShot.getInstanse().ss();
                    }
                });

                break;
            case SHOW_VERSION://显示版本号
                SystemInfoUtil.uploadAppVersion();
                break;
            case SHOW_DISK_IFNO://显示存储信息
                DiskInfoBean diskInfoBean = new Gson().fromJson(content, DiskInfoBean.class);
                Integer flag = diskInfoBean.getFlag();
                if (flag != null) {
                    if (flag == 0) { //显示
                        SystemInfoUtil.uploadDiskInfo();
                    } else if (flag == 1) {// 清理磁盘
                        SystemInfoUtil.deleteOtherFile();
                        SystemInfoUtil.uploadDiskInfo();
                    }
                }
                break;
            case POWER_RELOAD://关机重启
                PowerCtrlBean powerCtrlBean = new Gson().fromJson(content, PowerCtrlBean.class);
                if (powerCtrlBean.getRestart() == 0) {
                    PowerOffTool.getInstance().shutdown();
                }else{
                    PowerOffTool.getInstance().reboot();
                }
                break;
            case CHANNEL_TYPE://输入信号源选择
                ChannelBean channelBean = new Gson().fromJson(content, ChannelBean.class);
                Integer broadType = CommonUtils.getBroadType();
                if (broadType == 4) {//判断是不是小百合
                    Intent intent = new Intent();
                    switch (channelBean.getChannel()) {
                        case 0:
                            intent.setAction(XBHActions.CHANGE_TO_AV);
                            break;
                        case 1:
                            intent.setAction(XBHActions.CHANGE_TO_VGA);
                            break;
                        case 2:
                            intent.setAction(XBHActions.CHANGE_TO_HDMI);
                            break;
                    }
                    APP.getContext().sendBroadcast(intent);
                } else {
                    TipToast.showLongToast(APP.getContext(), "暂不支持该功能");
                }

                break;
            case PUSH_TO_UPDATE://检查更新
                SystemInfoUtil.checkUpdateInfo();
                break;
            case VOICE_TYPE://声音修改
                VoiceModel voiceModel = new Gson().fromJson(content, VoiceModel.class);
                SoundControl.setMusicSound(voiceModel.getVoice());
                break;
            case PUSH_MESSAGE://插播字幕
                LogUtil.E("显示字幕");
                DialogUtil.showInsertDialog(APP.getMainActivity(),DialogUtil.INSERT_TEXT, content);
                break;
            case VIDEO_PUSH://插播视频
                DialogUtil.showInsertDialog(APP.getMainActivity(),DialogUtil.INSERT_VIDEO, content);
                break;
        }


//        MsgModel msgModel = new Gson().fromJson(message, MsgModel.class);
//        String content = msgModel.getContent();
//        switch (msgModel.getType()) {
//            case ONLINE_TYPE:
//                LoginModel loginModel = new Gson().fromJson(content, LoginModel.class);
//                LogUtil.E(loginModel.toString());
//
//
//                break;
//            case CONTENT_TYPE:// 内容更新
//                break;
//            case VOICE_TYPE:// 声音控制
////                SoundControl.setMusicSound(content.getVoice());
//                break;
//            case CUTSCREN_TYPE://截屏
////                ThreadUitls.runInThread(new Runnable() {// 截图控制
////                    @Override
////                    public void run() {
////                        ScreenShot.getInstanse().shootScreen();
////                    }
////                });
//                break;
//            case RUNSET_TYPE://获取开关机时间
//
//
//                break;
//            case SHOW_SERNUM://设备号
////                Integer showType = content.getShowType();
////                Log.e(APP.getContext().getClass().getSimpleName(),"showType = "+showType);
////                if (showType != null && showType == 0) {//状态栏  视美泰主板
////                    APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
////
//////                    Integer showValue = (Integer) TYTool.getJsonObj(contentJson, "showValue", null);
//////                    if (showValue == 0) {//显示
//////                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
//////                    } else if (showValue == 1) {//隐藏
//////                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
//////                    }
////                } else { // 显示设备编号
////                    TipToast.showLongToast(APP.getMainActivity(),"设备编号："+CacheManager.getSerNumber());
////                }
//
//                break;
//            case SHOW_VERSION:// 版本信息
//                ResourceConst.uploadAppVersion();
//                break;
//            case SHOW_DISK_IFNO://磁盘容量
////                Integer flag = content.getFlag();
////                Log.e("123","磁盘容量："+flag);
////                if (flag != null) {
////                    if (flag == 0) { //显示
//////                        ResourceConst.uploadDiskInfo();
////                    } else if (flag == 1) {// 清理磁盘
//////                        ResourceConst.deleteOtherFile();
//////                        ResourceConst.uploadDiskInfo();
////                    }
////                }
//                break;
//            case POWER_RELOAD:// 机器重启
////                Integer restart = content.getRestart();
////                Log.e("123","机器重启："+restart);
////                if (restart != null) {
////                    if (restart == 0) {
//////                        ProgressDialog progressDialog = TYTool.coreInfoShow3sDialog();
//////                        progressDialog.setTitle("关机");
//////                        progressDialog.setMessage("3秒后将关闭设备");
//////                        progressDialog.show();
//////                        TYTool.powerShutDown.start();
////                    } else if (restart == 1) {
//////                        ProgressDialog progressDialog = TYTool.coreInfoShow3sDialog();
//////                        progressDialog.setTitle("重启");
//////                        progressDialog.setMessage("3秒后将重启设备");
//////                        progressDialog.show();
//////                        TYTool.restart.start();
////                    }
////                }
//
//                break;
//            case PUSH_TO_UPDATE://软件升级
//                break;
//            case HARDWARE_UPDATE://通知硬件设备更新
//                break;
//            case HARDWARESCREENROTATE_UPDATE://屏幕旋转
//                break;
//            case SET_CLEAR_LAYOUT:
//                break;
//            case PUSH_MESSAGE:
//
//
//                break;
//            case REFERSH_RENEWAL_STATUS://欠费停机设备支付
//                break;
//            case PUSH_IMAGE://手机快发
//                break;
//
//            /*case CHANNEL_TYPE://输入信号源选择
//                break;
//            case FACE_DETECT:
//                break;
//            case EARTH_CINEMA:
//                break;
//            case IMAGE_PUSH:
//                break;
//            case VIDEO_PUSH:
//                break;
//            case UNICOM_SCREEN:
//                break;
//
//            case ADSINFO_PUSH://自运营广告推送
//                break;
//            case SHARESTATUS_UPDATE://是否是广告机状态更改
//                break;*/
//            default:
//                break;
//        }

    }
}
