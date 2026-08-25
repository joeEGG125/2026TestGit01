package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/7
 */
public class UI_019050_FormDetail extends BaseForm {
	private String fileid;
	private String txdate;
	private String npsbatchBatchNo;
	private Integer ejfno;

	public String getFileid() {
		return fileid;
	}

	public void setFileid(String fileid) {
		this.fileid = fileid;
	}

	public String getTxdate() {
		return txdate;
	}

	public void setTxdate(String txdate) {
		this.txdate = txdate;
	}

	public String getNpsbatchBatchNo() {
		return npsbatchBatchNo;
	}

	public void setNpsbatchBatchNo(String npsbatchBatchNo) {
		this.npsbatchBatchNo = npsbatchBatchNo;
	}

	public Integer getEjfno() {
		return ejfno;
	}
	public void setEjfno(Integer ejfno) {
		this.ejfno = ejfno;
	}
}
