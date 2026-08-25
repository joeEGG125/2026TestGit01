package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019060_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String apibatchTxDate;
	private String apibatchTotCnt;
	private String apibatchTotAmt;

	public String getApibatchTxDate() {
		return apibatchTxDate;
	}

	public void setApibatchTxDate(String apibatchTxDate) {
		this.apibatchTxDate = apibatchTxDate;
	}

	public String getApibatchTotCnt() {
		return apibatchTotCnt;
	}

	public void setApibatchTotCnt(String apibatchTotCnt) {
		this.apibatchTotCnt = apibatchTotCnt;
	}

	public String getApibatchTotAmt() {
		return apibatchTotAmt;
	}

	public void setApibatchTotAmt(String apibatchTotAmt) {
		this.apibatchTotAmt = apibatchTotAmt;
	}
}
