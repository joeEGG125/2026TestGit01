package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;


public class UI_028100_Form extends BaseForm {


    private String txDate;

    private String stan;

    private String fepNo;

    private String ejfno;

    private String fiscSno;

    private String senderBank;

    private String rmSno;

    private String oTxDate;

    private String oFepNo;

    private String oEjno;

    private String actno;

    private String amt;

    private String oRmSno;

    private String oFiscSno;

    private String oSenderBank;

    private String receiverBank;

    private String inName;

    private String outName;

    private String memo;

    /**
     * 執行結果
     */
    private boolean result = true;
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message = "";


    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getFepNo() {
        return fepNo;
    }

    public void setFepNo(String fepNo) {
        this.fepNo = fepNo;
    }

    public String getEjfno() {
        return ejfno;
    }

    public void setEjfno(String ejfno) {
        this.ejfno = ejfno;
    }

    public String getFiscSno() {
        return fiscSno;
    }

    public void setFiscSno(String fiscSno) {
        this.fiscSno = fiscSno;
    }

    public String getSenderBank() {
        return senderBank;
    }

    public void setSenderBank(String senderBank) {
        this.senderBank = senderBank;
    }

    public String getRmSno() {
        return rmSno;
    }

    public void setRmSno(String rmSno) {
        this.rmSno = rmSno;
    }

    public String getoTxDate() {
        return oTxDate;
    }

    public void setoTxDate(String oTxDate) {
        this.oTxDate = oTxDate;
    }

    public String getoFepNo() {
        return oFepNo;
    }

    public void setoFepNo(String oFepNo) {
        this.oFepNo = oFepNo;
    }

    public String getoEjno() {
        return oEjno;
    }

    public void setoEjno(String oEjno) {
        this.oEjno = oEjno;
    }

    public String getActno() {
        return actno;
    }

    public void setActno(String actno) {
        this.actno = actno;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getoRmSno() {
        return oRmSno;
    }

    public void setoRmSno(String oRmSno) {
        this.oRmSno = oRmSno;
    }

    public String getoFiscSno() {
        return oFiscSno;
    }

    public void setoFiscSno(String oFiscSno) {
        this.oFiscSno = oFiscSno;
    }

    public String getoSenderBank() {
        return oSenderBank;
    }

    public void setoSenderBank(String oSenderBank) {
        this.oSenderBank = oSenderBank;
    }

    public String getReceiverBank() {
        return receiverBank;
    }

    public void setReceiverBank(String receiverBank) {
        this.receiverBank = receiverBank;
    }

    public String getInName() {
        return inName;
    }

    public void setInName(String inName) {
        this.inName = inName;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

   
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        if (messageType == MessageType.DANGER) {
            this.result = false;
        }
    }
    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
