package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012140_FormDetail extends BaseForm {
    private String troutactnotxt;
    private String pcode;
    private String txatmtxt;
    private String tradingDate;
    private String oribknotxt;
    private String oristantxt;
    private String troutbknotxt;
    private String ecinstructionddl;
    private String oridatetimetxt;
    private String merchantid;
    private String atmno;
    private String queryok;

    public UI_012140_FormDetail() {
        super();
    }

    public UI_012140_FormDetail(String tradingDate,String pcode,String oribknotxt,String oristantxt,String txatmtxt,String troutbknotxt,String troutactnotxt,String ecinstructionddl,
                          String oridatetimetxt,String merchantid,String atmno,String queryok) {
        this.tradingDate = tradingDate;
        this.pcode = pcode;
        this.oribknotxt = oribknotxt;
        this.oristantxt = oristantxt;
        this.txatmtxt = txatmtxt;
        this.troutbknotxt = troutbknotxt;
        this.troutactnotxt = troutactnotxt;
        this.ecinstructionddl = ecinstructionddl;
        this.oridatetimetxt = oridatetimetxt;
        this.merchantid = merchantid;
        this.atmno = atmno;
        this.queryok = queryok;
    }

    public String getQueryok() {
        return queryok;
    }

    public void setQueryok(String queryok) {
        this.queryok = queryok;
    }

    public String getTroutactnotxt() {
        return troutactnotxt;
    }

    public void setTroutactnotxt(String troutactnotxt) {
        this.troutactnotxt = troutactnotxt;
    }

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public String getTxatmtxt() {
        return txatmtxt;
    }

    public void setTxatmtxt(String txatmtxt) {
        this.txatmtxt = txatmtxt;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
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

    public String getTroutbknotxt() {
        return troutbknotxt;
    }

    public void setTroutbknotxt(String troutbknotxt) {
        this.troutbknotxt = troutbknotxt;
    }

    public String getEcinstructionddl() {
        return ecinstructionddl;
    }

    public void setEcinstructionddl(String ecinstructionddl) {
        this.ecinstructionddl = ecinstructionddl;
    }

    public String getOridatetimetxt() {
        return oridatetimetxt;
    }

    public void setOridatetimetxt(String oridatetimetxt) {
        this.oridatetimetxt = oridatetimetxt;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getAtmno() {
        return atmno;
    }

    public void setAtmno(String atmno) {
        this.atmno = atmno;
    }
}
