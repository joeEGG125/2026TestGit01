package com.syscom.fep.web.form.atmmon;

import java.util.List;

import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.web.form.BaseForm;

public class UI_060070_Form extends BaseForm {

	private static final long serialVersionUID = 1L;

	private List<Subsys> subsysList;
	
	private String sysconfSubsysno;
	private String sysconfName;
	
	

	
	public String getSysconfSubsysno() {
		return sysconfSubsysno;
	}

	public void setSysconfSubsysno(String sysconfSubsysno) {
		this.sysconfSubsysno = sysconfSubsysno;
	}

	public String getSysconfName() {
		return sysconfName;
	}

	public void setSysconfName(String sysconfName) {
		this.sysconfName = sysconfName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Subsys> getSubsysList() {
		return subsysList;
	}

	public void setSubsysList(List<Subsys> subsysList) {
		this.subsysList = subsysList;
	}

}
