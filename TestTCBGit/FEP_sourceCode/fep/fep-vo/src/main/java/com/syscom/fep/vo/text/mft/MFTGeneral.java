package com.syscom.fep.vo.text.mft;

public class MFTGeneral {
	private MFTGeneralRequest request;
	private MFTGeneralResponse response;

	public MFTGeneral() {
		this.request = new MFTGeneralRequest();
		this.response = new MFTGeneralResponse();
	}

	public MFTGeneralRequest getRequest() {
		return request;
	}

	public void setRequest(MFTGeneralRequest request) {
		this.request = request;
	}

	public MFTGeneralResponse getResponse() {
		return response;
	}

	public void setResponse(MFTGeneralResponse response) {
		this.response = response;
	}

}
