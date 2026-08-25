package com.syscom.fep.notify.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class NotifyResponse {
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("EJNo")
    private String eJNo;

    @JsonProperty("TxDate")
    private String tXDate;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Message")
    private Map<String,Object> message;

}
