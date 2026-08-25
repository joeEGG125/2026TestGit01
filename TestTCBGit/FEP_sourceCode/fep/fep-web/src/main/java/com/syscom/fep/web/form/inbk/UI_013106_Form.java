package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

public class UI_013106_Form extends BaseForm {

    private String aPIDCob;
    private String cURDdl;

    public String getcURDdl() { return cURDdl; }

    public void setcURDdl(String cURDdl) { this.cURDdl = cURDdl; }

    public String getaPIDCob() { return aPIDCob; }
    private UI_013106_Form.RadioOption radioOption = RadioOption.EXCHECKINRBN;
    public static enum RadioOption {EXCHECKINRBN, EXCHECKOUTRBN}


    public void setaPIDCob(String aPIDCob) { this.aPIDCob = aPIDCob; }

    public RadioOption getRadioOption() { return radioOption; }

    public void setRadioOption(RadioOption radioOption) { this.radioOption = radioOption; }



}
