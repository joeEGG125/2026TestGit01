package com.syscom.fep.web.form.dbmaintain;

import com.syscom.fep.web.form.BaseForm;

public class UI_070060_FormMain extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 畫面按鈕由系統判斷是否要顯示
	 */
	private String webType;
	
	/**
	 * 來源通道(查詢條件)
	 */
	private String msgfileChannel;
	
	/**
	 * 訊息代碼(查詢條件)
	 */
	private String msgfileErrorcode;
	
	/**
	 * 子系統(查詢條件)
	 */
	private String msgfileSubsys;
	
	/**
	 * 訊息嚴重性(查詢條件)
	 */
	private String msgfileSeverity;
	
	/**
	 * 訊息簡述(查詢條件)
	 */
	private String msgfileShortmsg;

	public String getWebType() {
		return webType;
	}

	public void setWebType(String webType) {
		this.webType = webType;
	}

	public String getMsgfileChannel() {
		return msgfileChannel;
	}

	public void setMsgfileChannel(String msgfileChannel) {
		this.msgfileChannel = msgfileChannel;
	}

	public String getMsgfileErrorcode() {
		return msgfileErrorcode;
	}

	public void setMsgfileErrorcode(String msgfileErrorcode) {
		this.msgfileErrorcode = msgfileErrorcode;
	}

	public String getMsgfileSubsys() {
		return msgfileSubsys;
	}

	public void setMsgfileSubsys(String msgfileSubsys) {
		this.msgfileSubsys = msgfileSubsys;
	}

	public String getMsgfileSeverity() {
		return msgfileSeverity;
	}

	public void setMsgfileSeverity(String msgfileSeverity) {
		this.msgfileSeverity = msgfileSeverity;
	}

	public String getMsgfileShortmsg() {
		return msgfileShortmsg;
	}

	public void setMsgfileShortmsg(String msgfileShortmsg) {
		this.msgfileShortmsg = msgfileShortmsg;
	}

}
