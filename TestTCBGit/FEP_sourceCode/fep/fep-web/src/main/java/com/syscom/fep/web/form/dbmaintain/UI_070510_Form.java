package com.syscom.fep.web.form.dbmaintain;

import java.util.Date;

import com.syscom.fep.web.form.BaseForm;

/**
 * 偽BIN資料檔HOTBIN
 * 
 * @author Han
 *
 */
public class UI_070510_Form extends BaseForm {

	private static final long serialVersionUID = 1L;

//	query頁
	private String txtBinNo;  // CREDIT CARD BIN
	private String txtBinOrg; // 發卡組織
//insert頁
	private String txtBinNoInsert;  // CREDIT CARD BIN
	private String txtBinOrgInsert; // 發卡組織
	private String focusInsert; 	// 新憎完後回上一頁需要焦點的列
//查詢結果
	private String binNo;
	private String binOrg;
	private Integer updateUserid;
	private Date updateTime;

	public String getTxtBinNo() {
		return txtBinNo;
	}

	public void setTxtBinNo(String txtBinNo) {
		this.txtBinNo = txtBinNo;
	}

	public String getTxtBinOrg() {
		return txtBinOrg;
	}

	public void setTxtBinOrg(String txtBinOrg) {
		this.txtBinOrg = txtBinOrg;
	}
	
	public String getBinNo() {
		return binNo;
	}

	public void setBinNo(String binNo) {
		this.binNo = binNo;
	}

	public String getBinOrg() {
		return binOrg;
	}

	public void setBinOrg(String binOrg) {
		this.binOrg = binOrg;
	}

	public Integer getUpdateUserid() {
		return updateUserid;
	}

	public void setUpdateUserid(Integer updateUserid) {
		this.updateUserid = updateUserid;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getTxtBinNoInsert() {
		return txtBinNoInsert;
	}

	public void setTxtBinNoInsert(String txtBinNoInsert) {
		this.txtBinNoInsert = txtBinNoInsert;
	}

	public String getTxtBinOrgInsert() {
		return txtBinOrgInsert;
	}

	public void setTxtBinOrgInsert(String txtBinOrgInsert) {
		this.txtBinOrgInsert = txtBinOrgInsert;
	}

	public String getFocusInsert() {
		return focusInsert;
	}

	public void setFocusInsert(String focusInsert) {
		this.focusInsert = focusInsert;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
