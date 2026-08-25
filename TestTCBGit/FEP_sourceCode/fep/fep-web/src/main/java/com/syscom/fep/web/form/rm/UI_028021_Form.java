package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author Chen_yu
 * @create 2021/10/09
 */

public class UI_028021_Form extends BaseForm {
    //原交易類別
    private String orgpcodeddl;
    //原交易金額
    private String orgtxamt;
    //原電文序號
    private String orgfiscno;
    //原通匯序號
    private String orgrmsno;
    //原解款行代號
    private String orgreceiverbank;
    //原交易類別
    private String orgpcode;
    //原交易序號
    private String orgstan;

    public String getOrgpcodeddl() {
        return orgpcodeddl;
    }

    public void setOrgpcodeddl(String orgpcodeddl) {
        this.orgpcodeddl = orgpcodeddl;
    }

    public String getOrgtxamt() {
        return orgtxamt;
    }

    public void setOrgtxamt(String orgtxamt) {
        this.orgtxamt = orgtxamt;
    }

    public String getOrgfiscno() {
        return orgfiscno;
    }

    public void setOrgfiscno(String orgfiscno) {
        this.orgfiscno = orgfiscno;
    }

    public String getOrgrmsno() {
        return orgrmsno;
    }

    public void setOrgrmsno(String orgrmsno) {
        this.orgrmsno = orgrmsno;
    }

    public String getOrgreceiverbank() {
        return orgreceiverbank;
    }

    public void setOrgreceiverbank(String orgreceiverbank) {
        this.orgreceiverbank = orgreceiverbank;
    }

    public String getOrgpcode() {
        return orgpcode;
    }

    public void setOrgpcode(String orgpcode) {
        this.orgpcode = orgpcode;
    }

    public String getOrgstan() {
        return orgstan;
    }

    public void setOrgstan(String orgstan) {
        this.orgstan = orgstan;
    }
}
