package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/3
 */
public class UI_019040_Form extends BaseForm{

    private String fwdrstTxDate;
    private UI_019040_Form.RadioOption radioOption = UI_019040_Form.RadioOption.ALL;
    private String fwdtxnTxId;
    private String channel;
    private String fwdtxnTroutActno;
    private String fwdtxnTrinBkno;
    private String fwdtxnTrinActno;
    private String fwdtxnTxAmt;

    /**
     * 系統錯誤選項 0:false 1:true
     */
    private Short sysFail;

    public RadioOption getRadioOption() {
        return radioOption;
    }

    public void setRadioOption(RadioOption radioOption) {
        this.radioOption = radioOption;
    }

    public UI_019040_Form() {
        super();
    }

    public String getFwdrstTxDate() {
        return fwdrstTxDate;
    }

    public void setFwdrstTxDate(String fwdrstTxDate) {
        this.fwdrstTxDate = fwdrstTxDate;
    }

    public String getFwdtxnTxId() {
        return fwdtxnTxId;
    }

    public void setFwdtxnTxId(String fwdtxnTxId) {
        this.fwdtxnTxId = fwdtxnTxId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFwdtxnTroutActno() {
        return fwdtxnTroutActno;
    }

    public void setFwdtxnTroutActno(String fwdtxnTroutActno) {
        this.fwdtxnTroutActno = fwdtxnTroutActno;
    }

    public String getFwdtxnTrinBkno() {
        return fwdtxnTrinBkno;
    }

    public void setFwdtxnTrinBkno(String fwdtxnTrinBkno) {
        this.fwdtxnTrinBkno = fwdtxnTrinBkno;
    }

    public String getFwdtxnTrinActno() {
        return fwdtxnTrinActno;
    }

    public void setFwdtxnTrinActno(String fwdtxnTrinActno) {
        this.fwdtxnTrinActno = fwdtxnTrinActno;
    }

    public String getFwdtxnTxAmt() {
        return fwdtxnTxAmt;
    }

    public void setFwdtxnTxAmt(String fwdtxnTxAmt) {
        this.fwdtxnTxAmt = fwdtxnTxAmt;
    }

    public Short getSysFail() {
        return sysFail;
    }

    public void setSysFail(Short sysFail) {
        this.sysFail = sysFail;
    }

    public static enum RadioOption {
        /**
         * ALL 全部
         */
        ALL,
        /**
         * FAIL 失敗
         */
        FAIL,
        /**
         * RSTEPS 重送
         */
        RSTEPS,
        /**
         * ORDER 預約
         */
        ORDER
    }
}
