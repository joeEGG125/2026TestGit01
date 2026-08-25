package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;

/**
 * @author Richard
 */
public class EMVRequestJA extends INBKAABase {

	public EMVRequestJA(ATMData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		throw ExceptionUtil.createNotImplementedException(ProgramName, "尚未實作!!!");
	}
}
