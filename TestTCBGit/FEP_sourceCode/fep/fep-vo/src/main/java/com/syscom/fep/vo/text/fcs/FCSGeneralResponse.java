package com.syscom.fep.vo.text.fcs;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class FCSGeneralResponse {
	private String chlEJNo = StringUtils.EMPTY;
	private String EJNo = StringUtils.EMPTY;
	private String rqTime = StringUtils.EMPTY;
	private String rsTime = StringUtils.EMPTY;
	private FEPRsHeaderOverride[] overrides;
	private String rsStat_RsStateCode = StringUtils.EMPTY;
	private String rsStat_RsStateCode_type = StringUtils.EMPTY;
	private String rsStat_Desc = StringUtils.EMPTY;
	private String t24Application = StringUtils.EMPTY;
	private String t24Version = StringUtils.EMPTY;
	private String t24Operation = StringUtils.EMPTY;
	private String t24RspCode = StringUtils.EMPTY;
	private String in_Text = StringUtils.EMPTY;

	public String getChlEJNo() {
		return chlEJNo;
	}

	public void setChlEJNo(String chlEJNo) {
		this.chlEJNo = chlEJNo;
	}

	public String getEJNo() {
		return EJNo;
	}

	public void setEJNo(String EJNo) {
		this.EJNo = EJNo;
	}

	public String getRqTime() {
		return rqTime;
	}

	public void setRqTime(String rqTime) {
		this.rqTime = rqTime;
	}

	public String getRsTime() {
		return rsTime;
	}

	public void setRsTime(String rsTime) {
		this.rsTime = rsTime;
	}

	public FEPRsHeaderOverride[] getOverrides() {
		return overrides;
	}

	public void setOverrides(FEPRsHeaderOverride[] overrides) {
		this.overrides = overrides;
	}

	public String getRsStat_RsStateCode() {
		return rsStat_RsStateCode;
	}

	public void setRsStat_RsStateCode(String rsStat_RsStateCode) {
		this.rsStat_RsStateCode = rsStat_RsStateCode;
	}

	public String getRsStat_RsStateCode_type() {
		return rsStat_RsStateCode_type;
	}

	public void setRsStat_RsStateCode_type(String rsStat_RsStateCode_type) {
		this.rsStat_RsStateCode_type = rsStat_RsStateCode_type;
	}

	public String getRsStat_Desc() {
		return rsStat_Desc;
	}

	public void setRsStat_Desc(String rsStat_Desc) {
		this.rsStat_Desc = rsStat_Desc;
	}

	public String getT24Application() {
		return t24Application;
	}

	public void setT24Application(String t24Application) {
		this.t24Application = t24Application;
	}

	public String getT24Version() {
		return t24Version;
	}

	public void setT24Version(String t24Version) {
		this.t24Version = t24Version;
	}

	public String getT24Operation() {
		return t24Operation;
	}

	public void setT24Operation(String t24Operation) {
		this.t24Operation = t24Operation;
	}

	public String getT24RspCode() {
		return t24RspCode;
	}

	public void setT24RspCode(String t24RspCode) {
		this.t24RspCode = t24RspCode;
	}

	public String getIn_Text() {
		return in_Text;
	}

	public void setIn_Text(String in_Text) {
		this.in_Text = in_Text;
	}
}
