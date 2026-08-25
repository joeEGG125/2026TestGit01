package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author chen_yu
 * @create 2021/12/06
 */
public class UI_028090_Form extends BaseForm {
    private String kind;
    private String tradingDate;
    private String senderBank;
    private String ejfno;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getSenderBank() {
        return senderBank;
    }

    public void setSenderBank(String senderBank) {
        this.senderBank = senderBank;
    }

    public String getEjfno() {
        return ejfno;
    }

    public void setEjfno(String ejfno) {
        this.ejfno = ejfno;
    }
}
