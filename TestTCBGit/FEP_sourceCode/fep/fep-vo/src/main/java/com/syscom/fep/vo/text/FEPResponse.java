package com.syscom.fep.vo.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class FEPResponse {
	@XStreamAlias("RsHeader")
	private FEPResponseRsHeader rsHeader = new FEPResponseRsHeader();
	@XStreamAlias("SvcRs")
	private FEPResponseSvcRs rs = new FEPResponseSvcRs();

	public FEPResponseRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPResponseRsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public FEPResponseSvcRs getRs() {
		return rs;
	}

	public void setRs(FEPResponseSvcRs rs) {
		this.rs = rs;
	}

	@XStreamAlias("RsHeader")
	public static class FEPResponseRsHeader {
		private String ChlEJNo;
		private String EJNo;
		private String RqTime;
		private String RsTime;
		@XStreamAlias("RsStat")
		private FEPResponseRsHeaderRsStat rsStat = new FEPResponseRsHeaderRsStat();
		@XStreamAlias("Overrides")
		private FEPResponseRsHeaderOverRides overrides = new FEPResponseRsHeaderOverRides();

		public String getChlEJNo() {
			return ChlEJNo;
		}

		public void setChlEJNo(String chlEJNo) {
			this.ChlEJNo = chlEJNo;
		}

		public String getEJNo() {
			return EJNo;
		}

		public void setEJNo(String EJNo) {
			this.EJNo = EJNo;
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

		public FEPResponseRsHeaderRsStat getRsStat() {
			return rsStat;
		}

		public void setRsStat(FEPResponseRsHeaderRsStat rsStat) {
			this.rsStat = rsStat;
		}

		public FEPResponseRsHeaderOverRides getOverrides() {
			return overrides;
		}

		public void setOverrides(FEPResponseRsHeaderOverRides overrides) {
			this.overrides = overrides;
		}
	}

	@XStreamAlias("RsStat")
	public static class FEPResponseRsHeaderRsStat {
		private String Desc;
		private String RsStatCode;

		public String getDesc() {
			return Desc;
		}

		public void setDesc(String desc) {
			this.Desc = desc;
		}

		public String getRsStatCode() {
			return RsStatCode;
		}

		public void setRsStatCode(String rsStatCode) {
			this.RsStatCode = rsStatCode;
		}
	}

	@XStreamAlias("Overrides")
	public static class FEPResponseRsHeaderOverRides {
		@XStreamAlias("Override")
		private FEPResponseRsHeaderOverRidesOverride override;

		public FEPResponseRsHeaderOverRidesOverride getOverride() {
			return override;
		}

		public void setOverride(FEPResponseRsHeaderOverRidesOverride override) {
			this.override = override;
		}
	}

	@XStreamAlias("Override")
	public static class FEPResponseRsHeaderOverRidesOverride {
		private String code;
		private String Text;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getText() {
			return Text;
		}

		public void setText(String text) {
			this.Text = text;
		}
	}

	@XStreamAlias("SvcRs")
	public static class FEPResponseSvcRs {
		private String Rs;

		public String getRs() {
			return Rs;
		}

		public void setRs(String rs) {
			this.Rs = rs;
		}
	}
}
