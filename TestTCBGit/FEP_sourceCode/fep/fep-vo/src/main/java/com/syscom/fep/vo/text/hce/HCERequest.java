package com.syscom.fep.vo.text.hce;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 */
@XStreamAlias("FEP")
public class HCERequest {
	@XStreamAlias("Header")
	private RCV_HCE_GeneralTrans_RQ rqHeader;
	@XStreamAlias("SvcRq")
	private RCV_HCE_GeneralTrans_RQ svcRq;
	
	public RCV_HCE_GeneralTrans_RQ getRqHeader() {
		return rqHeader;
	}
	public void setRqHeader(RCV_HCE_GeneralTrans_RQ rqHeader) {
		this.rqHeader = rqHeader;
	}
//	public HCERequestq getSvcRq() {
//		return svcRq;
//	}
//	public void setSvcRq(HCERequestq svcRq) {
//		this.svcRq = svcRq;
//	}
	public RCV_HCE_GeneralTrans_RQ getSvcRq() {
		return svcRq;
	}
	public void setSvcRq(RCV_HCE_GeneralTrans_RQ svcRq) {
		this.svcRq = svcRq;
	}
	
	


//	@XStreamAlias("SvcRq")
//	public static class HCERequestq {
//		@XStreamAlias("Rq")
//		private String rq;
//
//		public String getRq() {
//			return rq;
//		}
//
//		public void setRq(String rq) {
//			this.rq = rq;
//		}
//	}

}
