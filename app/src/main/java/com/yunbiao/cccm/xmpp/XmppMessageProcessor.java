package com.yunbiao.cccm.xmpp;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.yunbiao.cccm.net2.DataLoader;
import com.yunbiao.cccm.net2.activity.MenuActivity;
import com.yunbiao.cccm.net2.SubtitleLoader;
import com.yunbiao.cccm.net2.utils.NetUtil;
import com.yunbiao.cccm.net2.InsertLoader;
import com.yunbiao.cccm.net2.activity.MainController;
import com.yunbiao.cccm.net2.cache.CacheManager;
import com.yunbiao.cccm.net2.common.HeartBeatClient;
import com.yunbiao.cccm.net2.control.ScreenShot;
import com.yunbiao.cccm.net2.control.PowerOffTool;
import com.yunbiao.cccm.net2.control.actions.XBHActions;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.control.SoundControl;
import com.yunbiao.cccm.net2.model.ChannelBean;
import com.yunbiao.cccm.net2.model.DiskInfoBean;
import com.yunbiao.cccm.net2.model.LoginModel;
import com.yunbiao.cccm.net2.model.PowerCtrlBean;
import com.yunbiao.cccm.net2.model.SerNumBean;
import com.yunbiao.cccm.net2.model.VoiceModel;
import com.yunbiao.cccm.net2.utils.CommonUtils;
import com.yunbiao.cccm.net2.log.LogUtil;
import com.yunbiao.cccm.net2.utils.SystemInfoUtil;
import com.yunbiao.cccm.net2.utils.ThreadUtil;
import com.yunbiao.cccm.net2.view.TipToast;
import com.yunbiao.cccm.net2.model.InsertTextModel;

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

    private final static int SET_CLEAR_LAYOUT = 13;//一键删除布局
    private final static int PUSH_MESSAGE = 14;//推送广告消息，快发字幕
    private final static int CHANNEL_TYPE = 16;//输入源选择
    private final static int INSERT_CONTENT_TYPE = 24;//推送的视频
    private final static int UPDATE_LAYER = 25;//推送的视频

    /**
     * 消息分发
     */
    public static void dispatchMsg(final String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        final String content = jsonObject.getString("content");
        String type = jsonObject.getString("type");
        int mode = CacheManager.SP.getMode();

        switch (Integer.valueOf(type)) {
            case ONLINE_TYPE://登录
                MenuActivity.isServerConnected = true;
                NetUtil.getInstance().upLoadHardWareMessage();

                LoginModel loginModel = new Gson().fromJson(content, LoginModel.class);
                CacheManager.SP.putDeviceName(loginModel.getDeviceName());//设备名称
                CacheManager.SP.putSettingPwd(loginModel.getPassword());
                CacheManager.SP.putAccessCode(loginModel.getPwd());//接入码
                CacheManager.SP.putDeviceNum(loginModel.getSerNum());//设备编号
                CacheManager.SP.putStatus(loginModel.getStatus());
                CacheManager.SP.putWechatTicket(loginModel.getTicket());
                CacheManager.SP.putLayerType(loginModel.getLayerType());

                //登录完成后更新菜单界面的编号
                MainController.getInstance().updateDeviceNo();

                if(CacheManager.SP.getMode() == 0){//网络模式下才更新标签
                    MainController.getInstance().updateLayerType(loginModel.getLayerType());
                }
                break;
            case CONTENT_TYPE://布局更新
            case SET_CLEAR_LAYOUT://清除布局
                if(mode == 0){
                    MainController.getInstance().stopPlay();//停止播放，清空播放列表
                    DataLoader.getInstance().get();//重新初始化布局内容
                }
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
                    TipToast.showLongToast(APP.getMainActivity(), "设备编号：" + CacheManager.SP.getDeviceNum());
                }
                break;
            case CUTSCREN_TYPE://截屏
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
                InsertTextModel insertTextModel = new Gson().fromJson(content, InsertTextModel.class);
                SubtitleLoader.instance().setTXT(insertTextModel);
                break;
            case INSERT_CONTENT_TYPE://插播视频
                if(mode == 0){
                    InsertLoader.getInstance().loadInsert();
                }
                break;
            case UPDATE_LAYER://更新层级标签
                JSONObject layerModel = JSON.parseObject(content);
                Integer layerType = layerModel.getInteger("layerType");
                CacheManager.SP.putLayerType(layerType);
                if(CacheManager.SP.getMode() == 0){//网络模式下才更新标签
                    MainController.getInstance().updateLayerType(layerType);
                }
                break;
        }
    }
}
