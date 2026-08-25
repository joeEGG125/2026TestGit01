package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/13
 */
public class UI_019102_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String lblBankNo;
    private String clrtotalStDate;
    private String lblTxTime;
    private String lblBknoStan;

    public String getLblTxTime() { return lblTxTime; }

    public void setLblTxTime(String lblTxTime) { this.lblTxTime = lblTxTime; }

    public String getLblBknoStan() { return lblBknoStan; }

    public void setLblBknoStan(String lblBknoStan) { this.lblBknoStan = lblBknoStan; }

    public String getLblBankNo() {
        return lblBankNo;
    }

    public void setLblBankNo(String lblBankNo) {
        this.lblBankNo = lblBankNo;
    }

    public String getClrtotalStDate() { return clrtotalStDate; }

    public void setClrtotalStDate(String clrtotalStDate) { this.clrtotalStDate = clrtotalStDate; }
}
