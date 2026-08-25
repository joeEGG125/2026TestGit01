package com.syscom.fep.vo.text.inbk;

public class INBKGeneralRequest {
	private String _MsgID = "";
	private String _MsgType = "";
	private String _ChlName = "";
	private String _ChlEJNo = "";
	private String _ChlSendTime;
	private String _TxnID = "";
	private String _BranchID = "";
	private String _TermID = "";
	private String _UserID = "";
	private String _UNITNO = "";
	private String _PCODE = "";
	private String _TXFUNCTION = "";
	private String _FGSEQNO = "";
	private String _SUPID = "";

	public final String getMsgID() {
		return _MsgID;
	}

	public final void setMsgID(String value) {
		_MsgID = value;
	}

	public final String getMsgType() {
		return _MsgType;
	}

	public final void setMsgType(String value) {
		_MsgType = value;
	}

	public final String getChlName() {
		return _ChlName;
	}

	public final void setChlName(String value) {
		_ChlName = value;
	}

	public final String getChlEJNo() {
		return _ChlEJNo;
	}

	public final void setChlEJNo(String value) {
		_ChlEJNo = value;
	}

	public final String getChlSendTime() {
		return _ChlSendTime;
	}

	public final void setChlSendTime(String value) {
		_ChlSendTime = value;
	}

	public final String getTxnID() {
		return _TxnID;
	}

	public final void setTxnID(String value) {
		_TxnID = value;
	}

	public final String getBranchID() {
		return _BranchID;
	}

	public final void setBranchID(String value) {
		_BranchID = value;
	}

	public final String getTermID() {
		return _TermID;
	}

	public final void setTermID(String value) {
		_TermID = value;
	}

	public final String getUserID() {
		return _UserID;
	}

	public final void setUserID(String value) {
		_UserID = value;
	}

	/**
	 * 委託單位代號
	 * 
	 * <remark></remark>
	 */
	public final String getUNITNO() {
		return _UNITNO;
	}

	public final void setUNITNO(String value) {
		_UNITNO = value;
	}

	/**
	 * 財金 PCODE
	 * 
	 * <remark>固定為 5313</remark>
	 */
	public final String getPCODE() {
		return _PCODE;
	}

	public final void setPCODE(String value) {
		_PCODE = value;
	}

	/**
	 * 功能
	 * 
	 * <remark>I:查詢; R:放行</remark>
	 */
	public final String getTXFUNCTION() {
		return _TXFUNCTION;
	}

	public final void setTXFUNCTION(String value) {
		_TXFUNCTION = value;
	}

	/**
	 * 撥轉序號
	 * 
	 * <remark></remark>
	 */
	public final String getFGSEQNO() {
		return _FGSEQNO;
	}

	public final void setFGSEQNO(String value) {
		_FGSEQNO = value;
	}

	/**
	 * 放行人員
	 * 
	 * <remark></remark>
	 */
	public final String getSUPID() {
		return _SUPID;
	}

	public final void setSUPID(String value) {
		_SUPID = value;
	}
}
