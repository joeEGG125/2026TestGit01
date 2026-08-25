package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_013100_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String noticeidd;
	private String noticetext;
	private String textnotxt;
	private String idtext;
	private String resulttxt;
	private String banktxt;

	public UI_013100_Form() {}

	public UI_013100_Form(String noticeidd, String noticetext, String textnotxt, String idtext, String resulttxt, String banktxt) {
		this.noticeidd = noticeidd;
		this.noticetext = noticetext;
		this.textnotxt = textnotxt;
		this.idtext = idtext;
		this.resulttxt = resulttxt;
		this.banktxt = banktxt;
	}

	public String getNoticeidd() {
		return noticeidd;
	}

	public void setNoticeidd(String noticeidd) {
		this.noticeidd = noticeidd;
	}

	public String getNoticetext() {
		return noticetext;
	}

	public void setNoticetext(String noticetext) {
		this.noticetext = noticetext;
	}

	public String getTextnotxt() {
		return textnotxt;
	}

	public void setTextnotxt(String textnotxt) {
		this.textnotxt = textnotxt;
	}

	public String getIdtext() {
		return idtext;
	}

	public void setIdtext(String idtext) {
		this.idtext = idtext;
	}

	public String getResulttxt() {
		return resulttxt;
	}

	public void setResulttxt(String resulttxt) {
		this.resulttxt = resulttxt;
	}

	public String getBanktxt() {
		return banktxt;
	}

	public void setBanktxt(String banktxt) {
		this.banktxt = banktxt;
	}
}
