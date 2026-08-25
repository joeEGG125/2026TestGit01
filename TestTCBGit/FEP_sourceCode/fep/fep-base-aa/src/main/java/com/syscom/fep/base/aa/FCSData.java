package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.fcs.FCSGeneral;

public class FCSData extends MessageBase {
	private FCSGeneral txObject;

	public FCSGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(FCSGeneral txObject) {
		this.txObject = txObject;
	}
}
