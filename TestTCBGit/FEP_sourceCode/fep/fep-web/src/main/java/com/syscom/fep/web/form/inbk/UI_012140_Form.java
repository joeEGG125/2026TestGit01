package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_012140_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String tradingDate;
    private String pcode;
    private String oribknotxt;
    private String oristantxt;
    private String txatmtxt;
    private String troutbknotxt;
    private String contentMsg;
    private String troutactnotxt;
    private String ecinstructionddl;
    private String oridatetimetxt;
    private String merchantid;
    private String atmno;
    private String queryok;

    public UI_012140_Form() {
        super();
    }

    public UI_012140_Form(String tradingDate,String pcode,String oribknotxt,String oristantxt,String txatmtxt,String troutbknotxt,String contentMsg,String troutactnotxt,String ecinstructionddl,
                          String oridatetimetxt,String merchantid,String atmno) {
        this.tradingDate = tradingDate;
        this.pcode = pcode;
        this.oribknotxt = oribknotxt;
        this.oristantxt = oristantxt;
        this.txatmtxt = txatmtxt;
        this.troutbknotxt = troutbknotxt;
        this.contentMsg = contentMsg;
        this.troutactnotxt = troutactnotxt;
        this.ecinstructionddl = ecinstructionddl;
        this.oridatetimetxt = oridatetimetxt;
        this.merchantid = merchantid;
        this.atmno = atmno;
        this.queryok = queryok;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
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

    public String getContentMsg() {
        return contentMsg;
    }

    public void setContentMsg(String contentMsg) {
        this.contentMsg = contentMsg;
    }

    public String getTroutactnotxt() {
        return troutactnotxt;
    }

    public void setTroutactnotxt(String troutactnotxt) {
        this.troutactnotxt = troutactnotxt;
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

    public String getQueryok() {
        return queryok;
    }

    public void setQueryok(String queryok) {
        this.queryok = queryok;
    }
}
