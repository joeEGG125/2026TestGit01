package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_015202_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String lblBankNo;
	private String clearDate;
	private String lblTxTime;
	private String lblBknoStan;
	private String apId5;
	private String payType;

	public String getLblBankNo() {
		return lblBankNo;
	}

	public void setLblBankNo(String lblBankNo) {
		this.lblBankNo = lblBankNo;
	}

	public String getClearDate() {
		return clearDate;
	}

	public void setClearDate(String clearDate) {
		this.clearDate = clearDate;
	}

	public String getLblTxTime() {
		return lblTxTime;
	}

	public void setLblTxTime(String lblTxTime) {
		this.lblTxTime = lblTxTime;
	}

	public String getLblBknoStan() {
		return lblBknoStan;
	}

	public void setLblBknoStan(String lblBknoStan) {
		this.lblBknoStan = lblBknoStan;
	}

	public String getApId5() {
		return apId5;
	}

	public void setApId5(String apId5) {
		this.apId5 = apId5;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
}
