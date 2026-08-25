package com.syscom.fep.notify.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class NotifyMiddlePlatformRequestForm implements Serializable {
    @JsonProperty("TemplateId")
    private String templateId;
    @JsonProperty("EJNo")
    private String eJNo;
    @JsonProperty("TxDate")
    private String tXDate;
    @JsonProperty("NtfAcctNo")
    private String ntfAcctNo;
    @JsonProperty("DefMail")
    private String defMail;
    @JsonProperty("DefPhone")
    private String defphne_number;
    @JsonProperty("TellerId")
    private String tellerId;
    @JsonProperty("DefSms")
    private String defSms;
    @JsonProperty("DefPush")
    private String defPush;
    @JsonProperty("DefEmail")
    private String defEmail;
    @JsonProperty("Param1")
    private String param1;
    @JsonProperty("Param2")
    private String param2;
    @JsonProperty("Param3")
    private String param3;
    @JsonProperty("Param4")
    private String param4;
    @JsonProperty("Param5")
    private String param5;
    @JsonProperty("Param6")
    private String param6;
    @JsonProperty("Param7")
    private String param7;
}
