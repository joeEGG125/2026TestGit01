package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060010_FormMain extends BaseForm{
	private static final long serialVersionUID = 1L;

	/**
	 * 分行
	 */
	private String branchNameCTxt;

	/**
	 * ATM代號
	 */
	private String atmAtmNoTxt;

	/**
	 * 機型
	 */
	private String atmModelnoTxt;

	/**
	 * IP位置
	 */
	private String atmIpTxt;

	/**
	 * 憑證版本
	 */
	private String atmCertaliasTxt;

	public String getBranchNameCTxt() {
		return branchNameCTxt;
	}

	public void setBranchNameCTxt(String branchNameCTxt) {
		this.branchNameCTxt = branchNameCTxt;
	}

	public String getAtmAtmNoTxt() {
		return atmAtmNoTxt;
	}

	public void setAtmAtmNoTxt(String atmAtmNoTxt) {
		this.atmAtmNoTxt = atmAtmNoTxt;
	}

	public String getAtmModelnoTxt() {
		return atmModelnoTxt;
	}

	public void setAtmModelnoTxt(String atmModelnoTxt) {
		this.atmModelnoTxt = atmModelnoTxt;
	}

	public String getAtmIpTxt() {
		return atmIpTxt;
	}

	public void setAtmIpTxt(String atmIpTxt) {
		this.atmIpTxt = atmIpTxt;
	}

	public String getAtmCertaliasTxt() {
		return atmCertaliasTxt;
	}

	public void setAtmCertaliasTxt(String atmCertaliasTxt) {
		this.atmCertaliasTxt = atmCertaliasTxt;
	}
}
