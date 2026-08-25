package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019030_Form extends BaseForm {
	private static final long serialVersionUID = 1L;

	private String tradingDate;
	private Integer ejnotxt;
	private String queryok;
	private UI_019030_Form.RadioOption radioOption = UI_019030_Form.RadioOption.POSITIVE;

	public RadioOption getRadioOption() {
		return radioOption;
	}

	public void setRadioOption(RadioOption radioOption) {
		this.radioOption = radioOption;
	}

	public UI_019030_Form() {
		super();
	}

	public UI_019030_Form(String tradingDate, Integer ejnotxt) {
		this.tradingDate = tradingDate;
		this.ejnotxt = ejnotxt;
	}

	public String getTradingDate() {
		return tradingDate;
	}

	public void setTradingDate(String tradingDate) {
		this.tradingDate = tradingDate;
	}

	public Integer getEjnotxt() {
		return ejnotxt;
	}

	public void setEjnotxt(Integer ejnotxt) {
		this.ejnotxt = ejnotxt;
	}

	public String getQueryok() {
		return queryok;
	}

	public void setQueryok(String queryok) {
		this.queryok = queryok;
	}

	public static enum RadioOption {
		/**
		 * POSITIVE
		 */
		POSITIVE,
		/**
		 * 0501
		 */
		NEGARTIVE0501,
		/**
		 * 0601
		 */
		NEGATIVE0601
	}
}
