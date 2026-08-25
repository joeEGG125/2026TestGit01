package com.syscom.fep.vo.text.ivr;

public class IVRGeneral {
	private RCV_VO_GeneralTrans_RQ request;
	private SEND_VO_GeneralTrans_RS response;

	public IVRGeneral() {
		request = new RCV_VO_GeneralTrans_RQ();
		response = new SEND_VO_GeneralTrans_RS();
	}

	public RCV_VO_GeneralTrans_RQ getRequest() {
		return request;
	}

	public void setRequest(RCV_VO_GeneralTrans_RQ request) {
		this.request = request;
	}

	public SEND_VO_GeneralTrans_RS getResponse() {
		return response;
	}

	public void setResponse(SEND_VO_GeneralTrans_RS response) {
		this.response = response;
	}
}
