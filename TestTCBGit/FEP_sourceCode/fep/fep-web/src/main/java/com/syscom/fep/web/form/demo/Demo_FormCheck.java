package com.syscom.fep.web.form.demo;

import com.syscom.fep.web.form.BaseForm;

public class Demo_FormCheck extends BaseForm {
	private static final long serialVersionUID = 1L;

	private String feptxnTxDate;
	private Long feptxnEjfno;

	public String getFeptxnTxDate() {
		return feptxnTxDate;
	}

	public void setFeptxnTxDate(String feptxnTxDate) {
		this.feptxnTxDate = feptxnTxDate;
	}

	public Long getFeptxnEjfno() {
		return feptxnEjfno;
	}

	public void setFeptxnEjfno(Long feptxnEjfno) {
		this.feptxnEjfno = feptxnEjfno;
	}
}
