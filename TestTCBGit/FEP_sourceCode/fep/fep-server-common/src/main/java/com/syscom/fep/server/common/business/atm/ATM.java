package com.syscom.fep.server.common.business.atm;

import com.syscom.fep.base.aa.ATMData;

public class ATM extends ATMCheck {
	
	public ATM() {
		super();
	}

	public ATM(ATMData atmMsg, String api) {
		super(atmMsg, api);
	}

	public ATM(ATMData atmMsg) throws Exception {
		super(atmMsg);
	}
}