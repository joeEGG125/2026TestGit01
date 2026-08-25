package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000300_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String batchName;				//批次名稱
    private String batchStartDate;			//批次啟動日期
    private String batchShortName;			//批次簡稱
    private String subsys;					//系統別
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public String getBatchStartDate() {
		return batchStartDate;
	}
	public void setBatchStartDate(String batchStartDate) {
		this.batchStartDate = batchStartDate;
	}
	public String getBatchShortName() {
		return batchShortName;
	}
	public void setBatchShortName(String batchShortName) {
		this.batchShortName = batchShortName;
	}
	public String getSubsys() {
		return subsys;
	}
	public void setSubsys(String subsys) {
		this.subsys = subsys;
	}
}
