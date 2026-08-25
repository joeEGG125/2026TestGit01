package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019020_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String feptxnTxDate;
	private Integer feptxnEjfno;

	public String getFeptxnTxDate() {
		return feptxnTxDate;
	}

	public void setFeptxnTxDate(String feptxnTxDate) {
		this.feptxnTxDate = feptxnTxDate;
	}

	public Integer getFeptxnEjfno() {
		return feptxnEjfno;
	}

	public void setFeptxnEjfno(Integer feptxnEjfno) {
		this.feptxnEjfno = feptxnEjfno;
	}
}
