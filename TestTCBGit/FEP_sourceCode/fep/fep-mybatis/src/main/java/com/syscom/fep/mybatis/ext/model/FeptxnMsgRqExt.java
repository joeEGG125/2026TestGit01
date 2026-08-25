package com.syscom.fep.mybatis.ext.model;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeptxnMsgRqExt {
    @NotNull(message = "BankNo cannot be null")
    @JsonProperty("I_BankNo")
    private String I_BankNo; // 銀行代號(發動行)

    @NotNull(message = "StanNo cannot be null")
    @JsonProperty("I_StanNo")
    private String I_StanNo; // 交易序號

    @NotNull(message = "SystemID cannot be null")
    @JsonProperty("I_SystemID")
    private String I_SystemID;

    @NotNull(message = "SecretKey cannot be null")
    @JsonProperty("I_SecretKey")
    private String I_SecretKey;

    @NotNull(message = "TxDate cannot be null")
    @JsonProperty("I_TxDate")
    private String I_TxDate; // 交易日期


    public String getI_BankNo() {
        return I_BankNo;
    }

    public void setI_BankNo(String i_BankNo) {
        I_BankNo = i_BankNo;
    }

    public String getI_StanNo() {
        return I_StanNo;
    }

    public void setI_StanNo(String i_StanNo) {
        I_StanNo = i_StanNo;
    }

    public String getI_SystemID() {
        return I_SystemID;
    }

    public void setI_SystemID(String i_SystemID) {
        I_SystemID = i_SystemID;
    }

    public String getI_SecretKey() {
        return I_SecretKey;
    }

    public void setI_SecretKey(String i_SecretKey) {
        I_SecretKey = i_SecretKey;
    }

    public String getI_TxDate() {
        return I_TxDate;
    }

    public void setI_TxDate(String i_TxDate) {
        I_TxDate = i_TxDate;
    }
}
