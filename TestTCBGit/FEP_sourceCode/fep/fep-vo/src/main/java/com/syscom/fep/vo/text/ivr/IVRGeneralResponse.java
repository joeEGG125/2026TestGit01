package com.syscom.fep.vo.text.ivr;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class IVRGeneralResponse {
	private String ChlEJNo = StringUtils.EMPTY;
	private String EJNo = StringUtils.EMPTY;
	private String RqTime = StringUtils.EMPTY;
	private String RsTime = StringUtils.EMPTY;
	private FEPRsHeaderOverride[] Overrides;
	private String RsStat_RsStateCode = StringUtils.EMPTY;
	private String RsStat_RsStateCode_type = StringUtils.EMPTY;
	private String RsStat_Desc = StringUtils.EMPTY;

	private BigDecimal RECCOUNT;
	private String Dependency = StringUtils.EMPTY;
	private String IDNO = StringUtils.EMPTY;
	private String AccountNo = StringUtils.EMPTY;
	private String TPINStatus = StringUtils.EMPTY;
	private String PINBlock = StringUtils.EMPTY;

	public String getChlEJNo() {
		return ChlEJNo;
	}

	public void setChlEJNo(String value) {
		ChlEJNo = value;
	}

	public String getEJNo() {
		return EJNo;
	}

	public void setEJNo(String value) {
		EJNo = value;
	}

	public String getRqTime() {
		return RqTime;
	}

	public void setRqTime(String value) {
		RqTime = value;
	}

	public String getRsTime() {
		return RsTime;
	}

	public void setRsTime(String value) {
		RsTime = value;
	}

	/**
	 * 資料總筆數
	 * 
	 * <remark></remark>
	 */
	public BigDecimal getRECCOUNT() {
		return RECCOUNT;
	}

	public void setRECCOUNT(BigDecimal value) {
		RECCOUNT = value;
	}

	/**
	 * 驗密方式
	 * 
	 * <remark></remark>
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
	 * <remark></remark>
	 */
	public String getIDNO() {
		return IDNO;
	}

	public void setIDNO(String value) {
		IDNO = value;
	}

	/**
	 * 帳號
	 * 
	 * <remark></remark>
	 */
	public String getAccountNo() {
		return AccountNo;
	}

	public void setAccountNo(String value) {
		AccountNo = value;
	}

	/**
	 * 密碼狀態
	 * 
	 * <remark></remark>
	 */
	public String getTPINStatus() {
		return TPINStatus;
	}

	public void setTPINStatus(String value) {
		TPINStatus = value;
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

	public FEPRsHeaderOverride[] getOverrides() {
		return Overrides;
	}

	public void setOverrides(FEPRsHeaderOverride[] value) {
		Overrides = value;
	}

	public String getRsStatRsStateCode() {
		return RsStat_RsStateCode;
	}

	public void setRsStatRsStateCode(String value) {
		RsStat_RsStateCode = value;
	}

	public String getRsStatRsStateCodeType() {
		return RsStat_RsStateCode_type;
	}

	public void setRsStatRsStateCodeType(String value) {
		RsStat_RsStateCode_type = value;
	}

	public String getRsStatDesc() {
		return RsStat_Desc;
	}

	public void setRsStatDesc(String value) {
		RsStat_Desc = value;
	}
}