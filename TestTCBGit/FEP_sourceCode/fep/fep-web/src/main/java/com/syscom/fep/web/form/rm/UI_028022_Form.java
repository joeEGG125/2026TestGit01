package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

/**
 * @author xingyun_yang
 * @create 2021/9/23
 */
public class UI_028022_Form extends BaseForm {

    /**
     * 原電文序號
     */
    private String orgFiscno;
    /**
     * 原交易類別 1111:跨行電匯 1121:公庫匯款 1131:同業匯款 1171:退還匯款 1181:證券匯款 1191:票券匯款 1411:一般通訊
     */
    private String orgPcode;
    /**
     * 原交易金額
     */
    private String oOrgTxamt;
    /**
     * 原電文序號
     */
    private String oOrgFiscno;
    /**
     * 原通匯序號
     */
    private String oOrgRmsno;
    /**
     * 原解款行代號
     */
    private String oOrgReceiverBank;
    /**
     * 狀況代號及說明
     */
    private String oMsg;
    /**
     * 原交易類別
     */
    private String oOrgPcode;
    /**
     * 原交易序號
     */
    private String oOrgStan;
    /**
     * 解款行接受交易日期時間
     */
    private String oOrgTxDatetime;

    public String getOrgFiscno() { return orgFiscno; }

    public void setOrgFiscno(String orgFiscno) { this.orgFiscno = orgFiscno; }

    public String getOrgPcode() { return orgPcode; }

    public void setOrgPcode(String orgPcode) { this.orgPcode = orgPcode; }

    public String getoOrgTxamt() { return oOrgTxamt; }

    public void setoOrgTxamt(String oOrgTxamt) { this.oOrgTxamt = oOrgTxamt; }

    public String getoOrgFiscno() { return oOrgFiscno; }

    public void setoOrgFiscno(String oOrgFiscno) { this.oOrgFiscno = oOrgFiscno; }

    public String getoOrgRmsno() { return oOrgRmsno; }

    public void setoOrgRmsno(String oOrgRmsno) { this.oOrgRmsno = oOrgRmsno; }

    public String getoOrgReceiverBank() { return oOrgReceiverBank; }

    public void setoOrgReceiverBank(String oOrgReceiverBank) { this.oOrgReceiverBank = oOrgReceiverBank; }

    public String getoMsg() { return oMsg; }

    public void setoMsg(String oMsg) { this.oMsg = oMsg; }

    public String getoOrgPcode() { return oOrgPcode; }

    public void setoOrgPcode(String oOrgPcode) { this.oOrgPcode = oOrgPcode; }

    public String getoOrgStan() { return oOrgStan; }

    public void setoOrgStan(String oOrgStan) { this.oOrgStan = oOrgStan; }

    public String getoOrgTxDatetime() { return oOrgTxDatetime; }

    public void setoOrgTxDatetime(String oOrgTxDatetime) { this.oOrgTxDatetime = oOrgTxDatetime; }
}
