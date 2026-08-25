package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;
/**
 * @author jie
 * @create 2021/11/24
 */
public class UI_028120_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	// 對方行
    private String bkno;

    // 交易日期
    private String txdate;
    
    // 匯出入
    private String ioflag;

    // 電文序號
    private String inFiscsno;

	public String getBkno() {
		return bkno;
	}

	public void setBkno(String bkno) {
		this.bkno = bkno;
	}

	public String getTxdate() {
		return txdate;
	}

	public void setTxdate(String txdate) {
		this.txdate = txdate;
	}

	public String getIoflag() {
		return ioflag;
	}

	public void setIoflag(String ioflag) {
		this.ioflag = ioflag;
	}

	public String getInFiscsno() {
		return inFiscsno;
	}

	public void setInFiscsno(String inFiscsno) {
		this.inFiscsno = inFiscsno;
	}
}
