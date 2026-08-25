package com.syscom.fep.web.entity.common;

import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.model.Fepuser;

public class FepuserTmp extends Fepuser{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FepuserTmp() {}
    
	public FepuserTmp(Fepuser fepUser) {
		if (fepUser == null)
			return;
		BeanUtils.copyProperties(fepUser, this);
	}
	
	/**
	 * 權限群組 代號 + 中文
	 */
	private String fepUserGroupTxt;
	
	/**
	 * 分行代號 代號 + 中文
	 */
	private String fepuserBrnoTxt;
	
	/**
	 * 上次登錄日期 formate yyyy/mm/dd
	 */
	private String fepuserLuDateTxt;
	
	/**
	 * 上次登錄時間 formate hh:mm:ss
	 */
	private String fepuserLuTimeTxt;
	
	/**
	 * 上次修改人員 fepuserLogonid + fepuserName
	 */
	private String updateUseridTxt;
	
	/**
	 * 上次修改日期 formate yyyy/mm/dd hh:mm:ss
	 */
	private String userUpdateTimeTxt;
	
	/**
	 * 直屬查核人員 fepuserLogonid + fepuserName
	 */
	private String fepuserBossTxt;

	public String getFepuserBossTxt() {
		return fepuserBossTxt;
	}

	public void setFepuserBossTxt(String fepuserBossTxt) {
		this.fepuserBossTxt = fepuserBossTxt;
	}

	public String getFepUserGroupTxt() {
		return fepUserGroupTxt;
	}

	public void setFepUserGroupTxt(String fepUserGroupTxt) {
		this.fepUserGroupTxt = fepUserGroupTxt;
	}

	public String getFepuserBrnoTxt() {
		return fepuserBrnoTxt;
	}

	public void setFepuserBrnoTxt(String fepuserBrnoTxt) {
		this.fepuserBrnoTxt = fepuserBrnoTxt;
	}

	public String getFepuserLuDateTxt() {
		return fepuserLuDateTxt;
	}

	public void setFepuserLuDateTxt(String fepuserLuDateTxt) {
		this.fepuserLuDateTxt = fepuserLuDateTxt;
	}

	public String getFepuserLuTimeTxt() {
		return fepuserLuTimeTxt;
	}

	public void setFepuserLuTimeTxt(String fepuserLuTimeTxt) {
		this.fepuserLuTimeTxt = fepuserLuTimeTxt;
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
