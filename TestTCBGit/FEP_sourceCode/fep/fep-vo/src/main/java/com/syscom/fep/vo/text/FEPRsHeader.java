package com.syscom.fep.vo.text;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("RsHeader")
public class FEPRsHeader {
	private String ChlEJNo = StringUtils.EMPTY;
	private String EJNo = StringUtils.EMPTY;
	private String RqTime = StringUtils.EMPTY;
	private String RsTime = StringUtils.EMPTY;
	@XStreamAlias("RsStat")
	private FEPRsHeaderRsStat rsStat;
	@XStreamAlias("Overrides")
	private FEPRsHeaderOverride[] overrides;

	public String getChlEJNo() {
		return ChlEJNo;
	}

	public void setChlEJNo(String chlEJNo) {
		this.ChlEJNo = chlEJNo;
	}

	public String getEJNo() {
		return EJNo;
	}

	public void setEJNo(String ejNo) {
		this.EJNo = ejNo;
	}

	public String getRqTime() {
		return RqTime;
	}

	public void setRqTime(String rqTime) {
		this.RqTime = rqTime;
	}

	public String getRsTime() {
		return RsTime;
	}

	public void setRsTime(String rsTime) {
		this.RsTime = rsTime;
	}

	public FEPRsHeaderRsStat getRsStat() {
		return rsStat;
	}

	public void setRsStat(FEPRsHeaderRsStat rsStat) {
		this.rsStat = rsStat;
	}

	public FEPRsHeaderOverride[] getOverrides() {
		return overrides;
	}

	public void setOverrides(FEPRsHeaderOverride[] overrides) {
		this.overrides = overrides;
	}

	@XStreamAlias("RsStat")
	public static class FEPRsHeaderRsStat {
		private String Desc;
		@XStreamAlias("RsStatCode")
		private FEPRsHeaderRsStatRsStatCode rsStatCode;

		public String getDesc() {
			return Desc;
		}

		public void setDesc(String desc) {
			this.Desc = desc;
		}

		public FEPRsHeaderRsStatRsStatCode getRsStatCode() {
			return rsStatCode;
		}

		public void setRsStatCode(FEPRsHeaderRsStatRsStatCode rsStatCode) {
			this.rsStatCode = rsStatCode;
		}
	}

	@XStreamAlias("RsStatCode")
	public static class FEPRsHeaderRsStatRsStatCode {
		private String type;
		private String Value;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return Value;
		}

		public void setValue(String value) {
			this.Value = value;
		}
	}

	@XStreamAlias("Override")
	public static class FEPRsHeaderOverride {
		private String code;
		private String Value;

		public String getCode() {
			return this.code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getValue() {
			return this.Value;
		}

		public void setValue(String value) {
			this.Value = value;
		}
	}
}
