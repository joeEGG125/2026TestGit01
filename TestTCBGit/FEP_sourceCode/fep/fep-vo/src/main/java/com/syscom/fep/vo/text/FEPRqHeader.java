package com.syscom.fep.vo.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("RqHeader")
public class FEPRqHeader {
	private String MsgID;
	private String MsgType;
	private String ChlName;
	private String ChlEJNo;
	private String ChlSendTime;
	private String TxnID;
	private String BranchID;
	private String TermID;
	private String UserID;
	private String SignID;

	public String getMsgID() {
		return this.MsgID;
	}

	public void setMsgID(String msgID) {
		this.MsgID = msgID;
	}

	public String getMsgType() {
		return this.MsgType;
	}

	public void setMsgType(String msgType) {
		this.MsgType = msgType;
	}

	public String getChlName() {
		return this.ChlName;
	}

	public void setChlName(String chlName) {
		this.ChlName = chlName;
	}

	public String getChlEJNo() {
		return this.ChlEJNo;
	}

	public void setChlEJNo(String chlEJNo) {
		this.ChlEJNo = chlEJNo;
	}

	public String getChlSendTime() {
		return this.ChlSendTime;
	}

	public void setChlSendTime(String chlSendTime) {
		this.ChlSendTime = chlSendTime;
	}

	public String getTxnID() {
		return this.TxnID;
	}

	public void setTxnID(String txnID) {
		this.TxnID = txnID;
	}

	public String getBranchID() {
		return this.BranchID;
	}

	public void setBranchID(String branchID) {
		this.BranchID = branchID;
	}

	public String getTermID() {
		return this.TermID;
	}

	public void setTermID(String termID) {
		this.TermID = termID;
	}

	public String getUserID() {
		return this.UserID;
	}

	public void setUserID(String userID) {
		this.UserID = userID;
	}

	public String getSignID() {
		return this.SignID;
	}

	public void setSignID(String signID) {
		this.SignID = signID;
	}
}
