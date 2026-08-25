package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;

public class UI_080310_FormMain extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 群組名稱
	 */
	private String fepgroupGroupId;
	
    /**
     * 群組代號
     */
	private String fepgroupName;

	public String getFepgroupGroupId() {
		return fepgroupGroupId;
	}

	public void setFepgroupGroupId(String fepgroupGroupId) {
		this.fepgroupGroupId = fepgroupGroupId;
	}

	public String getFepgroupName() {
		return fepgroupName;
	}

	public void setFepgroupName(String fepgroupName) {
		this.fepgroupName = fepgroupName;
	}

}
