package com.syscom.fep.mybatis.ext.model;

import com.syscom.fep.mybatis.vo.BaseModel;

/**
 * @author xingyun_yang
 * @create 2021/8/25
 */
public class FwdtxnExt extends BaseModel {
	private static final long serialVersionUID = 1L;
	private String fwdtxnChannelS;
	private String fwdtxnTxDate;
	private String fwdtxnTxId;
	private String fwdtxnPcode;
	private String fwdtxnTroutActno;
	private String fwdtxnTrinBkno;
	private String fwdtxnTrinActno;
	private String fwdtxnTxAmt;
	private String fwdtxnReplyCode;
	private String fwdrstTxDate;
	private String fwdrstTxId;
	private String fwdrstRunNo;
	private String fwdrstEjfno;
	private String fwdrstTxrust;
	private String fwdrstReplyCode;
	private String fwdrstErrMsg;
	private String fwdtxnRerunFg;

	public String getFwdtxnChannelS() {
		return fwdtxnChannelS;
	}

	public void setFwdtxnChannelS(String fwdtxnChannelS) {
		this.fwdtxnChannelS = fwdtxnChannelS;
	}

	public String getFwdtxnTxDate() {
		return fwdtxnTxDate;
	}

	public void setFwdtxnTxDate(String fwdtxnTxDate) {
		this.fwdtxnTxDate = fwdtxnTxDate;
	}

	public String getFwdtxnTxId() {
		return fwdtxnTxId;
	}

	public void setFwdtxnTxId(String fwdtxnTxId) {
		this.fwdtxnTxId = fwdtxnTxId;
	}

	public String getFwdtxnPcode() {
		return fwdtxnPcode;
	}

	public void setFwdtxnPcode(String fwdtxnPcode) {
		this.fwdtxnPcode = fwdtxnPcode;
	}

	public String getFwdtxnTroutActno() {
		return fwdtxnTroutActno;
	}

	public void setFwdtxnTroutActno(String fwdtxnTroutActno) {
		this.fwdtxnTroutActno = fwdtxnTroutActno;
	}

	public String getFwdtxnTrinBkno() {
		return fwdtxnTrinBkno;
	}

	public void setFwdtxnTrinBkno(String fwdtxnTrinBkno) {
		this.fwdtxnTrinBkno = fwdtxnTrinBkno;
	}

	public String getFwdtxnTrinActno() {
		return fwdtxnTrinActno;
	}

	public void setFwdtxnTrinActno(String fwdtxnTrinActno) {
		this.fwdtxnTrinActno = fwdtxnTrinActno;
	}

	public String getFwdtxnTxAmt() {
		return fwdtxnTxAmt;
	}

	public void setFwdtxnTxAmt(String fwdtxnTxAmt) {
		this.fwdtxnTxAmt = fwdtxnTxAmt;
	}

	public String getFwdtxnReplyCode() {
		return fwdtxnReplyCode;
	}

	public void setFwdtxnReplyCode(String fwdtxnReplyCode) {
		this.fwdtxnReplyCode = fwdtxnReplyCode;
	}

	public String getFwdrstTxDate() {
		return fwdrstTxDate;
	}

	public void setFwdrstTxDate(String fwdrstTxDate) {
		this.fwdrstTxDate = fwdrstTxDate;
	}

	public String getFwdrstTxId() {
		return fwdrstTxId;
	}

	public void setFwdrstTxId(String fwdrstTxId) {
		this.fwdrstTxId = fwdrstTxId;
	}

	public String getFwdrstRunNo() {
		return fwdrstRunNo;
	}

	public void setFwdrstRunNo(String fwdrstRunNo) {
		this.fwdrstRunNo = fwdrstRunNo;
	}

	public String getFwdrstEjfno() {
		return fwdrstEjfno;
	}

	public void setFwdrstEjfno(String fwdrstEjfno) {
		this.fwdrstEjfno = fwdrstEjfno;
	}

	public String getFwdrstTxrust() {
		return fwdrstTxrust;
	}

