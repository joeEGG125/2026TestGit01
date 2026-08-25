package com.syscom.fep.mybatis.ext.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class UcdidMsgRqExt {
    @NotNull(message = "IDNo cannot be null")
    @JsonProperty("I_IDNo")
    private String I_IDNo;

    @NotNull(message = "HealthID cannot be null")
    @JsonProperty("I_HealthID")
    private String I_HealthID;

    @NotNull(message = "SystemID cannot be null")
    @JsonProperty("I_SystemID")
    private String I_SystemID;

    @NotNull(message = "SecretKey cannot be null")
    @JsonProperty("I_SecretKey")
    private String I_SecretKey;

    public String getI_IDNo() {
        return I_IDNo;
    }

    public void setI_IDNo(String i_BankNo) {
        I_IDNo = i_BankNo;
    }

    public String getI_HealthID() {
        return I_HealthID;
    }

    public void setI_HealthID(String i_HealthID) {
        I_HealthID = i_HealthID;
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
}
