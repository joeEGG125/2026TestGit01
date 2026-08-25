package com.syscom.fep.notify.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syscom.fep.notify.model.NotifyContentResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogNotifyResponse{
    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("EJNo")
    private String eJNo;

    @JsonProperty("TxDaTe")
    private String tXDate;

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("NotifyContents")
    private List<NotifyContentResponse> notifyContentResponses;

    @JsonProperty("Status")
    private String status;


}
