package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060610_A_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;

	private Long logno;
	private String logdate;

	public Long getLogno() {
		return logno;
	}

	public void setLogno(Long logno) {
		this.logno = logno;
	}

	public String getLogdate() {
		return logdate;
	}

	public void setLogdate(String logdate) {
		this.logdate = logdate;
	}
}
