package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;

public class UI_130900_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
    private String reportDep_depNo;
    private String reportDep_depName;
    private String reportDep_type;
    private String btnType;

	public String getReportDep_depNo() {
		return reportDep_depNo;
	}

	public void setReportDep_depNo(String reportDep_depNo) {
		this.reportDep_depNo = reportDep_depNo;
	}

	public String getReportDep_depName() {
		return reportDep_depName;
	}

	public void setReportDep_depName(String reportDep_depName) {
		this.reportDep_depName = reportDep_depName;
	}

	public String getReportDep_type() {
		return reportDep_type;
	}

	public void setReportDep_type(String reportDep_type) {
		this.reportDep_type = reportDep_type;
	}

	public String getBtnType() {
        return btnType;
    }

    public void setBtnType(String btnType) {
        this.btnType = btnType;
    }
}
