package com.syscom.fep.mybatis.ext.model;

/**
 * 交易資料模型類別
 * @author Zonghao
 */
public class GraylistResponseExt {
    // 基本查詢資訊
    private String qryrc;           // 查詢結果 A:QrySuccess, F:QryFail, V:SecretKeyError
    private String bankNo;          // 銀行代號
    private String accountNo;       // 帳號
    private String txDateTime;         // 交易日期(YYYYMMDDhhmmss)
    private String txType;          // 類別
    private String atmNo;           // ATM代號
    private String atmAddressC;     // ATM地址
    private String mp1;          // 電話1
    private String mp2;          // 電話2

    // 預設建構子
    public GraylistResponseExt() {
    }

    // Getter 和 Setter 方法
    public String getQryrc() {
        return qryrc;
    }

    public void setQryrc(String qryrc) {
        this.qryrc = qryrc;
    }

    public String getAccountNo() { return accountNo; }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getTxDateTime() {
        return txDateTime;
    }

    public void setTxDateTime(String txDateTime) {
        this.txDateTime = txDateTime;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getTxType() {
        return txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }
    public String getAtmAddressC() { return atmAddressC; }

    public void setAtmAddressC(String atmAddressC) { this.atmAddressC = atmAddressC; }

    public String getMp1() {
        return mp1;
    }

    public void setMp1(String mp1) {
        this.mp1 = mp1;
    }

    public String getMp2() {
        return mp2;
    }

    public void setMp2(String mp2) {
        this.mp2 = mp2;
    }

    @Override
    public String toString() {
        return "GraylistModel{" +
                "QRYRC='" + qryrc + '\'' +
                ", txDateTime='" + txDateTime + '\'' +
                ", last 5 accountNo=" + accountNo.substring(accountNo.length() - 5) + '\'' +
                ", txType='" + txType + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", atmNo='" + atmNo + '\'' +
                ", atmAddressC='" + atmAddressC + '\'' +
                '}';
    }
}