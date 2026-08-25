package com.syscom.fep.vo.text.webatm;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 2021-11-12 Richard add
 * 
 * @author Richard
 *
 */
@XStreamAlias("FEP")
public class WebATMResponse {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private WebATMRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public WebATMRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(WebATMRs svcRs) {
		this.svcRs = svcRs;
	}

	@XStreamAlias("SvcRs")
	public static class WebATMRs {
		@XStreamAlias("Rs")
		private String rs;

		public String getRs() {
			return rs;
		}

		public void setRs(String rs) {
			this.rs = rs;
		}
	}
}
