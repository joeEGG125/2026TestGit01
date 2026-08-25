package com.syscom.fep.mybatis.ext.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashTOTARequestExt {
    private String type;
    private String agentBank;
    private String seqNo;
    private String txnTime;
    private String idn;
    private String healthId;
    private String channelType;
    private String orgSeqNo;
    private String orgTxnNo;
    private String orgRespTime;

    @Override
    public String toString() {
        return "{" +
                    "type='" + type + '\'' +
                    ", agentBank='" + agentBank + '\'' +
                    ", seqNo='" + seqNo + '\'' +
                    ", txnTime='" + txnTime + '\'' +
                    ", idn='" + idn + '\'' +
                    ", healthId='" + healthId + '\'' +
                    ", channelType='" + channelType + '\'' +
                    ", orgSeqNo='" + orgSeqNo + '\'' +
                    ", orgTxnNo='" + orgTxnNo + '\'' +
                    ", orgRespTime='" + orgRespTime + '\'' +
                '}';
    }
}
