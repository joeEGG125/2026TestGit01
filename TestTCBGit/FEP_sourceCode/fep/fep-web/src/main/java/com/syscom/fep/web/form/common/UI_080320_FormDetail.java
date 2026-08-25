package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;

public class UI_080320_FormDetail extends BaseForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private String fepuserLogonid;
    
    private String fepuserName;
    private String fepUserGroup;
    private String fepuserBrno;
    private String fepuserStatus;
    private String fepuserLevel;
    private String fepuserBossTxt;
    private String updateUseridTxt;
    private String userUpdateTimeTxt;
	public String getFepuserLogonid() {
		return fepuserLogonid;
	}
	public void setFepuserLogonid(String fepuserLogonid) {
		this.fepuserLogonid = fepuserLogonid;
	}
	public String getFepuserName() {
		return fepuserName;
	}
	public void setFepuserName(String fepuserName) {
		this.fepuserName = fepuserName;
	}
	public String getFepUserGroup() {
		return fepUserGroup;
	}
	public void setFepUserGroup(String fepUserGroup) {
		this.fepUserGroup = fepUserGroup;
	}
	public String getFepuserBrno() {
		return fepuserBrno;
	}
	public void setFepuserBrno(String fepuserBrno) {
		this.fepuserBrno = fepuserBrno;
	}
	public String getFepuserStatus() {
		return fepuserStatus;
	}
	public void setFepuserStatus(String fepuserStatus) {
		this.fepuserStatus = fepuserStatus;
	}
	public String getFepuserLevel() {
		return fepuserLevel;
	}
	public void setFepuserLevel(String fepuserLevel) {
		this.fepuserLevel = fepuserLevel;
	}
	public String getFepuserBossTxt() {
		return fepuserBossTxt;
	}
	public void setFepuserBossTxt(String fepuserBossTxt) {
		this.fepuserBossTxt = fepuserBossTxt;
	}
	public String getUpdateUseridTxt() {
		return updateUseridTxt;
	}
	public void setUpdateUseridTxt(String updateUseridTxt) {
		this.updateUseridTxt = updateUseridTxt;
	}
	public String getUserUpdateTimeTxt() {
		return userUpdateTimeTxt;
	}
	public void setUserUpdateTimeTxt(String userUpdateTimeTxt) {
		this.userUpdateTimeTxt = userUpdateTimeTxt;
	}
}
