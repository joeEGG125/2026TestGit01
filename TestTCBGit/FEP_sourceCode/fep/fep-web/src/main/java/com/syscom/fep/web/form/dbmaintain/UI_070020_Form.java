package com.syscom.fep.web.form.dbmaintain;

import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.web.form.BaseForm;

import java.util.List;

public class UI_070020_Form extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 前台js部分假日日期換色用
	 */
	List<Bsdays> bsdayList;
	
	/**
	 * 畫面按鈕由系統判斷是否要顯示
	 */
	private String webType;
	
	/**
	 * 年，進入頁面，text自動帶入今年
	 */
	private String txtBSDAYS_YEAR;
	
	/**
	 * 前台:地區
	 */
	private String lblBSDAYS_ZONE_CODE;
	
	/**
	 * 要亮燈的日期，逗號隔開 ex "20220713,20220714,20220715"
	 * 	 
	 * */
	private String activeCalendar;
	private String sysstatTbsdyFisc;
	private String sysstatNbsdyFisc;

	public String getCleanCalendar() {
		return cleanCalendar;
	}

	public void setCleanCalendar(String cleanCalendar) {
		this.cleanCalendar = cleanCalendar;
	}

	private String cleanCalendar;

	private List<String> monthList;
	
	private String BSDAYS_ZONE_CODEDdl;
	

	public String getWebType() {
		return webType;
	}

	public void setWebType(String webType) {
		this.webType = webType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTxtBSDAYS_YEAR() {
		return txtBSDAYS_YEAR;
	}

	public void setTxtBSDAYS_YEAR(String txtBSDAYS_YEAR) {
		this.txtBSDAYS_YEAR = txtBSDAYS_YEAR;
	}

	public String getLblBSDAYS_ZONE_CODE() {
		return lblBSDAYS_ZONE_CODE;
	}

	public void setLblBSDAYS_ZONE_CODE(String lblBSDAYS_ZONE_CODE) {
		this.lblBSDAYS_ZONE_CODE = lblBSDAYS_ZONE_CODE;
	}

	public List<String> getMonthList() {
		return monthList;
	}

	public void setMonthList(List<String> monthList) {
		this.monthList = monthList;
	}

	public String getBSDAYS_ZONE_CODEDdl() {
		return BSDAYS_ZONE_CODEDdl;
	}

	public void setBSDAYS_ZONE_CODEDdl(String bSDAYS_ZONE_CODEDdl) {
		BSDAYS_ZONE_CODEDdl = bSDAYS_ZONE_CODEDdl;
	}

	public List<Bsdays> getBsdayList() {
		return bsdayList;
	}

	public void setBsdayList(List<Bsdays> bsdayList) {
		this.bsdayList = bsdayList;
	}

	public String getActiveCalendar() {
		return activeCalendar;
	}

	public void setActiveCalendar(String activeCalendar) {
		this.activeCalendar = activeCalendar;
	}

	public String getSysstatTbsdyFisc() {return sysstatTbsdyFisc;}

	public void setSysstatTbsdyFisc(String sysstatTbsdyFisc) {this.sysstatTbsdyFisc = sysstatTbsdyFisc;}

	public String getSysstatNbsdyFisc() {return sysstatNbsdyFisc;}

	public void setSysstatNbsdyFisc(String sysstatNbsdyFisc) {this.sysstatNbsdyFisc = sysstatNbsdyFisc;}
}
