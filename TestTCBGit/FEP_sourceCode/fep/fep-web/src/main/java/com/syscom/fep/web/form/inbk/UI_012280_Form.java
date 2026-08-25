package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012280_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String tradingDate;
    private String bkno;
    private String stan;
    private String queryflagtxt;
    private String pcodetxt;
    private String tbsdytxt;
    private String txatmtxt;
    private String troutbknotxt;
    private String troutactnotxt;
    private String trinbknotxt;
    private String trinactnotxt;
    private String cardnotxt;
    private String trinactnoactualtxt;
    private String atmnotxt;

    public UI_012280_Form() {
        super();
    }

    public UI_012280_Form(String tradingDate,String bkno,String stan,String queryflagtxt,String pcodetxt,String tbsdytxt,String txatmtxt,String troutbknotxt,String troutactnotxt,
                          String trinbknotxt,String trinactnotxt,String cardnotxt,String trinactnoactualtxt,String atmnotxt) {
        this.tradingDate = tradingDate;
        this.bkno = bkno;
        this.stan = stan;
        this.queryflagtxt = queryflagtxt;
        this.pcodetxt = pcodetxt;
        this.tbsdytxt = tbsdytxt;
        this.txatmtxt = txatmtxt;
        this.troutbknotxt = troutbknotxt;
        this.troutactnotxt = troutactnotxt;
        this.trinbknotxt = trinbknotxt;
        this.trinactnotxt = trinactnotxt;
        this.cardnotxt = cardnotxt;
        this.trinactnoactualtxt = trinactnoactualtxt;
        this.atmnotxt = atmnotxt;
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getPcodetxt() {
        return pcodetxt;
    }

    public void setPcodetxt(String pcodetxt) {
        this.pcodetxt = pcodetxt;
    }

    public String getTbsdytxt() {
        return tbsdytxt;
    }

    public void setTbsdytxt(String tbsdytxt) {
        this.tbsdytxt = tbsdytxt;
    }

    public String getTxatmtxt() {
        return txatmtxt;
    }

    public void setTxatmtxt(String txatmtxt) {
        this.txatmtxt = txatmtxt;
    }

    public String getTroutbknotxt() {
        return troutbknotxt;
    }

    public void setTroutbknotxt(String troutbknotxt) {
        this.troutbknotxt = troutbknotxt;
    }

    public String getTroutactnotxt() {
        return troutactnotxt;
    }

    public void setTroutactnotxt(String troutactnotxt) {
        this.troutactnotxt = troutactnotxt;
    }

    public String getTrinbknotxt() {
        return trinbknotxt;
    }

    public void setTrinbknotxt(String trinbknotxt) {
        this.trinbknotxt = trinbknotxt;
    }

    public String getTrinactnotxt() {
        return trinactnotxt;
    }

    public void setTrinactnotxt(String trinactnotxt) {
        this.trinactnotxt = trinactnotxt;
    }

    public String getCardnotxt() {
        return cardnotxt;
    }

    public void setCardnotxt(String cardnotxt) {
        this.cardnotxt = cardnotxt;
    }

    public String getTrinactnoactualtxt() {
        return trinactnoactualtxt;
    }

    public void setTrinactnoactualtxt(String trinactnoactualtxt) {
        this.trinactnoactualtxt = trinactnoactualtxt;
    }

    public String getAtmnotxt() {
        return atmnotxt;
    }

    public void setAtmnotxt(String atmnotxt) {
        this.atmnotxt = atmnotxt;
    }
}
