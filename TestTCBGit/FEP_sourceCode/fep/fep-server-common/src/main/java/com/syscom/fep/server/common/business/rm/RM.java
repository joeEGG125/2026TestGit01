package com.syscom.fep.server.common.business.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.aa.T24Data;

public class RM extends RMCheck {

	public RM() {
		super();
	}

	public RM(RMData rmTxMsg) {
		super(rmTxMsg);
	}

	public RM(T24Data t24Msg) {
		super(t24Msg);
	}

}
