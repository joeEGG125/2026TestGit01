package com.syscom.fep.vo.text.hce;

public class HCEGeneral {
    private RCV_HCE_GeneralTrans_RQ request;
    private SEND_HCE_GeneralTrans_RS response;

	public RCV_HCE_GeneralTrans_RQ getRequest() {
		return request;
	}

	public void setRequest(RCV_HCE_GeneralTrans_RQ request) {
		this.request = request;
	}

	public SEND_HCE_GeneralTrans_RS getResponse() {
		return response;
	}

	public void setResponse(SEND_HCE_GeneralTrans_RS response) {
		this.response = response;
	}
    
}
