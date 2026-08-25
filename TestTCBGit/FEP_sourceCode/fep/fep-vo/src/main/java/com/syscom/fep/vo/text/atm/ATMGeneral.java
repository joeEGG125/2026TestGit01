package com.syscom.fep.vo.text.atm;

import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;

public class ATMGeneral {
	private RCV_EATM_GeneralTrans_RQ eatmrequest;
	private ATMGeneralRequest request;
	private ATMGeneralResponse response;

	public ATMGeneral() {
		this.eatmrequest = new RCV_EATM_GeneralTrans_RQ();
		this.request = new ATMGeneralRequest();
		this.response = new ATMGeneralResponse();
	}


	public RCV_EATM_GeneralTrans_RQ getEatmrequest() {
		return eatmrequest;
	}

	public void setEatmrequest(RCV_EATM_GeneralTrans_RQ eatmrequest) {
		this.eatmrequest = eatmrequest;
	}

	public ATMGeneralRequest getRequest() {
		return request;
	}

	public ATMGeneralResponse getResponse() {
		return response;
	}


	public void setRequest(ATMGeneralRequest request) {
		this.request = request;
	}


	public void setResponse(ATMGeneralResponse response) {
		this.response = response;
	}
}
