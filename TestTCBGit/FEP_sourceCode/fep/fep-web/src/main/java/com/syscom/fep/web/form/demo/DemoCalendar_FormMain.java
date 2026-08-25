package com.syscom.fep.web.form.demo;

import com.syscom.fep.web.form.BaseForm;

public class DemoCalendar_FormMain extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String chooseYear;
	private String activeCalendar;

	public String getChooseYear() {
		return chooseYear;
	}

	public void setChooseYear(String year) {
		this.chooseYear = year;
	}

	public String getActiveCalendar() {
		return activeCalendar;
	}

	public void setActiveCalendar(String activeCalendar) {
		this.activeCalendar = activeCalendar;
	}
}
