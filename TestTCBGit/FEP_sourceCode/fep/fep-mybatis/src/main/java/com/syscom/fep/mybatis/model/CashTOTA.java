package com.syscom.fep.mybatis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashTOTA {
    private String type;
    private String agentBank;
    private String seqNo;
    private String txnTime;
    private String txnNo;
    private String respTime;
    private String returnCode;
    private String orgIdn;
    private String orgHealthId;
    private String orgChannelType;
    private String orgTxnNo;
    private String orgRespTime;
    private String orgReturnCode;

    @Override
    public String toString() {
        return "{" +
                    "type='" + type + '\'' +
                    ", agentBank='" + agentBank + '\'' +
                    ", seqNo='" + seqNo + '\'' +
                    ", txnTime='" + txnTime + '\'' +
                    ", txnNo='" + txnNo + '\'' +
                    ", respTime='" + respTime + '\'' +
                    ", returnCode='" + returnCode + '\'' +
                    ", orgIdn='" + orgIdn + '\'' +
                    ", orgHealthId='" + orgHealthId + '\'' +
                    ", orgChannelType='" + orgChannelType + '\'' +
                    ", orgTxnNo='" + orgTxnNo + '\'' +
                    ", orgRespTime='" + orgRespTime + '\'' +
                    ", orgReturnCode='" + orgReturnCode + '\'' +
                '}';
    }
}
