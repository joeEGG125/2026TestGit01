package com.syscom.fep.vo.text.inbk;

import com.syscom.fep.vo.text.inbk.response.S0710Response;

public class INBKGeneral {
	private INBKGeneralRequest mRequest;
	private INBKGeneralResponse mResponse;
	private S0710Response.S0710SvcRs mS0710_SvcRs;

	public INBKGeneral() {
		this.mRequest = new INBKGeneralRequest();
		this.mResponse = new INBKGeneralResponse();
	}

	public INBKGeneralRequest getRequest() {
		return mRequest;
	}

	public INBKGeneralResponse getResponse() {
		return mResponse;
	}

	/**
	 * S0710的回應電文Body部分
	 * 
	 * @return
	 */
	public S0710Response.S0710SvcRs getS0710SvcRs() {
		return mS0710_SvcRs;
	}

	public void setS0710SvcRs(S0710Response.S0710SvcRs value) {
		mS0710_SvcRs = value;
	}
}
