package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_019130_Form extends BaseForm {
    private String datetime;

    public UI_019130_Form() {
        super();
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public UI_019130_Form(String datetime) {
        this.datetime = datetime;
    }


}
