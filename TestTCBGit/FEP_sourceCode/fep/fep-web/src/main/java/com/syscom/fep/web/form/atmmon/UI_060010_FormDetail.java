package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060010_FormDetail extends BaseForm{
	private static final long serialVersionUID = 1L;

	/**
	 * 首頁帶過來的欄位
	 */
	private String rbtnDelFlg;

	/**
	 * 首頁帶過來的欄位 EVM機台
	 */
	private String checkCtrlEmvB;

	/**
	 * 首頁帶過來的欄位 ATM_OS
	 */
	private String rbtnOS;

	/**
	 * 首頁帶過來的欄位 廠牌
	 */
	private String vendor;

	/**
	 * 首頁帶過來的欄位 型態別
	 */
	private String typeQuery;

	/**
	 * 首頁帶過來的欄位 績效單位
	 */
	private String insBrno;

	/**
	 * ATM代號
	 */
	private String atmAtmNo;

	/**
	 * S/N
	 */
	private String atmSno;

	/**
	 * 廠牌
	 */
	private String atmVendorTxt;

	/**
	 * 型別
	 */
	private String atmModelNo;

	/**
	 * IP Address
	 */
	private String atmIp;

	/**
	 * 型態別
	 */
	private String atmAtmType;

	/**
	 * 型態別中文
	 */
	private String atmAtmTypeTxt;

	/**
	 * 型態別 中文 + code
	 */
	private String atmAtmTypeCodeTxt;

	/**
	 * 行內外別
	 */
	private String atmLoc;

	/**
	 * 行內外別中文
	 */
	private String atmLocTxt;

	/**
	 * 行內外別 中文 + 代碼
	 */
	private String atmLocCodeTxt;

	/**
	 * 通路別
	 */
	private String atmChannelType;

	/**
	 * 通路別 中文
	 */
	private String atmChannelTypeTxt;

	/**
	 * 地區
	 */
	private String atmArea;

	/**
	 * 地區中文
	 */
	private String atmAreaTxt;

	/**
	 * 裝設地點
	 */
	private String atmLocation;

	/**
	 * 運補保全
	 */
	private String atmGuardCash;

	/**
	 * 電子保全
	 */
	private String atmGuardSecure;

	/**
	 * 管理分行
	 */
	private String atmBrNoMa;

	/**
	 * 分行中文名稱
	 */
	private String atmBranchNameC;

	/**
	 * 所在地城市中文名稱
	 */
	private String atmCityC;

	/**
	 * 所在地中文地址或位置
	 */
	private String atmAddressC;

	/**
	 * 啟用日期
	 */
	private String atmStartDate;

	/**
	 * 備註
	 */
	private String atmMemo;

	/**
	 * 是否24小時服務
	 */
	private String atm24Service;

	/**
	 * 是否連線層換KEY
	 */
	private String atmCheckMac;

	/**
	 * 系統更新時間
	 */
	private String updateTime;

	/**
	 * 使用者更新時間
	 */
	private String userUpdateTime;

	/**
	 * 憑證版本
	 */
	private String atmCertalias;

	public String getRbtnDelFlg() {
		return rbtnDelFlg;
	}

	public void setRbtnDelFlg(String rbtnDelFlg) {
		this.rbtnDelFlg = rbtnDelFlg;
	}

	public String getCheckCtrlEmvB() {
		return checkCtrlEmvB;
	}

	public void setCheckCtrlEmvB(String checkCtrlEmvB) {
		this.checkCtrlEmvB = checkCtrlEmvB;
	}

	public String getRbtnOS() {
		return rbtnOS;
	}

	public void setRbtnOS(String rbtnOS) {
		this.rbtnOS = rbtnOS;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getTypeQuery() {
		return typeQuery;
	}

	public void setTypeQuery(String typeQuery) {
		this.typeQuery = typeQuery;
	}

	public String getInsBrno() {
		return insBrno;
	}

	public void setInsBrno(String insBrno) {
		this.insBrno = insBrno;
	}

	public String getAtmAtmNo() {
		return atmAtmNo;
	}

	public void setAtmAtmNo(String atmAtmNo) {
		this.atmAtmNo = atmAtmNo;
	}

	public String getAtmSno() {
		return atmSno;
	}

	public void setAtmSno(String atmSno) {
		this.atmSno = atmSno;
	}

	public String getAtmVendorTxt() {
		return atmVendorTxt;
	}

	public void setAtmVendorTxt(String atmVendorTxt) {
		this.atmVendorTxt = atmVendorTxt;
	}

	public String getAtmModelNo() {
		return atmModelNo;
	}

	public void setAtmModelNo(String atmModelNo) {
		this.atmModelNo = atmModelNo;
	}

	public String getAtmIp() {
		return atmIp;
	}

	public void setAtmIp(String atmIp) {
		this.atmIp = atmIp;
	}

	public String getAtmAtmType() {
		return atmAtmType;
	}

	public void setAtmAtmType(String atmAtmType) {
		this.atmAtmType = atmAtmType;
	}

	public String getAtmAtmTypeTxt() {
		return atmAtmTypeTxt;
	}

	public void setAtmAtmTypeTxt(String atmAtmTypeTxt) {
		this.atmAtmTypeTxt = atmAtmTypeTxt;
	}

	public String getAtmAtmTypeCodeTxt() {
		return atmAtmTypeCodeTxt;
	}

	public void setAtmAtmTypeCodeTxt(String atmAtmTypeCodeTxt) {
		this.atmAtmTypeCodeTxt = atmAtmTypeCodeTxt;
	}

	public String getAtmLoc() {
		return atmLoc;
	}

	public void setAtmLoc(String atmLoc) {
		this.atmLoc = atmLoc;
	}

	public String getAtmLocTxt() {
		return atmLocTxt;
	}

	public void setAtmLocTxt(String atmLocTxt) {
		this.atmLocTxt = atmLocTxt;
	}

	public String getAtmLocCodeTxt() {
		return atmLocCodeTxt;
	}

	public void setAtmLocCodeTxt(String atmLocCodeTxt) {
		this.atmLocCodeTxt = atmLocCodeTxt;
	}

	public String getAtmChannelType() {
		return atmChannelType;
	}

	public void setAtmChannelType(String atmChannelType) {
		this.atmChannelType = atmChannelType;
	}

	public String getAtmChannelTypeTxt() {
		return atmChannelTypeTxt;
	}

	public void setAtmChannelTypeTxt(String atmChannelTypeTxt) {
		this.atmChannelTypeTxt = atmChannelTypeTxt;
	}

	public String getAtmArea() {
		return atmArea;
	}

	public void setAtmArea(String atmArea) {
		this.atmArea = atmArea;
	}

	public String getAtmAreaTxt() {
		return atmAreaTxt;
	}

	public void setAtmAreaTxt(String atmAreaTxt) {
		this.atmAreaTxt = atmAreaTxt;
	}

	public String getAtmLocation() {
		return atmLocation;
	}

	public void setAtmLocation(String atmLocation) {
		this.atmLocation = atmLocation;
	}

	public String getAtmGuardCash() {
		return atmGuardCash;
	}

	public void setAtmGuardCash(String atmGuardCash) {
		this.atmGuardCash = atmGuardCash;
	}

	public String getAtmGuardSecure() {
		return atmGuardSecure;
	}

	public void setAtmGuardSecure(String atmGuardSecure) {
		this.atmGuardSecure = atmGuardSecure;
	}

	public String getAtmBrNoMa() {
		return atmBrNoMa;
	}

	public void setAtmBrNoMa(String atmBrNoMa) {
		this.atmBrNoMa = atmBrNoMa;
	}

	public String getAtmBranchNameC() {
		return atmBranchNameC;
	}

	public void setAtmBranchNameC(String atmBranchNameC) {
		this.atmBranchNameC = atmBranchNameC;
	}

	public String getAtmCityC() {
		return atmCityC;
	}

	public void setAtmCityC(String atmCityC) {
		this.atmCityC = atmCityC;
	}

	public String getAtmAddressC() {
		return atmAddressC;
	}

	public void setAtmAddressC(String atmAddressC) {
		this.atmAddressC = atmAddressC;
	}

	public String getAtmStartDate() {
		return atmStartDate;
	}

	public void setAtmStartDate(String atmStartDate) {
		this.atmStartDate = atmStartDate;
	}

	public String getAtmMemo() {
		return atmMemo;
	}

	public void setAtmMemo(String atmMemo) {
		this.atmMemo = atmMemo;
	}

	public String getAtm24Service() {
		return atm24Service;
	}

	public void setAtm24Service(String atm24Service) {
		this.atm24Service = atm24Service;
	}

	public String getAtmCheckMac() {
		return atmCheckMac;
	}

	public void setAtmCheckMac(String atmCheckMac) {
		this.atmCheckMac = atmCheckMac;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getUserUpdateTime() {
		return userUpdateTime;
	}

	public void setUserUpdateTime(String userUpdateTime) {
		this.userUpdateTime = userUpdateTime;
	}

	public String getAtmCertalias() {
		return atmCertalias;
	}

	public void setAtmCertalias(String atmCertalias) {
		this.atmCertalias = atmCertalias;
	}
}
