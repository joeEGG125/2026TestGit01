package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author LeYun
 * @create 2026/6/17
 */
public class UI_019520_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    /**
     * 營業日
     */
    private String feptxnTbsdyFisc;

    /**
     * 查詢交易 全部 未完成
     */
    private String brapSelect;

    public String getFeptxnTbsdyFisc() { return feptxnTbsdyFisc; }

    public void setFeptxnTbsdyFisc(String feptxnTbsdyFisc) { this.feptxnTbsdyFisc = feptxnTbsdyFisc; }

    public String getBrapSelect() { return brapSelect; }

    public void setBrapSelect(String brapSelect) { this.brapSelect = brapSelect; }



}
