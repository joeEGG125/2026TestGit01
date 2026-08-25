package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/24
 */
public class UI_028010_Form extends BaseForm {

    /**
     * 收信行代號
     */
    private String receiverBank;

    /**
     * 中英文內容
     */
    private String chnmemo;

    /**
     `* 英文內容`
     */
    private String engmemo;

    public String getReceiverBank() { return receiverBank; }

    public void setReceiverBank(String receiverBank) { this.receiverBank = receiverBank; }

    public String getChnmemo() { return chnmemo; }

    public void setChnmemo(String chnmemo) { this.chnmemo = chnmemo; }

    public String getEngmemo() { return engmemo; }

    public void setEngmemo(String engmemo) { this.engmemo = engmemo; }


}
