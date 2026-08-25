package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.FEPMessage;
import com.syscom.fep.vo.text.FEPMessageNew;
import com.syscom.fep.vo.text.FEPRequest;
import com.syscom.fep.vo.text.FEPResponse;
import com.syscom.fep.vo.text.rm.RMGeneral;

public class RMData extends MessageBase {
	private RMGeneral txObject;
	private FEPMessage rmMessage;
	private FEPMessageNew rmMessageFEP;
	private FEPRequest fepRequest;
	private FEPResponse fepResponse;

	public RMGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(RMGeneral txObject) {
		this.txObject = txObject;
	}

	public FEPMessage getRmMessage() {
		return rmMessage;
	}

	public void setRmMessage(FEPMessage fepMessage) {
		this.rmMessage = fepMessage;
	}

	public FEPMessageNew getRmMessageFEP() {
		return rmMessageFEP;
	}

	public void setRmMessageFEP(FEPMessageNew rmMessageFEP) {
		this.rmMessageFEP = rmMessageFEP;
	}

	public FEPRequest getFepRequest() {
		return fepRequest;
	}

	public void setFepRequest(FEPRequest fepRequest) {
		this.fepRequest = fepRequest;
	}

	public FEPResponse getFepResponse() {
		return fepResponse;
	}

	public void setFepResponse(FEPResponse fepResponse) {
		this.fepResponse = fepResponse;
	}
}
