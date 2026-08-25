package com.syscom.fep.web.form.dbmaintain;


import com.syscom.fep.web.form.BaseForm;

public class UI_070070_Form extends BaseForm {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 交易類別
	 */
	private String uiTxType1;
	
	/**
	 * 業務分類
	 */
	private String uiTxType2;
	
	/**
	 * 交易處理模式
	 */
	private String uiProcType;
	
	/**
	 * 交易訊息
	 */
	private String msgctlMsgid;

	public String getUiTxType1() {
		return uiTxType1;
	}

	public void setUiTxType1(String uiTxType1) {
		this.uiTxType1 = uiTxType1;
	}

	public String getUiTxType2() {
		return uiTxType2;
	}

	public void setUiTxType2(String uiTxType2) {
		this.uiTxType2 = uiTxType2;
	}

	public String getUiProcType() {
		return uiProcType;
	}

	public void setUiProcType(String uiProcType) {
		this.uiProcType = uiProcType;
	}

	public String getMsgctlMsgid() {
		return msgctlMsgid;
	}

	public void setMsgctlMsgid(String msgctlMsgid) {
		this.msgctlMsgid = msgctlMsgid;
	}
	
}
