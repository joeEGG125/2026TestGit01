package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author jie
 * @create 2021/12/02
 */
public class UI_028220_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;
	// 批號
    private String rmbtchmtrTimes;

    // 匯款日期
    private String rmbtchmtrRemDate;
    
    // 匯款分行
    private String rmbtchmtrSdn;

    // 匯款總筆數
    private String rmbtchmtrCnt;

    // 匯款總金額
    private String rmbtchmtrAmt;

	public String getRmbtchmtrTimes() {
		return rmbtchmtrTimes;
	}

	public void setRmbtchmtrTimes(String rmbtchmtrTimes) {
		this.rmbtchmtrTimes = rmbtchmtrTimes;
	}

	public String getRmbtchmtrRemDate() {
		return rmbtchmtrRemDate;
	}

	public void setRmbtchmtrRemDate(String rmbtchmtrRemDate) {
		this.rmbtchmtrRemDate = rmbtchmtrRemDate;
	}

	public String getRmbtchmtrSdn() {
		return rmbtchmtrSdn;
	}

	public void setRmbtchmtrSdn(String rmbtchmtrSdn) {
		this.rmbtchmtrSdn = rmbtchmtrSdn;
	}

	public String getRmbtchmtrCnt() {
		return rmbtchmtrCnt;
	}

	public void setRmbtchmtrCnt(String rmbtchmtrCnt) {
		this.rmbtchmtrCnt = rmbtchmtrCnt;
	}

	public String getRmbtchmtrAmt() {
		return rmbtchmtrAmt;
	}

	public void setRmbtchmtrAmt(String rmbtchmtrAmt) {
		this.rmbtchmtrAmt = rmbtchmtrAmt;
	}
}
