package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/16
 */
public class UI_019400_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    /**
     * 地區 香港 澳門 台灣
     */
    private String ddlZoneBrap;

    /**
     * 清算日期
     */
    private String lblStDate;

    /**
     * PCODE
     */
    private String pcode;

    /**
     * APID
     */
    private String apId;

    /**
     * 分行代號
     */
    private String brapBrno;

    /**
     * 績效行
     */
    private String brapDeptCode;

    /**
     * 交易類別 A:代理 I:轉入 O:轉出
     */
    private String brapTxType;

    /**
     * 幣別
     */
    private String brapCur;

    public String getDdlZoneBrap() { return ddlZoneBrap; }

    public void setDdlZoneBrap(String ddlZoneBrap) { this.ddlZoneBrap = ddlZoneBrap; }

    public String getLblStDate() { return lblStDate; }

    public void setLblStDate(String lblStDate) { this.lblStDate = lblStDate; }

    public String getPcode() { return pcode; }

    public void setPcode(String pcode) { this.pcode = pcode; }

    public String getApId() { return apId; }

    public void setApId(String apId) { this.apId = apId; }

    public String getBrapBrno() { return brapBrno; }

    public void setBrapBrno(String brapBrno) { this.brapBrno = brapBrno; }

    public String getBrapDeptCode() { return brapDeptCode; }

    public void setBrapDeptCode(String brapDeptCode) { this.brapDeptCode = brapDeptCode; }

    public String getBrapTxType() { return brapTxType; }

    public void setBrapTxType(String brapTxType) { this.brapTxType = brapTxType; }

    public String getBrapCur() { return brapCur; }

    public void setBrapCur(String brapCur) { this.brapCur = brapCur; }

}
