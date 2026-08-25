package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

public class UI_028260_Form extends BaseForm {
    private String dtTxDate;
    private String tbFepNo;
    private String tbTxAmt;
    private String dtOrgDate;
    private String tbBrno;
    private String ddlOriginal;
    private String tbOrgFepNo;
    private String tbSUPNO;
    private String tbSscode; // 2024-11-18 Richard modified for 【Heap Inspection】

    public String getTbSUPNO() { return tbSUPNO; }

    public void setTbSUPNO(String tbSUPNO) { this.tbSUPNO = tbSUPNO; }

    public String getTbSscode() { return tbSscode; }

    public void setTbSscode(String tbSscode) { this.tbSscode = tbSscode; }

    public String getDtTxDate() { return dtTxDate; }

    public void setDtTxDate(String dtTxDate) { this.dtTxDate = dtTxDate; }

    public String getTbFepNo() { return tbFepNo; }

    public void setTbFepNo(String tbFepNo) { this.tbFepNo = tbFepNo; }

    public String getTbTxAmt() { return tbTxAmt; }

    public void setTbTxAmt(String tbTxAmt) { this.tbTxAmt = tbTxAmt; }

    public String getDtOrgDate() { return dtOrgDate; }

    public void setDtOrgDate(String dtOrgDate) { this.dtOrgDate = dtOrgDate; }

    public String getTbBrno() { return tbBrno; }

    public void setTbBrno(String tbBrno) { this.tbBrno = tbBrno; }

    public String getDdlOriginal() { return ddlOriginal; }

    public void setDdlOriginal(String ddlOriginal) { this.ddlOriginal = ddlOriginal; }

    public String getTbOrgFepNo() { return tbOrgFepNo; }

    public void setTbOrgFepNo(String tbOrgFepNo) { this.tbOrgFepNo = tbOrgFepNo; }
}
