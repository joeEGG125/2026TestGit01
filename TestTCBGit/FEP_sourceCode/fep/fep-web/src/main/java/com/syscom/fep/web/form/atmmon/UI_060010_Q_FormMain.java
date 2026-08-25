package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060010_Q_FormMain extends BaseForm {
	private static final long serialVersionUID = 1L;

	/**
	 * 分行
	 */
	private String branchNameCTxt;
	
	/**
	 * 連線狀態
	 */
	private String atmstatStatus;
	
	/**
	 * 是否連線至FEP
	 */
	private String atmFepConnection;

	/**
	 * ATM代號
	 */
	private String atmAtmNoTxt;

	/**
	 * 機型
	 */
	private String atmModelnoTxt;
	
	/**
	 * 廠牌
	 */
	private String atmVendor;

	/**
	 * ATM IP
	 */
	private String atmIpTxt;
	
	/**
	 * GATEWAY IP
	 */
	private String atmAtmpIp;

	/**
	 * 憑證版本
	 */
	private String atmCertaliasTxt;

	/**
	 * 通訊元件版本
	 */
	private String atmStatApVersionNTxt;

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

	public String getAtmstatStatus() {
		return atmstatStatus;
	}

	public void setAtmstatStatus(String atmstatStatus) {
		this.atmstatStatus = atmstatStatus;
	}

	public String getAtmFepConnection() {
		return atmFepConnection;
	}

	public void setAtmFepConnection(String atmFepConnection) {
		this.atmFepConnection = atmFepConnection;
	}

	public String getAtmAtmpIp() {
		return atmAtmpIp;
	}

	public void setAtmAtmpIp(String atmAtmpIp) {
		this.atmAtmpIp = atmAtmpIp;
	}

	public String getAtmVendor() {
		return atmVendor;
	}

	public void setAtmVendor(String atmVendor) {
		this.atmVendor = atmVendor;
	}

	public String getAtmStatApVersionNTxt() {
		return atmStatApVersionNTxt;
	}

	public void setAtmStatApVersionNTxt(String atmStatApVersionNTxt) {
		this.atmStatApVersionNTxt = atmStatApVersionNTxt;
	}
}
