package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

/**
 * @author Richard
 */
public class ATMControl extends ATMPAABase {

    public ATMControl(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 處理電文
     *
     * @return
     */
    @Override
    public String processRequestData() throws Exception {
        throw ExceptionUtil.createNotImplementedException(ProgramName, "尚未實作!!!");
    }
}
