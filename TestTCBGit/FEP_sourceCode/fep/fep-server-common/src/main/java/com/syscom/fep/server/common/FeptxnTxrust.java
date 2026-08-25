package com.syscom.fep.server.common;

/**
 FEPTXN_TXRUST
 */
public final class FeptxnTxrust
{
    /**
     初始值
     */
    public static final String Initial = "0";
    /**
     接收／正常
     */
    public static final String Successed = "A";
    /**
     接收／不正常
     */
    public static final String Pending = "B";
    /**
     接收／沖正
     */
    public static final String Reverse = "C";
    /**
     拒絕／正常
     */
    public static final String RejectNormal = "R";
    /**
     拒絕／不正常
     */
    public static final String RejectAbnormal = "S";
    /**
     原交易已拒絕
     */
    public static final String OriTxnRejected = "I";
    /**
     無帳務沖正
     */
    public static final String NoAcctReverse = "N";
    /**
     已沖正成功
     */
    public static final String ReverseSuccessed = "D";
    /**
     沖銷或授權完成進行中
     */
    public static final String Processing = "T";
    /**
     LateResponse上主機沖正
     */
    public static final String LateResponseEC = "H";
}

