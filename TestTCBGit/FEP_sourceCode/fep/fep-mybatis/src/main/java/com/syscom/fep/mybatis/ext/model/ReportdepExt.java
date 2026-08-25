package com.syscom.fep.mybatis.ext.model;

import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.model.Reportdep;

public class ReportdepExt extends Reportdep{
	
	public ReportdepExt() {
		
	}
	
	public ReportdepExt(Reportdep reportdep) {
		if(reportdep == null) {
			return;
		}
		BeanUtils.copyProperties(reportdep, this);
	}
	
	private static final long serialVersionUID = 1L;
	
	private String UpdateUserName;
	private String reportdepTypeTxt;

	public String getUpdateUserName() {
		return UpdateUserName;
	}

	public void setUpdateUserName(String updateUserName) {
		UpdateUserName = updateUserName;
	}

	public String getReportdepTypeTxt() {
		return reportdepTypeTxt;
	}

	public void setReportdepTypeTxt(String reportdepTypeTxt) {
		this.reportdepTypeTxt = reportdepTypeTxt;
	}
	
}
