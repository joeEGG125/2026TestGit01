package com.syscom.fep.vo.constant;

public interface RMINStatus {
    //匯入自動入戶
    public static final String AutoEnterAccount = "01";
    //待解款(原10匯入中之退匯匯入併至02待解款)
    public static final String WaitRemit = "02";
    //已解款
    public static final String Remited = "03";
    //自動退匯(基碼/通匯序號不符)
    public static final String AutoBackRemit = "04";
    //匯入款退匯待放行
    public static final String BackRemitWaitPass = "05";
    //匯入款退匯待放行
    public static final String BackRemitPassed = "06";
    //滯留自動入戶
    public static final String RMInStoreByHand = "07";
    //疑似 HIT AML
    public static final String HitAML = "08";
    //FEP檢核有誤
    public static final String FEPCheckError = "09";
    //傳送中(AML)
    public static final String TransferringAML = "98";
    //傳送中(CBS)
    public static final String Transferring = "99";
}
