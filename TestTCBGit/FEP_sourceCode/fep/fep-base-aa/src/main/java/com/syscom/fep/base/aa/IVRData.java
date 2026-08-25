package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.ivr.IVRGeneral;
import com.syscom.fep.vo.text.ivr.request.IVRRequest;
import com.syscom.fep.vo.text.ivr.response.IVRResponse;

public class IVRData extends MessageBase {
	private IVRGeneral txObject;
	private IVRRequest request;
	private IVRResponse response;

	public IVRGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(IVRGeneral txObject) {
		this.txObject = txObject;
	}

	public IVRRequest getRequest() {
		return request;
	}

	public void setRequest(IVRRequest request) {
		this.request = request;
	}

	public IVRResponse getResponse() {
		return response;
	}

	public void setResponse(IVRResponse response) {
		this.response = response;
	}

	public ATMGeneral getAtmtxObject() { return atmtxObject; }

	public void setAtmtxObject(ATMGeneral atmtxObject) {
		this.atmtxObject = atmtxObject;
	}

	private ATMGeneral atmtxObject;

}
