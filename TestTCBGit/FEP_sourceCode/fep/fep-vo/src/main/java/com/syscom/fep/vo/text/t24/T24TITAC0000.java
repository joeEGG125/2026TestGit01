package com.syscom.fep.vo.text.t24;

import java.util.HashMap;

public class T24TITAC0000 {
    // IsPassed Part------------------------------------------------------------------
    public boolean IsPROC_TXN_STSPassed;
    public boolean IsPROC_RET_CODEPassed;
    public boolean IsPROC_APP_NAMEPassed;
    public boolean IsPROC_APP_IDPassed;
    public boolean IsPROC_DATEPassed;
    public boolean IsPROC_TIMEPassed;

    // Prviate part ------------------------------------------------------------------
    private String _PROC_TXN_STS;
    private String _PROC_RET_CODE;
    private String _PROC_APP_NAME;
    private String _PROC_APP_ID;
    private String _PROC_DATE;
    private String _PROC_TIME;

    // Property part -----------------------------------------------------------------
    public String getProcTxnSts() {
        return _PROC_TXN_STS;
    }
    public void setProcTxnSts(String value) {
        _PROC_TXN_STS = value;
        IsPROC_TXN_STSPassed = true;
    }

    public String getProcRetCode() {
        return _PROC_RET_CODE;
    }
    public void setProcRetCode(String value) {
        _PROC_RET_CODE = value;
        IsPROC_RET_CODEPassed = true;
    }

    public String getProcAppName() {
        return _PROC_APP_NAME;
    }
    public void setProcAppName(String value) {
        _PROC_APP_NAME = value;
        IsPROC_APP_NAMEPassed = true;
    }

    public String getProcAppId() {
        return _PROC_APP_ID;
    }
    public void setProcAppId(String value) {
        _PROC_APP_ID = value;
        IsPROC_APP_IDPassed = true;
    }

    public String getProcDate() {
        return _PROC_DATE;
    }
    public void setProcDate(String value) {
        _PROC_DATE = value;
        IsPROC_DATEPassed = true;
    }

    public String getProcTime() {
        return _PROC_TIME;
    }
    public void setProcTime(String value) {
        _PROC_TIME = value;
        IsPROC_TIMEPassed = true;
    }

    public boolean genDictionary(HashMap<String, String> TITABody) {
        if (IsPROC_TXN_STSPassed) {
            TITABody.put("PROC.TXN.STS", getProcTxnSts());
        }

        if (IsPROC_RET_CODEPassed) {
            TITABody.put("PROC.RET.CODE", getProcRetCode());
        }

        if (IsPROC_APP_NAMEPassed) {
            TITABody.put("PROC.APP.NAME", getProcAppName());
        }

        if (IsPROC_APP_IDPassed) {
            TITABody.put("PROC.APP.ID", getProcAppId());
        }

        if (IsPROC_DATEPassed) {
            TITABody.put("PROC.DATE", getProcDate());
        }

        if (IsPROC_TIMEPassed) {
            TITABody.put("PROC.TIME", getProcTime());
        }

        return true;
    }
}
