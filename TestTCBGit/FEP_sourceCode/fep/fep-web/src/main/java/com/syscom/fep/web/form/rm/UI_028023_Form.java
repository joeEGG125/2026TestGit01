package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/23
 */
public class UI_028023_Form extends BaseForm {

    /**
     * 原交易類別 1112:跨行電匯 1122:公庫匯款 1132:同業匯款 1172:退還匯款 1812:證券匯款 1192:票券匯款 1412:一般通訊 1100:所有匯款類
     */
    private String orgPcodeDdl;
    /**
     * 原交易類別
     */
    private String oOrgPcode;
    /**
     * 待解筆數
     */
    private String rmPendingCnt;
    /**
     * 待解金額
     */
    private String rmPendingAmt;

    public String getOrgPcodeDdl() { return orgPcodeDdl; }

    public void setOrgPcodeDdl(String orgPcodeDdl) { this.orgPcodeDdl = orgPcodeDdl; }

    public String getoOrgPcode() { return oOrgPcode; }

    public void setoOrgPcode(String oOrgPcode) { this.oOrgPcode = oOrgPcode; }

    public String getRmPendingCnt() { return rmPendingCnt; }

    public void setRmPendingCnt(String rmPendingCnt) { this.rmPendingCnt = rmPendingCnt; }

    public String getRmPendingAmt() { return rmPendingAmt; }

    public void setRmPendingAmt(String rmPendingAmt) { this.rmPendingAmt = rmPendingAmt; }
}
