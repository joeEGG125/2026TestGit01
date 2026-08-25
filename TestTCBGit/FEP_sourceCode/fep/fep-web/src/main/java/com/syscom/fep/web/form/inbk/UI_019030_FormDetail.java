package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019030_FormDetail extends BaseForm {
    private String queryok;
    private String feptxnPcode;
    private String tradingDate;
    private String feptxnTbsdyFisc;
    private String rcrbl;
    private Long ejnotxt;
    private String atmrctxt;
    private String msgid;

    public UI_019030_FormDetail() {
        super();
    }

    public UI_019030_FormDetail(String queryok,String feptxnPcode,String tradingDate,String feptxnTbsdyFisc,String rcrbl,Long ejnotxt,String atmrctxt,String msgid){
        this.queryok = queryok;
        this.feptxnPcode = feptxnPcode;
        this.tradingDate = tradingDate;
        this.feptxnTbsdyFisc = feptxnTbsdyFisc;
        this.rcrbl = rcrbl;
        this.ejnotxt = ejnotxt;
        this.atmrctxt = atmrctxt;
        this.msgid = msgid;
    }

    public String getQueryok() {
        return queryok;
    }

    public void setQueryok(String queryok) {
        this.queryok = queryok;
    }

    public String getFeptxnPcode() {
        return feptxnPcode;
    }

    public void setFeptxnPcode(String feptxnPcode) {
        this.feptxnPcode = feptxnPcode;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getFeptxnTbsdyFisc() {
        return feptxnTbsdyFisc;
    }

    public void setFeptxnTbsdyFisc(String feptxnTbsdyFisc) {
        this.feptxnTbsdyFisc = feptxnTbsdyFisc;
    }

    public String getRcrbl() {
        return rcrbl;
    }

    public void setRcrbl(String rcrbl) {
        this.rcrbl = rcrbl;
    }

    public Long getEjnotxt() {
        return ejnotxt;
    }

    public void setEjnotxt(Long ejnotxt) {
        this.ejnotxt = ejnotxt;
    }

    public String getAtmrctxt() {
        return atmrctxt;
    }

    public void setAtmrctxt(String atmrctxt) {
        this.atmrctxt = atmrctxt;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
