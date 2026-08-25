package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012130_FormDetail extends BaseForm {
    //處理結果
    private String prcresultddl;
    //原交易日期
    private String oritxdatetxt;
    //原入帳日期
    private String oritbsdyfisctxt;
    //原交易種類
    private String pcodetxt;
    //原交易金額
    private String txatmtxt;
    //原扣款帳號
    private String troutbknotxt;
    private String troutactnotxt;
    //原轉入帳號
    private String trinbknotxt;
    private String trinactnotxr;
    //原卡片帳號
    private String cardnotxt;
    //原入帳帳號
    private String trinactnoactualtxt;
    //原ATM代號
    private String atmnotxt;
    //財金STAN
    private String oribknotxt;
    private String oristantxt;

    private String queryok;

    public UI_012130_FormDetail() {
        super();
    }

    public UI_012130_FormDetail(String prcresultddl,String oritxdatetxt,String oritbsdyfisctxt,String pcodetxt,String txatmtxt,String troutbknotxt,String troutactnotxt,String trinbknotxt,
            String trinactnotxr,String cardnotxt,String trinactnoactualtxt,String atmnotxt,String oribknotxt,String oristantxt,String queryok){
        this.prcresultddl = prcresultddl;
        this.oritxdatetxt = oritxdatetxt;
        this.oritbsdyfisctxt = oritbsdyfisctxt;
        this.pcodetxt = pcodetxt;
        this.txatmtxt = txatmtxt;
        this.troutbknotxt = troutbknotxt;
        this.troutactnotxt = troutactnotxt;
        this.trinbknotxt = trinbknotxt;
        this.trinactnotxr = trinactnotxr;
        this.cardnotxt = cardnotxt;
        this.trinactnoactualtxt = trinactnoactualtxt;
        this.atmnotxt = atmnotxt;
        this.oribknotxt = oribknotxt;
        this.oristantxt = oristantxt;
        this.queryok = queryok;
    }

    public String getPrcresultddl() {
        return prcresultddl;
    }

    public void setPrcresultddl(String prcresultddl) {
        this.prcresultddl = prcresultddl;
    }

    public String getOritxdatetxt() {
        return oritxdatetxt;
    }

    public void setOritxdatetxt(String oritxdatetxt) {
        this.oritxdatetxt = oritxdatetxt;
    }

    public String getOritbsdyfisctxt() {
        return oritbsdyfisctxt;
    }

    public void setOritbsdyfisctxt(String oritbsdyfisctxt) {
        this.oritbsdyfisctxt = oritbsdyfisctxt;
    }

    public String getPcodetxt() {
        return pcodetxt;
    }

    public void setPcodetxt(String pcodetxt) {
        this.pcodetxt = pcodetxt;
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

    public String getTrinactnotxr() {
        return trinactnotxr;
    }

    public void setTrinactnotxr(String trinactnotxr) {
        this.trinactnotxr = trinactnotxr;
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

    public String getOribknotxt() {
        return oribknotxt;
    }

    public void setOribknotxt(String oribknotxt) {
        this.oribknotxt = oribknotxt;
    }

    public String getOristantxt() {
        return oristantxt;
    }

    public void setOristantxt(String oristantxt) {
        this.oristantxt = oristantxt;
    }

    public String getQueryok() {
        return queryok;
    }

    public void setQueryok(String queryok) {
        this.queryok = queryok;
    }
}
