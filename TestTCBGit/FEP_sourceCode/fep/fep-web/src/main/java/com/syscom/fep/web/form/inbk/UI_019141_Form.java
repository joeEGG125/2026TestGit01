package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019141_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String txDate;
	private String txTroutActno;
	private String txTrinBkno;
	private String txTrinActno;
	private String txtTxAmt;
	private String txID;

	public String getTxDate() {
		return txDate;
	}

	public void setTxDate(String txDate) {
		this.txDate = txDate;
	}

	public String getTxTroutActno() {
		return txTroutActno;
	}

	public void setTxTroutActno(String txTroutActno) {
		this.txTroutActno = txTroutActno;
	}

	public String getTxTrinBkno() {
		return txTrinBkno;
	}

	public void setTxTrinBkno(String txTrinBkno) {
		this.txTrinBkno = txTrinBkno;
	}

	public String getTxTrinActno() {
		return txTrinActno;
	}

	public void setTxTrinActno(String txTrinActno) {
		this.txTrinActno = txTrinActno;
	}

	public String getTxtTxAmt() {
		return txtTxAmt;
	}

	public void setTxtTxAmt(String txtTxAmt) {
		this.txtTxAmt = txtTxAmt;
	}

	public String getTxID() {
		return txID;
	}

	public void setTxID(String txID) {
		this.txID = txID;
	}
}
