package com.syscom.fep.notify.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class MiddlePlatformRequestForm implements Serializable {
    @JsonProperty("header")
    private Header header;

    @JsonProperty("serviceRq")
    private ServiceRequest serviceRequest;

    @Getter
    @Setter
    public static class Header {
        private String clientSystemId;
        private String clientTimestamp;
        private String clientSeqNo;
        private String globalId = "";
        private String tellerId = "";
        private String supervisorAId = "";
        private String supervisorBId = "";
        private String validFlag;
    }

    @Getter
    @Setter
    public static class ServiceRequest {
        @JsonProperty("serviceInfo")
        private ServiceInfo serviceInfo;

        @JsonProperty("content")
        private Content content;
    }

    @Getter
    @Setter
    public static class ServiceInfo {
        private String txnCode = "";
        private String txTimestamp = "";
        private String transactionRef = "";
        private String fiscStan = "";
        private int globalCounter;
        private String globalPreSys = "";
        private String branchCode = "";
    }

    @Getter
    @Setter
    public static class Content {
        private String ntfSendObj = "";
        private String ntfAcctNo = "";
        private String ntfButype = "";
        private String defMail = "";
        private String defPhone = "";
        private String defSms = "";
        private String defPush = "";
        private String defEmail = "";
        private Map<String, String> messageFields;
    }
}
