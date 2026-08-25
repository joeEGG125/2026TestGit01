package com.syscom.fep.vo.text.app;

public class APPGeneralRequest {
    public APPGeneralRequest()
    {
    }

    /**
     交易金額

     <remark></remark>
     */
    private String amt;
    public String getAmt() {
        return amt;
    }
    public void setAmt(String value) {
        amt = value;
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
    public String getConfirmDateTime() {
        return confirmDateTime;
    }
    public void setConfirmDateTime(String value) {
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

}
