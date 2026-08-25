package com.syscom.fep.vo.text.rm;

public class RMGeneral {
	private RMGeneralRequest request;
	private RMGeneralResponse response;

	public RMGeneral() {
		this.request = new RMGeneralRequest();
		this.response = new RMGeneralResponse();
	}

	public RMGeneralRequest getRequest() {
		return request;
	}

	public RMGeneralResponse getResponse() {
		return response;
	}
}
