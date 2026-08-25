package com.syscom.fep.vo.constant;

public interface RMBackReason {
    //正常
    public static final String Normal = "00";
    //通匯序號錯誤
    public static final String RMSNOError = "01";
    //押碼不符
    public static final String MACError = "02";
    //收款人帳號錯誤
    public static final String ReceiverNoError = "03";
    //收款人姓名錯誤
    public static final String ReceiverNameError = "04";
    //其他錯誤理由
    public static final String ElseError = "99";
}
