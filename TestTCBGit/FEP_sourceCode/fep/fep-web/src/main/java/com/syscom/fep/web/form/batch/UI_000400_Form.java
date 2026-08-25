package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

import java.util.Date;

public class UI_000400_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String twsTaskname;				//批次名稱
	private String batchStartDate;

	public String getBatchStartDate() {
		return batchStartDate;
	}

	public void setBatchStartDate(String batchStartDate) {
		this.batchStartDate = batchStartDate;
	}

	public String getTwsTaskname() {
		return twsTaskname;
	}

	public void setTwsTaskname(String twsTaskname) {
		this.twsTaskname = twsTaskname;
	}
}
