package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;

/**
 * @author Richard
 */
public class EMVRequestI3 extends INBKAABase {

	public EMVRequestI3(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() throws Exception {
		throw ExceptionUtil.createNotImplementedException(ProgramName, "尚未實作!!!");
	}
}
