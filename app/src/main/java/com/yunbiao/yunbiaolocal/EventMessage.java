package com.yunbiao.yunbiaolocal;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2018/11/30.
 */

public class EventMessage {
    private int controlType;
    private String consoleMsg;

    public int getControlType() {
        return controlType;
    }

    public void setControlType(int controlType) {
        this.controlType = controlType;
    }

    public String getConsoleMsg() {
        return consoleMsg;
    }

    public void setConsoleMsg(String consoleMsg) {
        this.consoleMsg = consoleMsg;
    }

    public static void sendMsg(int ctlType,String ctlMsg){
        EventMessage eventMessage = new EventMessage();
        eventMessage.setConsoleMsg(ctlMsg);
        eventMessage.setControlType(ctlType);
        EventBus.getDefault().post(eventMessage);
    }
}
