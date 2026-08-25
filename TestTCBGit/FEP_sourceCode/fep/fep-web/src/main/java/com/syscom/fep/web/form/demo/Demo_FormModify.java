package com.syscom.fep.web.form.demo;

import com.syscom.fep.web.form.BaseForm;

public class Demo_FormModify extends BaseForm {
    private static final long serialVersionUID = 1L;
    private Long feptxnEjfno;
    private String feptxnReqRc;

    public Long getFeptxnEjfno() {
        return feptxnEjfno;
    }

    public void setFeptxnEjfno(Long feptxnEjfno) {
        this.feptxnEjfno = feptxnEjfno;
    }

    public String getFeptxnReqRc() {
        return feptxnReqRc;
    }

    public void setFeptxnReqRc(String feptxnReqRc) {
        this.feptxnReqRc = feptxnReqRc;
    }
}
