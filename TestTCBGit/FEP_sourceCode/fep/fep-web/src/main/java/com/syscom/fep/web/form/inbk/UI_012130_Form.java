package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012130_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String tradingDate;
    private String bkno;
    private String stan;
    private String queryok;

    public UI_012130_Form() {
        super();
    }

    public UI_012130_Form(String tradingDate,String bkno,String stan,String queryok) {
        this.tradingDate = tradingDate;
        this.bkno = bkno;
        this.stan = stan;
        this.queryok = queryok;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getQueryok() {
        return queryok;
    }

    public void setQueryok(String queryok) {
        this.queryok = queryok;
    }
}
