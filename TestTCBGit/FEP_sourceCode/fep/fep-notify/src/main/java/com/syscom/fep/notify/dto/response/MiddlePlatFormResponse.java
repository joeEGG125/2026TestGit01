package com.syscom.fep.notify.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiddlePlatFormResponse {
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("EjNo")
    private String ejNo;
    @JsonProperty("TxDate")
    private String txDate;
}