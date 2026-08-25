package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/13
 */
public class UI_019201_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String lblBankNo;
    private String clearDate;

    public String getLblBankNo() {
        return lblBankNo;
    }

    public void setLblBankNo(String lblBankNo) {
        this.lblBankNo = lblBankNo;
    }

    public String getClearDate() { return clearDate; }

    public void setClearDate(String clearDate) { this.clearDate = clearDate; }
}
