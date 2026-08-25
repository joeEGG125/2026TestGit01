package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

public class UI_028290_Form extends BaseForm {
    private String ddlRM_STAT;
    private String rMFLAGTxt;
    private String executeBtnEnabled;

    public String getExecuteBtnEnabled() { return executeBtnEnabled; }

    public void setExecuteBtnEnabled(String executeBtnEnabled) { this.executeBtnEnabled = executeBtnEnabled; }

    public String getrMFLAGTxt() { return rMFLAGTxt; }

    public void setrMFLAGTxt(String rMFLAGTxt) {
        this.rMFLAGTxt = rMFLAGTxt;
    }

    public String getDdlRM_STAT() {
        return ddlRM_STAT;
    }

    public void setDdlRM_STAT(String ddlRM_STAT) {
        this.ddlRM_STAT = ddlRM_STAT;
    }
}
