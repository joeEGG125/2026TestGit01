package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author LeYun
 * @create 2026/6/17
 */
public class UI_019530_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    /**
     * 營業日
     */
    private String feptxnTbsdyFisc;

    /**
     * 交易 收款 付款
     */
    private String transSelect;

    /**
     * 使用者權限, true表示為FEP_EBMK或FEP_EBCK
     */
    private boolean hasFepEbmk;
    private boolean hasFepEbck;

    /**
     * 是否在 16:50 之前
     */
    private boolean before1650;

    public String getFeptxnTbsdyFisc() { return feptxnTbsdyFisc; }

    public void setFeptxnTbsdyFisc(String feptxnTbsdyFisc) { this.feptxnTbsdyFisc = feptxnTbsdyFisc; }

    public String getTransSelect() { return transSelect; }

    public void setTransSelect(String transSelect) { this.transSelect = transSelect; }

    public boolean getHasFepEbmk() {
        return hasFepEbmk;
    }

    public void setHasFepEbmk(boolean hasFepEbmk) {
        this.hasFepEbmk = hasFepEbmk;
    }

    public boolean getHasFepEbck() {
        return hasFepEbck;
    }

    public void setHasFepEbck(boolean hasFepEbck) {
        this.hasFepEbck = hasFepEbck;
    }

    public boolean isBefore1650() {
        return before1650;
    }

    public void setBefore1650(boolean before1650) {
        this.before1650 = before1650;
    }
}
