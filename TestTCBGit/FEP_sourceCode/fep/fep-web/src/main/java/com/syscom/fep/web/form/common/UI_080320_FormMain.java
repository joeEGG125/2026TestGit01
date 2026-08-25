package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;

public class UI_080320_FormMain extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 員工編號
	 */
	private String userTelId;
	
	/**
	 * 權限群組
	 */
	private String groupId;
	
	/**
	 * 分行代號
	 */
	private String atmbctl;

	public String getUserTelId() {
		return userTelId;
	}

	public void setUserTelId(String userTelId) {
		this.userTelId = userTelId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getAtmbctl() {
		return atmbctl;
	}

	public void setAtmbctl(String atmbctl) {
		this.atmbctl = atmbctl;
	}
}
