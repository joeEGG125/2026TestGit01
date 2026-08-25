package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.nb.NBFEPGeneral;

public class NBFEPData extends MessageBase {

	private NBFEPGeneral txObject;

	public NBFEPGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(NBFEPGeneral txObject) {
		this.txObject = txObject;
	}


}
