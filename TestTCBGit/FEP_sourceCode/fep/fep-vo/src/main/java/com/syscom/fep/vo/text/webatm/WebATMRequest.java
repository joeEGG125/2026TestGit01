package com.syscom.fep.vo.text.webatm;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 2021-11-12 Richard add
 * 
 * @author Richard
 *
 */
@XStreamAlias("FEP")
public class WebATMRequest {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private WebATMRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public WebATMRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(WebATMRq svcRq) {
		this.svcRq = svcRq;
	}

	@XStreamAlias("SvcRq")
	public static class WebATMRq {
		@XStreamAlias("Rq")
		private String rq;

		public String getRq() {
			return rq;
		}

		public void setRq(String rq) {
			this.rq = rq;
		}
	}
}
