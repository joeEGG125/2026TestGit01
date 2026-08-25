package com.syscom.fep.mybatis.ems.vo;

import java.io.Serializable;

/**
 * Mybatis的module類都需要繼承BaseModel
 * 
 * @author Richard
 */
public abstract class BaseModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 異動人員
	 */
	private long updateUser = 0L;
	/**
	 * 是否記錄AuditTrail
	 */
	private boolean logAuditTrail = false;
	
	public abstract String fieldsToXml();

	public long getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(long updateUser) {
		this.updateUser = updateUser;
	}

	public boolean isLogAuditTrail() {
		return logAuditTrail;
	}

	public void setLogAuditTrail(boolean logAuditTrail) {
		this.logAuditTrail = logAuditTrail;
	}
}
