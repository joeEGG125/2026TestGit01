package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060078_FormMain extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 財金營業日(畫面查詢條件)
	 */
	private String sysstatTbsdyFisc;
	
	/**
	 * 財金營業日(資料庫查詢條件)
	 */
	private String sysstatTbsdyFiscData;
	
	/**
	 * 自行營業日(畫面查詢條件)
	 */
	private String zoneZoneTbsdy;
	
	/**
	 * 自行營業日(資料庫查詢條件)
	 */
	private String zoneZoneTbsdyData;
	
	/**
	 * ATM代號(查詢條件)
	 */
	private String atmNo;
	
	/**
	 * 幣別(查詢條件)
	 */
	private String cur;
	
	/**
	 * 交易別(查詢條件)
	 */
    private String txCode;
   

	public String getSysstatTbsdyFisc() {
		return sysstatTbsdyFisc;
	}

	public void setSysstatTbsdyFisc(String sysstatTbsdyFisc) {
		this.sysstatTbsdyFisc = sysstatTbsdyFisc;
	}

	public String getSysstatTbsdyFiscData() {
		return sysstatTbsdyFiscData;
	}

	public void setSysstatTbsdyFiscData(String sysstatTbsdyFiscData) {
		this.sysstatTbsdyFiscData = sysstatTbsdyFiscData;
	}

	public String getZoneZoneTbsdy() {
		return zoneZoneTbsdy;
	}

	public void setZoneZoneTbsdy(String zoneZoneTbsdy) {
		this.zoneZoneTbsdy = zoneZoneTbsdy;
	}

	public String getZoneZoneTbsdyData() {
		return zoneZoneTbsdyData;
	}

	public void setZoneZoneTbsdyData(String zoneZoneTbsdyData) {
		this.zoneZoneTbsdyData = zoneZoneTbsdyData;
	}

	public String getAtmNo() {
		return atmNo;
	}

	public void setAtmNo(String atmNo) {
		this.atmNo = atmNo;
	}

	public String getCur() {
		return cur;
	}

	public void setCur(String cur) {
		this.cur = cur;
	}

	public String getTxCode() {
		return txCode;
	}

	public void setTxCode(String txCode) {
		this.txCode = txCode;
	}
}
