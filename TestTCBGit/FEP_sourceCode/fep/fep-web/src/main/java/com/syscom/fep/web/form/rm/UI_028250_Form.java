package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;
/**
 * @author jie
 * @create 2021/12/09
 */
public class UI_028250_Form extends BaseForm {
	private static final long serialVersionUID = 1L;

    // 程式代號
    private String programID;

    // 現存執行狀態
    private String PRGSTAT_FLAG;

	public String getProgramID() {
		return programID;
	}

	public void setProgramID(String programID) {
		this.programID = programID;
	}

	public String getPRGSTAT_FLAG() {
		return PRGSTAT_FLAG;
	}

	public void setPRGSTAT_FLAG(String pRGSTAT_FLAG) {
		PRGSTAT_FLAG = pRGSTAT_FLAG;
	} 
}
