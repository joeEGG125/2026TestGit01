package com.syscom.fep.mybatis.ext.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class GraylistRequestExt {
    @NotNull(message = "BankNo cannot be null")
    @JsonProperty("I_ALLBANK_BKNO")
    private String I_ALLBANK_BKNO; // 銀行代號(發動行)
    
    @NotNull(message = "ActNo cannot be null")
    @JsonProperty("I_TROUT_ACTNO")
    private String I_TROUT_ACTNO; // 轉出帳號

    @NotNull(message = "AtmNo cannot be null")
    @JsonProperty("I_ATM_ATMNO")
    private String I_ATM_ATMNO; // ATM代號

    @NotNull(message = "TxDateTime cannot be null")
    @JsonProperty("I_TxDateTime")
    private String I_TxDateTime; // 交易日期

    @NotNull(message = "SystemID cannot be null")
    @JsonProperty("I_SystemID")
    private String I_SystemID; // 系統代號
    
    @NotNull(message = "SecretKey cannot be null")
    @JsonProperty("I_SecK")
    private String I_SecK; // 金鑰

    @NotNull(message = "TxType cannot be null")
    @JsonProperty("I_TxType")
    private String I_TxType; // 類別

    @JsonProperty("I_LogEj")
    private int I_LogEj; // 類別

    @Override
    public String toString() {
        return "GraylistRequestExt{" +
                "BKNO='" + I_ALLBANK_BKNO + '\'' +
                ", ACTNO='" + I_TROUT_ACTNO + '\'' +
                ", ATMNO='" + I_ATM_ATMNO + '\'' +
                ", TxDateTime='" + I_TxDateTime + '\'' +
                ", SystemID='" + I_SystemID + '\'' +
                ", SecK='" + "*********" + I_SecK.substring(I_SecK.length() - 5) + '\'' +
                ", TxType='" + I_TxType + '\'' +
                ", LogEj='" + I_LogEj + '\'' +
                '}';
    }
}
