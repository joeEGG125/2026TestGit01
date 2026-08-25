package com.syscom.fep.web.form.dbmaintain;

import com.syscom.fep.web.form.BaseForm;

public class UI_070020_FormDetail extends BaseForm {

	private static final long serialVersionUID = 1L;

	private String style; // updagte or insert
	private String webType;

	/**
	 * 分行代號
	 */
	private String BSDAYS_ZONE_CODEDdl;

	/**
	 * 日曆日
	 */
	private String txtBSDAYS_DATE;

	/**
	 * 本年的第幾天
	 */
	private String txtBSDAYS_JDAY;

	/**
	 * 工作日記號
	 */
	private String BSDAYS_WORKDAYDdl;
	/**
	 * 星期幾
	 */
	private String txtBSDAYS_WEEKNO;
	/**
	 * 下營業日
	 */
	private String txtBSDAYS_NBSDY;
	/**
	 * ATM清算日
	 */
	private String txtBSDAYS_ST_DATE_ATM;
	/**
	 * RM清算日
	 */
	private String txtBSDAYS_ST_DATE_RM;
	/**
	 * 清算記號
	 */
	private String txtBSDAYS_ST_FLAG;

	public String getBSDAYS_ZONE_CODEDdl() {
		return BSDAYS_ZONE_CODEDdl;
	}

	public void setBSDAYS_ZONE_CODEDdl(String bSDAYS_ZONE_CODEDdl) {
		BSDAYS_ZONE_CODEDdl = bSDAYS_ZONE_CODEDdl;
	}

	public String getTxtBSDAYS_DATE() {
		return txtBSDAYS_DATE;
	}

	public void setTxtBSDAYS_DATE(String txtBSDAYS_DATE) {
		this.txtBSDAYS_DATE = txtBSDAYS_DATE;
	}

	public String getTxtBSDAYS_JDAY() {
		return txtBSDAYS_JDAY;
	}

	public void setTxtBSDAYS_JDAY(String txtBSDAYS_JDAY) {
		this.txtBSDAYS_JDAY = txtBSDAYS_JDAY;
	}

	public String getBSDAYS_WORKDAYDdl() {
		return BSDAYS_WORKDAYDdl;
	}

	public void setBSDAYS_WORKDAYDdl(String bSDAYS_WORKDAYDdl) {
		BSDAYS_WORKDAYDdl = bSDAYS_WORKDAYDdl;
	}

	public String getTxtBSDAYS_NBSDY() {
		return txtBSDAYS_NBSDY;
	}

	public void setTxtBSDAYS_NBSDY(String txtBSDAYS_NBSDY) {
		this.txtBSDAYS_NBSDY = txtBSDAYS_NBSDY;
	}

	public String getTxtBSDAYS_ST_DATE_ATM() {
		return txtBSDAYS_ST_DATE_ATM;
	}

	public void setTxtBSDAYS_ST_DATE_ATM(String txtBSDAYS_ST_DATE_ATM) {
		this.txtBSDAYS_ST_DATE_ATM = txtBSDAYS_ST_DATE_ATM;
	}

	public String getTxtBSDAYS_ST_DATE_RM() {
		return txtBSDAYS_ST_DATE_RM;
	}

	public void setTxtBSDAYS_ST_DATE_RM(String txtBSDAYS_ST_DATE_RM) {
		this.txtBSDAYS_ST_DATE_RM = txtBSDAYS_ST_DATE_RM;
	}

	public String getTxtBSDAYS_WEEKNO() {
		return txtBSDAYS_WEEKNO;
	}

	public void setTxtBSDAYS_WEEKNO(String txtBSDAYS_WEEKNO) {
		this.txtBSDAYS_WEEKNO = txtBSDAYS_WEEKNO;
	}

	public String getTxtBSDAYS_ST_FLAG() {
		return txtBSDAYS_ST_FLAG;
	}

	public void setTxtBSDAYS_ST_FLAG(String txtBSDAYS_ST_FLAG) {
		this.txtBSDAYS_ST_FLAG = txtBSDAYS_ST_FLAG;
	}

	public String getWebType() {
		return webType;
	}

	public void setWebType(String webType) {
		this.webType = webType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
