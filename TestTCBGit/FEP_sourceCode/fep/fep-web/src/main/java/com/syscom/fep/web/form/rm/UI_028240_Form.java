package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;
/**
 * @author jie
 * @create 2021/12/07
 */
public class UI_028240_Form extends BaseForm {
	private static final long serialVersionUID = 1L;

    // 營業日期
    private String txdate;

	public String getTxdate() {
		return txdate;
	}

	public void setTxdate(String txdate) {
		this.txdate = txdate;
	}
}
