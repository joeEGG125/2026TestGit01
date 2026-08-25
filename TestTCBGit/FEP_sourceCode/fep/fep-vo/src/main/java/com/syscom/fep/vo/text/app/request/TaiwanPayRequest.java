package com.syscom.fep.vo.text.app.request;

import com.syscom.fep.vo.text.app.APPGeneral;

public class TaiwanPayRequest {
    /**
     交易金額

     <remark></remark>
     */
    private String Amt;
    public String getAmt() {
        return Amt;
    }
    public void setAmt(String value) {
        Amt = value;
    }

    /**
     端末設備代號

     <remark></remark>
     */
    private String terminalID;
    public String getTerminalID() {
        return terminalID;
    }
    public void setTerminalID(String value) {
        terminalID = value;
    }

    /**
     交易日期時間

     <remark></remark>
     */
    private String localDateTime;
    public String getLocalDateTime() {
        return localDateTime;
    }
    public void setLocalDateTime(String value) {
        localDateTime = value;
    }

    /**
     確認交易日期時間

     <remark></remark>
     */
    private String confirmDateTime;
    public final String getConfirmDateTime() {
        return confirmDateTime;
    }
    public final void setConfirmDateTime(String value) {
        confirmDateTime = value;
    }

    /**
     訂單號碼

     <remark></remark>
     */
    private String orderNumber;
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String value) {
        orderNumber = value;
    }

    /**
     特約商店代號

     <remark></remark>
     */
    private String merchantID;
    public String getMerchantID() {
        return merchantID;
    }
    public void setMerchantID(String value) {
        merchantID = value;
    }

    /**
     帳號

     <remark></remark>
     */
    private String cardNumber;
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String value) {
        cardNumber = value;
    }

    /**
     端末設備型態

     <remark></remark>
     */
    private String sourceType;
    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String value) {
        sourceType = value;
    }

    /**
     TXN destination institute ID

     <remark></remark>
     */
    private String TXNDID;
    public String getTXNDID() {
        return TXNDID;
    }
    public void setTXNDID(String value) {
        TXNDID = value;
    }

    /**
     TXN source institute ID

     <remark></remark>
     */
    private String TXNSID;
    public String getTXNSID() {
        return TXNSID;
    }
    public void setTXNSID(String value) {
        TXNSID = value;
    }

    /**
     system trace audit #

     <remark></remark>
     */
    private String STAN;
    public String getSTAN() {
        return STAN;
    }
    public void setSTAN(String value) {
        STAN = value;
    }

    /**
     IC交易序號

     <remark></remark>
     */
    private String SNUM;
    public String getSNUM() {
        return SNUM;
    }
    public void setSNUM(String value) {
        SNUM = value;
    }

    /**
     交易類型

     <remark></remark>
     */
    private String mti;
    public String getMti() {
        return mti;
    }
    public void setMti(String value) {
        mti = value;
    }

    /**
     處理代碼

     <remark></remark>
     */
    private String processingCode;
    public String getProcessingCode() {
        return processingCode;
    }
    public void setProcessingCode(String value) {
        processingCode = value;
    }

    /**
     回應碼

     <remark></remark>
     */
    private String responseCode;
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String value) {
        responseCode = value;
    }

    /**
     (原)STAN

     <remark></remark>
     */
    private String OriSTAN;
    public String getOriSTAN() {
        return OriSTAN;
    }
    public void setOriSTAN(String value) {
        OriSTAN = value;
    }

    /**
     從 General 對應回來

     <remark></remark>
     */
    public String makeMessageFromGeneral(APPGeneral general) {
        String msg = "{";
        msg += "\"amt\":\"" + general.getmRequest().getAmt() + "\",";
        msg += "\"terminalID\":\"" + general.getmRequest().getTerminalID() + "\",";
        msg += "\"localDateTime\":\"" + general.getmRequest().getLocalDateTime() + "\",";
        msg += "\"confirmDateTime\":\"" + general.getmRequest().getConfirmDateTime() + "\",";
        msg += "\"orderNumber\":\"" + general.getmRequest().getOrderNumber() + "\",";
        msg += "\"merchantID\":\"" + general.getmRequest().getMerchantID() + "\",";
        msg += "\"cardNumber\":\"" + general.getmRequest().getCardNumber() + "\",";
        msg += "\"sourceType\":\"" + general.getmRequest().getSourceType() + "\",";
        msg += "\"TXNDID\":\"" + general.getmRequest().getTXNDID() + "\",";
        msg += "\"TXNSID\":\"" + general.getmRequest().getTXNSID() + "\",";
        msg += "\"STAN\":\"" + general.getmRequest().getSTAN() + "\",";
        msg += "\"SNUM\":\"" + general.getmRequest().getSNUM() + "\",";
        msg += "\"mti\":\"" + general.getmRequest().getMti() + "\",";
        msg += "\"processingCode\":\"" + general.getmRequest().getProcessingCode() + "\",";
        msg += "\"responseCode\":\"" + general.getmRequest().getResponseCode() + "\",";
        msg += "\"OriSTAN\":\"" + general.getmRequest().getOriSTAN() + "\",";
        msg = msg.substring(0, msg.length() - 1);
        msg += "}";
        return msg;
    }


}
