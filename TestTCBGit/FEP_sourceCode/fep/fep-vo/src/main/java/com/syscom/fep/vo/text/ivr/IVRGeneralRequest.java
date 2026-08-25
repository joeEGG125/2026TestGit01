package com.syscom.fep.vo.text.ivr;

import org.apache.commons.lang3.StringUtils;

public class IVRGeneralRequest {
	private String MsgID = StringUtils.EMPTY;
	private String MsgType = StringUtils.EMPTY;
	private String ChlName = StringUtils.EMPTY;
	private String ChlEJNo = StringUtils.EMPTY;
	private String ChlSendTime;
	private String TxnID = StringUtils.EMPTY;
	private String BranchID = StringUtils.EMPTY;
	private String TermID = StringUtils.EMPTY;
	private String UserID = StringUtils.EMPTY;
	private String SignID = StringUtils.EMPTY;
	private String SvcRq = StringUtils.EMPTY;
	private String Rq = StringUtils.EMPTY;
	private String Dependency = StringUtils.EMPTY;
	private String IDNo = StringUtils.EMPTY;
	private String AccountNo = StringUtils.EMPTY;
	private String PINBlock = StringUtils.EMPTY;
	private String TPinStatus = StringUtils.EMPTY; // add by 榮升 2015/02/12 因IPIN修改增加欄位
	private String IPADD = StringUtils.EMPTY; // add by 榮升 2015/02/12 因IPIN修改增加欄位
	private String MAC = StringUtils.EMPTY;
	private String Action = StringUtils.EMPTY;
	private String Flag = StringUtils.EMPTY;

	public String getMsgID() {
		return MsgID;
	}

	public void setMsgID(String value) {
		MsgID = value;
	}

	public String getMsgType() {
		return MsgType;
	}

	public void setMsgType(String value) {
		MsgType = value;
	}

	public String getChlName() {
		return ChlName;
	}

	public void setChlName(String value) {
		ChlName = value;
	}

	public String getChlEJNo() {
		return ChlEJNo;
	}

	public void setChlEJNo(String value) {
		ChlEJNo = value;
	}

	public String getChlSendTime() {
		return ChlSendTime;
	}

	public void setChlSendTime(String value) {
		ChlSendTime = value;
	}

	public String getTxnID() {
		return TxnID;
	}

	public void setTxnID(String value) {
		TxnID = value;
	}

	public String getBranchID() {
		return BranchID;
	}

	public void setBranchID(String value) {
		BranchID = value;
	}

	public String getTermID() {
		return TermID;
	}

	public void setTermID(String value) {
		TermID = value;
	}

	public String getUserID() {
		return UserID;
	}

	public void setUserID(String value) {
		UserID = value;
	}

	/**
	 * 放行主管的員編
	 * 
	 * <remark></remark>
	 */
	public String getSignID() {
		return SignID;
	}

	public void setSignID(String value) {
		SignID = value;
	}

	/**
	 * 
	 * 
	 * <remark></remark>
	 */
	public String getSvcRq() {
		return SvcRq;
	}

	public void setSvcRq(String value) {
		SvcRq = value;
	}

	/**
	 * 匯款電文內容
	 * 
	 * <remark></remark>
	 */
	public String getRq() {
		return Rq;
	}

	public void setRq(String value) {
		Rq = value;
	}

	/**
	 * 驗密方式
	 * 
	 * <remark>1:驗密BY ID 2:驗密BY A/C</remark>
	 */
	public String getDependency() {
		return Dependency;
	}

	public void setDependency(String value) {
		Dependency = value;
	}

	/**
	 * 身份證號/統編,含檢查碼
	 * 
	 * <remark>[optional]</remark>
	 */
	public String getIDNo() {
		return IDNo;
	}

	public void setIDNo(String value) {
		IDNo = value;
	}

	/**
	 * 帳號
	 * 
	 * <remark>[optional]</remark>
	 */
	public String getAccountNo() {
		return AccountNo;
	}

	public void setAccountNo(String value) {
		AccountNo = value;
	}

	/**
	 * 語音密碼
	 * 
	 * <remark></remark>
	 */
	public String getPINBlock() {
		return PINBlock;
	}

	public void setPINBlock(String value) {
		PINBlock = value;
	}

	// add by 榮升 2015/02/12 因IPIN修改增加欄位
	/**
	 * 密碼狀態
	 * 
	 * <remark></remark>
	 */
	public String getTPinStatus() {
		return TPinStatus;
	}

	public void setTPinStatus(String value) {
		TPinStatus = value;
	}

	// add by 榮升 2015/02/12 因IPIN修改增加欄位
	/**
	 * IP位置
	 * 
	 * <remark></remark>
	 */
	public String getIPADD() {
		return IPADD;
	}

	public void setIPADD(String value) {
		IPADD = value;
	}

	/**
	 * MAC value
	 * 
	 * <remark></remark>
	 */
	public String getMAC() {
		return MAC;
	}

	public void setMAC(String value) {
		MAC = value;
	}

	/**
	 * 交易類別
	 * 
	 * <remark>0:新申請 1:驗密方式變更 2:密碼記號變更</remark>
	 */
	public String getAction() {
		return Action;
	}

	public void setAction(String value) {
		Action = value;
	}

	/**
	 * 密碼註記
	 * 
	 * <remark></remark>
	 */
	public String getFlag() {
		return Flag;
	}

	public void setFlag(String value) {
		Flag = value;
	}
}