package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;
/**
 * @author jie
 * @create 2021/12/06
 */
public class UI_028230_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;
    private String rmoutTxDate;

    private String rmoutBrno;
    
    private String rmoutOriginal;

    private String rmoutFepno;

    private String rmoutFiscRtnCode;
    
    private String rmoutStat;

	public String getRmoutTxDate() {
		return rmoutTxDate;
	}

	public void setRmoutTxDate(String rmoutTxDate) {
		this.rmoutTxDate = rmoutTxDate;
	}

	public String getRmoutBrno() {
		return rmoutBrno;
	}

	public void setRmoutBrno(String rmoutBrno) {
		this.rmoutBrno = rmoutBrno;
	}

	public String getRmoutOriginal() {
		return rmoutOriginal;
	}

	public void setRmoutOriginal(String rmoutOriginal) {
		this.rmoutOriginal = rmoutOriginal;
	}

	public String getRmoutFepno() {
		return rmoutFepno;
	}

	public void setRmoutFepno(String rmoutFepno) {
		this.rmoutFepno = rmoutFepno;
	}

	public String getRmoutFiscRtnCode() {
		return rmoutFiscRtnCode;
	}

	public void setRmoutFiscRtnCode(String rmoutFiscRtnCode) {
		this.rmoutFiscRtnCode = rmoutFiscRtnCode;
	}

	public String getRmoutStat() {
		return rmoutStat;
	}

	public void setRmoutStat(String rmoutStat) {
		this.rmoutStat = rmoutStat;
	}
}
