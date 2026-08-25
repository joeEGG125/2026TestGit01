package com.syscom.fep.vo.constant;

public interface RMIN_TMP_STATUS {
    //未處理
    public static final String UnProcess = "A";
    //處理中
    public static final String Processing = "B";
    //待放行
    public static final String WaitForPass = "C";
    //已放行
    public static final String Passed = "D";
    //退匯
    public static final String BackRemit = "E";
    //不予處理
    public static final String NoProcess = "F";
    //已解款
    public static final String GrantMoney = "G";
    //人工入帳失敗
    public static final String InAccountByHandFail = "H";
    //FEP檢核錯誤
    public static final String FEPCheckError = "I";
    //待覆核
    public static final String WaitForReCheck = "J";
    //人工解款沖正成功
    public static final String ReverseMoneyByHandOK = "K";
    //人工解款沖正失敗
    public static final String ReverseMoneyByHandFail = "L";
    //全部
    public static final String All = "Z";
}

