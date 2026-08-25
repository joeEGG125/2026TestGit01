package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019140_Form extends BaseForm {
    private String datetime;
    private String inbkpendPcode;

    public UI_019140_Form() {
        super();
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getInbkpendPcode() {
        return inbkpendPcode;
    }

    public void setInbkpendPcode(String inbkpendPcode) {
        this.inbkpendPcode = inbkpendPcode;
    }

    public UI_019140_Form(String datetime, String inbkpendPcode) {
        this.datetime = datetime;
        this.inbkpendPcode = inbkpendPcode;
    }


}
