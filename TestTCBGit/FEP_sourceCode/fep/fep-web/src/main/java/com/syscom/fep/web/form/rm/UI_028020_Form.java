package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author Chen_yu
 * @create 2021/9/27
 */
public class UI_028020_Form extends BaseForm {
    // 查詢方式
    private String inqFlag;
    // 查詢序號
    private String sno;
    // 查詢方式
    private String orgKind;
    // 回應代號
    private String rc;
    // 狀況代號
    private String status;
    // 解決方式
    private String resolve;

    public String getInqFlag() {
        return inqFlag;
    }

    public void setInqFlag(String inqFlag) {
        this.inqFlag = inqFlag;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getOrgKind() {
        return orgKind;
    }

    public void setOrgKind(String orgKind) {
        this.orgKind = orgKind;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) {
        this.resolve = resolve;
    }
}
