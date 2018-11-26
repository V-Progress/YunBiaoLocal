package com.yunbiao.yunbiaolocal.viewfactory.bean;

public class WeiDetail {
	
	private String ticket;//微信二维码id
	private Integer fontSize;//文字大小
	private String fontColor;//文字颜色
	private String fontFamily;//文字字体
	private String background;//背景颜色
	private String msgShowType;
	private String msgSize;
	private String msgSource;
	private Boolean isMeet;
	
	public String getMsgShowType() {
		return msgShowType;
	}
	public void setMsgShowType(String msgShowType) {
		this.msgShowType = msgShowType;
	}
	public String getMsgSize() {
		return msgSize;
	}
	public void setMsgSize(String msgSize) {
		this.msgSize = msgSize;
	}
	public String getMsgSource() {
		return msgSource;
	}
	public void setMsgSource(String msgSource) {
		this.msgSource = msgSource;
	}
	public Boolean getIsMeet() {
		return isMeet;
	}
	public void setIsMeet(Boolean isMeet) {
		this.isMeet = isMeet;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public Integer getFontSize() {
		return fontSize;
	}
	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	public String getFontFamily() {
		return fontFamily;
	}
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	
}

