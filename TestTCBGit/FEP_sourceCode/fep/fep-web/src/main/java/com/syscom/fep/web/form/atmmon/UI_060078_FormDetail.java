package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060078_FormDetail extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 財金營業日
	 */
	private String atmcTbsdyFisc;
	
	/**
	 * 自行營業日
	 */
	private String atmcTbsdy;
	
	/**
	 * 清算分行
	 */
	private String atmcBrnoSt;
	
	/**
	 * ATM代號
	 */
	private String atmcAtmno;
	
	/**
	 * 幣別
	 */
	private String atmcCur;
	
	/**
	 * 交易別
	 */
	private String atmcTxCode;
	
	/**
	 * 摘要
	 */
	private String atmcDscpt;
	
	/**
	 * 保留
	 */
	private String atmcSelfcd;
	
	/**
	 * 借方筆數
	 */
	private String atmcDrCnt;
	
	/**
	 * 借方金額
	 */
	private String atmcDrAmt;
	
	/**
	 * 貸方筆數
	 */
	private String atmcCrCnt;
	
	/**
	 * 貸方金額
	 */
	private String atmcCrAmt;
	
	/**
	 * 應付手續費筆數
	 */
	private String atmcTxCntCr;
	
	/**
	 * 應付手續費金額
	 */
	private String atmcTxFeeCr;
	
	/**
	 * 應收手續費筆數
	 */
	private String atmcTxCntDr;
	
	/**
	 * 應收手續費金額
	 */
	private String atmcTxFeeDr;
	
	/**
	 * 暫收款筆數
	 */
	private String atmcTmCnt;
	
	/**
	 * 暫收款金額
	 */
	private String atmcTmAmt;
	
	/**
	 * 行外記號
	 */
	private String atmcLoc;
	
	/**
	 * ATM所在區域
	 */
	private String atmcZone;
	
	/**
	 * ATM主清算幣別
	 */
	private String atmcCurSt;
	
	/**
	 * 跨區交易記號
	 */
	private String atmcCrossFlag;
	
	/**
	 * 跨區交易記號 更新人員
	 */
	private String updateUserid;
	
	/**
	 * 更新日期時間 
	 */
	private String updateTime;

	public String getAtmcTbsdyFisc() {
		return atmcTbsdyFisc;
	}

	public void setAtmcTbsdyFisc(String atmcTbsdyFisc) {
		this.atmcTbsdyFisc = atmcTbsdyFisc;
	}

	public String getAtmcTbsdy() {
		return atmcTbsdy;
	}

	public void setAtmcTbsdy(String atmcTbsdy) {
		this.atmcTbsdy = atmcTbsdy;
	}

	public String getAtmcBrnoSt() {
		return atmcBrnoSt;
	}

	public void setAtmcBrnoSt(String atmcBrnoSt) {
		this.atmcBrnoSt = atmcBrnoSt;
	}

	public String getAtmcAtmno() {
		return atmcAtmno;
	}

	public void setAtmcAtmno(String atmcAtmno) {
		this.atmcAtmno = atmcAtmno;
	}

	public String getAtmcCur() {
		return atmcCur;
	}

	public void setAtmcCur(String atmcCur) {
		this.atmcCur = atmcCur;
	}

	public String getAtmcTxCode() {
		return atmcTxCode;
	}

	public void setAtmcTxCode(String atmcTxCode) {
		this.atmcTxCode = atmcTxCode;
	}

	public String getAtmcDscpt() {
		return atmcDscpt;
	}

	public void setAtmcDscpt(String atmcDscpt) {
		this.atmcDscpt = atmcDscpt;
	}

	public String getAtmcSelfcd() {
		return atmcSelfcd;
	}

	public void setAtmcSelfcd(String atmcSelfcd) {
		this.atmcSelfcd = atmcSelfcd;
	}

	public String getAtmcDrCnt() {
		return atmcDrCnt;
	}

	public void setAtmcDrCnt(String atmcDrCnt) {
		this.atmcDrCnt = atmcDrCnt;
	}

	public String getAtmcDrAmt() {
		return atmcDrAmt;
	}

	public void setAtmcDrAmt(String atmcDrAmt) {
		this.atmcDrAmt = atmcDrAmt;
	}

	public String getAtmcCrCnt() {
		return atmcCrCnt;
	}

	public void setAtmcCrCnt(String atmcCrCnt) {
		this.atmcCrCnt = atmcCrCnt;
	}

	public String getAtmcCrAmt() {
		return atmcCrAmt;
	}

	public void setAtmcCrAmt(String atmcCrAmt) {
		this.atmcCrAmt = atmcCrAmt;
	}

	public String getAtmcTxCntCr() {
		return atmcTxCntCr;
	}

	public void setAtmcTxCntCr(String atmcTxCntCr) {
		this.atmcTxCntCr = atmcTxCntCr;
	}

	public String getAtmcTxFeeCr() {
		return atmcTxFeeCr;
	}

	public void setAtmcTxFeeCr(String atmcTxFeeCr) {
		this.atmcTxFeeCr = atmcTxFeeCr;
	}

	public String getAtmcTxCntDr() {
		return atmcTxCntDr;
	}

	public void setAtmcTxCntDr(String atmcTxCntDr) {
		this.atmcTxCntDr = atmcTxCntDr;
	}

	public String getAtmcTxFeeDr() {
		return atmcTxFeeDr;
	}

	public void setAtmcTxFeeDr(String atmcTxFeeDr) {
		this.atmcTxFeeDr = atmcTxFeeDr;
	}

	public String getAtmcTmCnt() {
		return atmcTmCnt;
	}

	public void setAtmcTmCnt(String atmcTmCnt) {
		this.atmcTmCnt = atmcTmCnt;
	}

	public String getAtmcTmAmt() {
		return atmcTmAmt;
	}

	public void setAtmcTmAmt(String atmcTmAmt) {
		this.atmcTmAmt = atmcTmAmt;
	}

	public String getAtmcLoc() {
		return atmcLoc;
	}

	public void setAtmcLoc(String atmcLoc) {
		this.atmcLoc = atmcLoc;
	}

	public String getAtmcZone() {
		return atmcZone;
	}

	public void setAtmcZone(String atmcZone) {
		this.atmcZone = atmcZone;
	}

	public String getAtmcCurSt() {
		return atmcCurSt;
	}

	public void setAtmcCurSt(String atmcCurSt) {
		this.atmcCurSt = atmcCurSt;
	}

	public String getAtmcCrossFlag() {
		return atmcCrossFlag;
	}

	public void setAtmcCrossFlag(String atmcCrossFlag) {
		this.atmcCrossFlag = atmcCrossFlag;
	}

	public String getUpdateUserid() {
		return updateUserid;
	}

	public void setUpdateUserid(String updateUserid) {
		this.updateUserid = updateUserid;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
}
