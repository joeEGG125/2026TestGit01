package com.syscom.fep.vo.constant;

public interface RMOUTStatus {
    //未扣帳
    public static final String NotDebit = "00";
    //已登錄
    public static final String Registered = "01";
    //待補登
    public static final String Wait = "02";
    //未扣帳
    public static final String WaitForPass = "03";
    //已登錄
    public static final String Passed = "04";
    //傳送中
    public static final String Transferring = "04";
    //待補登
    public static final String Transfered = "05";
    //未扣帳
    public static final String GrantMoney = "06";
    //已登錄
    public static final String FISCReject = "07";
    //待補登
    public static final String BackExchange = "08";
    //未扣帳
    public static final String DeletedNotUpdateAccount = "09";
    //已登錄
    public static final String DeleteAlreadyUpdateAccount = "10";
    //待補登
    public static final String DiskBatchRMOutFail = "11";
    //待補登
    public static final String SystemProblem = "99";
    //未扣帳
    public static final String DeleteEmergencyTransfer = "12";
    //已登錄
    public static final String Deleted = "13";
    //待補登
    public static final String OtherBankRemit = "14";
}
