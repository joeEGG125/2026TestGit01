package com.syscom.fep.vo.constant;

public interface RMCategory {

    //一般通訊匯出類
    public static final String MSGOut = "20";
    //一般通訊匯入類
    public static final String MSGIn = "21";
    //臨櫃電匯匯入類
    public static final String RMInOverCounterTele = "10";
    //退匯集體項
    public static final String BackExchange = "11";
    //當日匯款匯出類
    public static final String RMOutTBSDY = "01";
    //次日匯款匯出類
    public static final String RMOutNBSDY = "02";
    //媒體當日匯款匯出類
    public static final String MediaRMOutTBSDY = "03";
    //媒體預約匯款匯出類
    public static final String MediaReservedRMOut = "04";
    //人工退匯匯出類
    public static final String RMOutBackExchangeByHand = "05";
    //自動退匯匯出類
    public static final String RMOutBackExchangeAuto = "06";
    //匯入類(滯留磁片交易)
    public static final String RMInPending = "12";


}
