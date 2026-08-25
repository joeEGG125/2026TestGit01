package com.syscom.fep.vo.text.inbk;

import java.math.BigDecimal;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class INBKGeneralResponse {
	private String _ChlEJNo = "";
	private String _EJNo = "";
	private String _RqTime = "";
	private String _RsTime = "";
	private FEPRsHeaderOverride[] _Overrides;
	private String _RsStat_RsStateCode = "";
	private String _RsStat_RsStateCode_type = "";
	private String _RsStat_Desc = "";
	private String _PCODE = "";
	private String _TXFUNCTION = "";
	private String _FGSEQNO = "";
	private String _TXDATE = "";
	private BigDecimal _FGAMT;
	private String _STAN = "";
	private String _STATUS = "";
	private String _TLRID = "";
	private String _SUPID = "";
	private String _RESULT = "";
	private String _UNITNO = "";
	private String _ALIASNAME = "";
	private String _PAYTYPE = "";
	private String _FEENO = "";
	private String _PAYNAME = "";
	private String _RECCOUNT = "";

	public String getChlEJNo() {
		return _ChlEJNo;
	}

	public void setChlEJNo(String value) {
		_ChlEJNo = value;
	}

	public String getEJNo() {
		return _EJNo;
	}

	public void setEJNo(String value) {
		_EJNo = value;
	}

	public String getRqTime() {
		return _RqTime;
	}

	public void setRqTime(String value) {
		_RqTime = value;
	}

	public String getRsTime() {
		return _RsTime;
	}

	public void setRsTime(String value) {
		_RsTime = value;
	}

	public FEPRsHeaderOverride[] getOverrides() {
		return _Overrides;
	}

	public void setOverrides(FEPRsHeaderOverride[] value) {
		_Overrides = value;
	}

	public String getRsStatRsStateCode() {
		return _RsStat_RsStateCode;
	}

	public void setRsStatRsStateCode(String value) {
		_RsStat_RsStateCode = value;
	}

	public String getRsStatRsStateCodeType() {
		return _RsStat_RsStateCode_type;
	}

	public void setRsStatRsStateCodeType(String value) {
		_RsStat_RsStateCode_type = value;
	}

	public String getRsStatDesc() {
		return _RsStat_Desc;
	}

	public void setRsStatDesc(String value) {
		_RsStat_Desc = value;
	}

	/**
	 * 財金 PCODE
	 * 
	 * <remark>同 Request</remark>
	 */
	public String getPCODE() {
		return _PCODE;
	}

	public void setPCODE(String value) {
		_PCODE = value;
	}

	/**
	 * 功能
	 * 
	 * <remark>同 Request</remark>
	 */
	public String getTXFUNCTION() {
		return _TXFUNCTION;
	}

	public void setTXFUNCTION(String value) {
		_TXFUNCTION = value;
	}

	/**
	 * 撥轉序號
	 * 
	 * <remark></remark>
	 */
	public String getFGSEQNO() {
		return _FGSEQNO;
	}

	public void setFGSEQNO(String value) {
		_FGSEQNO = value;
	}

	/**
	 * 輸入日期
	 * 
	 * <remark></remark>
	 */
	public String getTXDATE() {
		return _TXDATE;
	}

	public void setTXDATE(String value) {
		_TXDATE = value;
	}

	/**
	 * 撥轉金額
	 * 
	 * <remark></remark>
	 */
	public BigDecimal getFGAMT() {
		return _FGAMT;
	}

	public void setFGAMT(BigDecimal value) {
		_FGAMT = value;
	}

	/**
	 * 財金序號
	 * 
	 * <remark></remark>
	 */
	public String getSTAN() {
		return _STAN;
	}

	public void setSTAN(String value) {
		_STAN = value;
	}

	/**
	 * 已放行否
	 * 
	 * <remark>N=未放行 Y=已放行 P=已送出放行,但PENDING中</remark>
	 */
	public String getSTATUS() {
		return _STATUS;
	}

	public void setSTATUS(String value) {
		_STATUS = value;
	}

	/**
	 * 登錄櫃員
	 * 
	 * <remark></remark>
	 */
	public String getTLRID() {
		return _TLRID;
	}

	public void setTLRID(String value) {
		_TLRID = value;
	}

	/**
	 * 放行人員
	 * 
	 * <remark></remark>
	 */
	public String getSUPID() {
		return _SUPID;
	}

	public void setSUPID(String value) {
		_SUPID = value;
	}

	/**
	 * 
	 * 
	 * <remark>處理結果</remark>
	 */
	public String getRESULT() {
		return _RESULT;
	}

	public void setRESULT(String value) {
		_RESULT = value;
	}

	/**
	 * 委託單位代號
	 * 
	 * <remark></remark>
	 */
	public String getUNITNO() {
		return _UNITNO;
	}

	public void setUNITNO(String value) {
		_UNITNO = value;
	}

	/**
	 * 委託單位簡稱
	 * 
	 * <remark></remark>
	 */
	public String getALIASNAME() {
		return _ALIASNAME;
	}

	public void setALIASNAME(String value) {
		_ALIASNAME = value;
	}

	/**
	 * 費用類別代號
	 * 
	 * <remark></remark>
	 */
	public String getPAYTYPE() {
		return _PAYTYPE;
	}

	public void setPAYTYPE(String value) {
		_PAYTYPE = value;
	}

	/**
	 * 費用代號
	 * 
	 * <remark></remark>
	 */
	public String getFEENO() {
		return _FEENO;
	}

	public void setFEENO(String value) {
		_FEENO = value;
	}

	/**
	 * 費用名稱
	 * 
	 * <remark></remark>
	 */
	public String getPAYNAME() {
		return _PAYNAME;
	}

	public void setPAYNAME(String value) {
		_PAYNAME = value;
	}

	/**
	 * 明細筆數
	 * 
	 * <remark></remark>
	 */
	public String getRECCOUNT() {
		return _RECCOUNT;
	}

	public void setRECCOUNT(String value) {
		_RECCOUNT = value;
	}

}
