package com.syscom.fep.web.form.inbk;

import com.syscom.fep.mybatis.model.Wkpostdtl;
import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/16
 */
public class UI_019401_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String lblZone;
    private String lblStDate;
    private String sysCode;
    private String acBranchCode;
    private String drCrSide;
    private String acCode;
    private String subAcCode;
    private String dtlAcCode;
    private String deptCode;
    private String txAmt;
    private String brapTxType;
    private String brapCur;

    public String getLblZone() { return lblZone; }

    public void setLblZone(String lblZone) { this.lblZone = lblZone; }

    public String getLblStDate() { return lblStDate; }

    public void setLblStDate(String lblStDate) { this.lblStDate = lblStDate; }

    public String getSysCode() { return sysCode; }

    public void setSysCode(String sysCode) { this.sysCode = sysCode; }

    public String getAcBranchCode() { return acBranchCode; }

    public void setAcBranchCode(String acBranchCode) { this.acBranchCode = acBranchCode; }

    public String getDrCrSide() { return drCrSide; }

    public void setDrCrSide(String drCrSide) { this.drCrSide = drCrSide; }

    public String getAcCode() { return acCode; }

    public void setAcCode(String acCode) { this.acCode = acCode; }

    public String getSubAcCode() { return subAcCode; }

    public void setSubAcCode(String subAcCode) { this.subAcCode = subAcCode; }

    public String getDtlAcCode() { return dtlAcCode; }

    public void setDtlAcCode(String dtlAcCode) { this.dtlAcCode = dtlAcCode; }

    public String getDeptCode() { return deptCode; }

    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }

    public String getTxAmt() { return txAmt; }

    public void setTxAmt(String txAmt) { this.txAmt = txAmt; }

    public String getBrapTxType() { return brapTxType; }

    public void setBrapTxType(String brapTxType) { this.brapTxType = brapTxType; }

    public String getBrapCur() { return brapCur; }

    public void setBrapCur(String brapCur) { this.brapCur = brapCur; }

}
