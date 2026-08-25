package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012290_FormDetail extends BaseForm {
    private String tradingDate;
    private String bkno;
    private String stan;
    private String queryflagtxt;

    public UI_012290_FormDetail() {
        super();
    }

    public UI_012290_FormDetail(String tradingDate,String bkno,String stan,String queryflagtxt) {
        this.tradingDate = tradingDate;
        this.bkno = bkno;
        this.stan = stan;
        this.queryflagtxt = queryflagtxt;
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

    public String getQueryflagtxt() {
        return queryflagtxt;
    }

    public void setQueryflagtxt(String queryflagtxt) {
        this.queryflagtxt = queryflagtxt;
    }
}
