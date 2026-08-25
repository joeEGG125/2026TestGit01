package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_013101_Form extends BaseForm {
    private String apidddl;
    private String curlbl;

    public UI_013101_Form() {
        super();
    }

    public UI_013101_Form(String apidddl,String curlbl) {
        this.apidddl = apidddl;
        this.curlbl = curlbl;
    }

    public String getApidddl() {
        return apidddl;
    }

    public void setApidddl(String apidddl) {
        this.apidddl = apidddl;
    }

    public String getCurlbl() {
        return curlbl;
    }

    public void setCurlbl(String curlbl) {
        this.curlbl = curlbl;
    }
}
