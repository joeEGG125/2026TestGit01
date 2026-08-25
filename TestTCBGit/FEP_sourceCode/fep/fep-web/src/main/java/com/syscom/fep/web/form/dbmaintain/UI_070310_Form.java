package com.syscom.fep.web.form.dbmaintain;

import com.syscom.fep.web.form.BaseForm;

public class UI_070310_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String binNo;
	private String binBkno;
	private String binNet;
	private String binZone;
	private String binOrg;
	private String binProd;
	private String btnType;
	private Short binEmvflag;


	public String getBinNo() {
		return binNo;
	}

	public void setBinNo(String binNo) {
		this.binNo = binNo;
	}

	public String getBinNet() {
		return binNet;
	}

	public void setBinNet(String binNet) {
		this.binNet = binNet;
	}

	public String getBinBkno() {
		return binBkno;
	}

	public void setBinBkno(String binBkno) {
		this.binBkno = binBkno;
	}

	public String getBinOrg() {
		return binOrg;
	}

	public void setBinOrg(String binOrg) {
		this.binOrg = binOrg;
	}

	public String getBinProd() {
		return binProd;
	}

	public void setBinProd(String binProd) {
		this.binProd = binProd;
	}

	public String getBinZone() {
		return binZone;
	}

	public void setBinZone(String binZone) {
		this.binZone = binZone;
	}

	public String getBtnType() {
		return btnType;
	}

	public void setBtnType(String btnType) {
		this.btnType = btnType;
	}

	public Short getBinEmvflag() {
		return binEmvflag;
	}

	public void setBinEmvflag(Short binEmvflag) {
		this.binEmvflag = binEmvflag;
	}
	
}
