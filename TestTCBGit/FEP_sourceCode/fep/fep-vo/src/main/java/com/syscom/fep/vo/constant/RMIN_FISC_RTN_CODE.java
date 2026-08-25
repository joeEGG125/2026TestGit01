package com.syscom.fep.vo.constant;

public interface RMIN_FISC_RTN_CODE {
    /**
     正常
     */
    public static final String Normal = "0001";
    /**
     押碼錯誤
     */
    public static final String MACError = "0301";
    /**
     同步錯誤
     */
    public static final String SyncError = "0302";
    /**
     其他錯誤
     */
    public static final String ElseError = "2999";
    /**
     序號錯誤
     */
    public static final String SeqnoError = "5001";
    /**
     超過限額
     */
    public static final String OverLimit = "5002";
    /**
     中文欄位或變動長度欄位長度錯誤
     */
    public static final String FieldLengthError = "5004";
    /**
     發(收)信單位與發(收)信行代號不符
     */
    public static final String BankNotConsistent = "5006";
    /**
     原交易訊息類別錯誤
     */
    public static final String OriginMsgError = "5007";
    /**
     解款單位無此分行
     */
    public static final String CashRemittanceNotJoinBus = "5013";
    /**
     該筆匯款已經由人工匯款處理
     */
    public static final String HandledByHand = "5017";
    /**
     收信行未參加該跨行業務
     */
    public static final String ReceiverNotJoinBus = "5018";
    /**
     發信行未參加該跨行業務
     */
    public static final String SenderNotJoinBus = "5019";

}
