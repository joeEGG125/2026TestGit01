package com.syscom.fep.vo.text.fcs;

public class FCSGeneral {
	private FCSGeneralRequest request;
	private FCSGeneralResponse response;

	public FCSGeneral() {
		this.request = new FCSGeneralRequest();
		this.response = new FCSGeneralResponse();
	}

	public FCSGeneralRequest getRequest() {
		return request;
	}

	public FCSGeneralResponse getResponse() {
		return response;
	}
}
