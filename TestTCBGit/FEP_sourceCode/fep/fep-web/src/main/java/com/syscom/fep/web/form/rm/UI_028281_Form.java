package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

public class UI_028281_Form extends BaseForm {
    private String txDate;
    private String rmInTxDate;
    private String rmInBrno;
    private String rmInFepNo;

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getRmInTxDate() {
        return rmInTxDate;
    }

    public void setRmInTxDate(String rmInTxDate) {
        this.rmInTxDate = rmInTxDate;
    }

    public String getRmInBrno() {
        return rmInBrno;
    }

    public void setRmInBrno(String rmInBrno) {
        this.rmInBrno = rmInBrno;
    }

    public String getRmInFepNo() {
        return rmInFepNo;
    }

    public void setRmInFepNo(String rmInFepNo) {
        this.rmInFepNo = rmInFepNo;
    }
}