	public void setFwdrstTxrust(String fwdrstTxrust) {
		this.fwdrstTxrust = fwdrstTxrust;
	}

	public String getFwdrstReplyCode() {
		return fwdrstReplyCode;
	}

	public void setFwdrstReplyCode(String fwdrstReplyCode) {
		this.fwdrstReplyCode = fwdrstReplyCode;
	}

	public String getFwdrstErrMsg() {
		return fwdrstErrMsg;
	}

	public void setFwdrstErrMsg(String fwdrstErrMsg) {
		this.fwdrstErrMsg = fwdrstErrMsg;
	}

	public String getFwdtxnRerunFg() {
		return fwdtxnRerunFg;
	}

	public void setFwdtxnRerunFg(String fwdTxnRerunFg) {
		this.fwdtxnRerunFg = fwdTxnRerunFg;
	}

	@Override
	public String toString() {
		return "FwdtxnandrstExt{" +
				"fwdtxnChannelS='" + fwdtxnChannelS + '\'' +
				", fwdtxnTxDate='" + fwdtxnTxDate + '\'' +
				", fwdtxnTxId='" + fwdtxnTxId + '\'' +
				", fwdtxnPcode='" + fwdtxnPcode + '\'' +
				", fwdtxnTroutActno='" + fwdtxnTroutActno + '\'' +
				", fwdtxnTrinBkno='" + fwdtxnTrinBkno + '\'' +
				", fwdtxnTrinActno='" + fwdtxnTrinActno + '\'' +
				", fwdtxnTxAmt='" + fwdtxnTxAmt + '\'' +
				", fwdtxnReplyCode='" + fwdtxnReplyCode + '\'' +
				", fwdrstTxDate='" + fwdrstTxDate + '\'' +
				", fwdrstTxId='" + fwdrstTxId + '\'' +
				", fwdrstRunNo='" + fwdrstRunNo + '\'' +
				", fwdrstEjfno='" + fwdrstEjfno + '\'' +
				", fwdrstTxrust='" + fwdrstTxrust + '\'' +
				", fwdrstReplyCode='" + fwdrstReplyCode + '\'' +
				", fwdrstErrMsg='" + fwdrstErrMsg + '\'' +
				", fwdTxnRerunFg='" + fwdtxnRerunFg + '\'' +
				'}';
	}

	public FwdtxnExt(String fwdtxnChannelS, String fwdtxnTxDate, String fwdtxnTxId, String fwdtxnPcode, String fwdtxnTroutActno, String fwdtxnTrinBkno, String fwdtxnTrinActno, String fwdtxnTxAmt,
			String fwdtxnReplyCode, String fwdrstTxDate, String fwdrstTxId, String fwdrstRunNo, String fwdrstEjfno, String fwdrstTxrust, String fwdrstReplyCode, String fwdrstErrMsg,
			String fwdtxnRerunFg) {
		this.fwdtxnChannelS = fwdtxnChannelS;
		this.fwdtxnTxDate = fwdtxnTxDate;
		this.fwdtxnTxId = fwdtxnTxId;
		this.fwdtxnPcode = fwdtxnPcode;
		this.fwdtxnTroutActno = fwdtxnTroutActno;
		this.fwdtxnTrinBkno = fwdtxnTrinBkno;
		this.fwdtxnTrinActno = fwdtxnTrinActno;
		this.fwdtxnTxAmt = fwdtxnTxAmt;
		this.fwdtxnReplyCode = fwdtxnReplyCode;
		this.fwdrstTxDate = fwdrstTxDate;
		this.fwdrstTxId = fwdrstTxId;
		this.fwdrstRunNo = fwdrstRunNo;
		this.fwdrstEjfno = fwdrstEjfno;
		this.fwdrstTxrust = fwdrstTxrust;
		this.fwdrstReplyCode = fwdrstReplyCode;
		this.fwdrstErrMsg = fwdrstErrMsg;
		this.fwdtxnRerunFg = fwdtxnRerunFg;
	}

	@Override
	public String fieldsToXml() {
		return null;
	}
}
