package com.syscom.fep.notify.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotifyRequestParmVars {
    @JsonProperty("TemplateId")
    private Long templateId;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("ParmVars")
    private Map<String, String> parmVars;
}
