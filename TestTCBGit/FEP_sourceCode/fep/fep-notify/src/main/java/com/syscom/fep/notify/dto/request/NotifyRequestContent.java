package com.syscom.fep.notify.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotifyRequestContent {
    @JsonProperty("TemplateId")
    private String templateId;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("ParmVars")
    private Map<String, String> parmVars;

    @JsonProperty("ContentIndex")
    private String contentIndex;

    @JsonIgnore
    private String reserved; // 2024/08/29 Richard add just add for 【Mass Assignment: Insecure Binder Configuration】

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Message")
    private String message;

}
