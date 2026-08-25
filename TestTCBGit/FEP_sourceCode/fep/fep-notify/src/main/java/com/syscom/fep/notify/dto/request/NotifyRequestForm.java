package com.syscom.fep.notify.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class NotifyRequestForm implements Serializable {
    @JsonProperty("RuleSetId")
    private String ruleSetId;
    @JsonProperty("EJNo")
    private String eJNo;
    @JsonProperty("TxDate")
    private String tXDate;
    @JsonProperty("ClientId")
    private String clientId;
    @JsonIgnore
    private String reserved; // 2024/08/20 Richard add just add for 【Mass Assignment: Insecure Binder Configuration】

    @JsonProperty("Contents")
    private List<NotifyRequestContent> contents;
}
