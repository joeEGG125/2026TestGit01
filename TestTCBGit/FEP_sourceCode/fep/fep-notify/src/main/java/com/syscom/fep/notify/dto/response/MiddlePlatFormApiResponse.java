package com.syscom.fep.notify.dto.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MiddlePlatFormApiResponse {
    @JacksonXmlProperty(localName = "header")
    private Header header;

    @JacksonXmlProperty(localName = "status")
    private Status status;

    @JacksonXmlProperty(localName = "serviceRs")
    private ServiceRs serviceRs;

    @Getter
    @Setter
    public static class Header {
        @JacksonXmlProperty(localName = "clientSystemId")
        private String clientSystemId;

        @JacksonXmlProperty(localName = "clientTimestamp")
        private String clientTimestamp;

        @JacksonXmlProperty(localName = "clientSeqNo")
        private String clientSeqNo;

        @JacksonXmlProperty(localName = "guid")
        private String guid;

        @JacksonXmlProperty(localName = "txSeqNo")
        private String txSeqNo;

        @JacksonXmlProperty(localName = "validFlag")
        private String validFlag;

        @JacksonXmlProperty(localName = "globalId")
        private String globalId;

        @JacksonXmlProperty(localName = "tellerId")
        private String tellerId;

        @JacksonXmlProperty(localName = "supervisorAId")
        private String supervisorAId;

        @JacksonXmlProperty(localName = "supervisorBId")
        private String supervisorBId;

        @JacksonXmlProperty(localName = "ecFlag")
        private String ecFlag;

        @JacksonXmlProperty(localName = "ecClientSeqNo")
        private String ecClientSeqNo;

        @JacksonXmlProperty(localName = "ecTxSeqNo")
        private String ecTxSeqNo;
    }

    @Getter
    @Setter
    public static class Status {
        @JacksonXmlProperty(localName = "code")
        private String code;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "severity")
        private String severity;

        @JacksonXmlProperty(localName = "exception")
        private String exception;

        @JacksonXmlProperty(localName = "authorizationLevel")
        private String authorizationLevel;

        @JacksonXmlProperty(localName = "extension")
        private String extension;
    }

    @Getter
    @Setter
    public static class ServiceRs {
        @JacksonXmlProperty(localName = "serviceInfo")
        private ServiceInfo serviceInfo;

        @JacksonXmlProperty(localName = "content")
        private String content;

        @JacksonXmlProperty(localName = "messageInfoList")
        private MessageInfoList messageInfoList;
    }

    @Getter
    @Setter
    public static class ServiceInfo {
        @JacksonXmlProperty(localName = "txnCode")
        private String txnCode;

        @JacksonXmlProperty(localName = "txTimestamp")
        private String txTimestamp;

        @JacksonXmlProperty(localName = "globalCounter")
        private int globalCounter;

        @JacksonXmlProperty(localName = "businessDate")
        private String businessDate;

        @JacksonXmlProperty(localName = "transactionRef")
        private String transactionRef;

        @JacksonXmlProperty(localName = "fiscStan")
        private String fiscStan;

        @JacksonXmlProperty(localName = "globalPreSys")
        private String globalPreSys;

        @JacksonXmlProperty(localName = "branchCode")
        private String branchCode;

        @JacksonXmlProperty(localName = "timer")
        private Timer timer;

        @JacksonXmlProperty(localName = "paging")
        private String paging;
    }

    @Getter
    @Setter
    public static class MessageInfoList {
    }

    @Getter
    @Setter
    public static class Timer {
        @JacksonXmlProperty(localName = "startCurrentTimeMillis")
        private String startCurrentTimeMillis;

        @JacksonXmlProperty(localName = "endCurrentTimeMillis")
        private String endCurrentTimeMillis;

        @JacksonXmlProperty(localName = "elapsedTimeMillis")
        private String elapsedTimeMillis;
    }
}
