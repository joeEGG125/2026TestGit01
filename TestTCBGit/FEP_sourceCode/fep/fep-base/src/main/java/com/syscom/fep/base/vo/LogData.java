package com.syscom.fep.base.vo;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.syscom.fep.base.enums.DBIOMethod;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("LogData")
public class LogData implements Serializable {
    private static final long serialVersionUID = 1315581087837734721L;
    @XStreamAlias("Channel")
    private FEPChannel channel;
    @XStreamAlias("SubSys")
    private SubSystem subSys;
    @XStreamAlias("MessageId")
    private String messageId = StringUtils.EMPTY;
    @XStreamAlias("Message")
    private String message = StringUtils.EMPTY;
    @XStreamAlias("ProgramName")
    private String programName = StringUtils.EMPTY;
    @XStreamAlias("MessageFlowType")
    private MessageFlow messageFlowType;
    @XStreamAlias("ProgramFlowType")
    private ProgramFlow programFlowType;
    @XStreamAlias("STAN")
    private String stan = StringUtils.EMPTY;
    @XStreamAlias("ATMNo")
    private String atmNo = StringUtils.EMPTY;
    @XStreamAlias("ATMSeq")
    private String atmSeq = StringUtils.EMPTY;
    @XStreamAlias("TxDate")
    private String txDate = StringUtils.EMPTY;
    @XStreamAlias("TroutBank")
    private String troutBank = StringUtils.EMPTY;
    @XStreamAlias("TroutActno")
    private String troutActno = StringUtils.EMPTY;
    @XStreamAlias("TrinBank")
    private String trinBank = StringUtils.EMPTY;
    @XStreamAlias("TrinActno")
    private String trinActno = StringUtils.EMPTY;
    @XStreamAlias("Chact")
    private String chact = StringUtils.EMPTY;
    @XStreamAlias("EJ")
    private int ej;
    @XStreamAlias("Remark")
    private String remark = StringUtils.EMPTY;
    @XStreamAlias("ReturnCode")
    private FEPReturnCode returnCode;
    @XStreamAlias("Bkno")
    private String bkno = StringUtils.EMPTY;
    @XStreamOmitField
    private transient Throwable programException;
    @XStreamAlias("TableName")
    private String tableName = StringUtils.EMPTY;
    @XStreamAlias("TableDescription")
    private String tableDescription = StringUtils.EMPTY;
    @XStreamAlias("PrimaryKeys")
    private String primaryKeys = StringUtils.EMPTY;
    @XStreamAlias("IOMethd")
    private DBIOMethod ioMethd;
    @XStreamAlias("ExternalCode")
    private String externalCode = StringUtils.EMPTY;
    @XStreamAlias("ResponseMessage")
    private String responseMessage = StringUtils.EMPTY;
    @XStreamAlias("Notification")
    private boolean notification;
    @XStreamAlias("Step")
    private int step;
    @XStreamAlias("PCode")
    private String pCode = StringUtils.EMPTY;
    @XStreamAlias("DesBkno")
    private String desBkno = StringUtils.EMPTY;
    @XStreamAlias("FiscRC")
    private String fiscRC = StringUtils.EMPTY;
    @XStreamAlias("MessageGroup")
    private String messageGroup = StringUtils.EMPTY;
    @XStreamAlias("MessageParm13")
    private String messageParm13 = StringUtils.EMPTY;
    @XStreamAlias("MessageParm14")
    private String messageParm14 = StringUtils.EMPTY;
    @XStreamAlias("TxUser")
    private String txUser = StringUtils.EMPTY;
    @XStreamAlias("Operator")
    private String operator = StringUtils.EMPTY;
    @XStreamAlias("Responsible")
    private String responsible = StringUtils.EMPTY;
    @XStreamAlias("IsNewWebATM")
    private boolean isNewWebATM;
    private String messageCorrelationId;
    private String serviceUrl;
    @XStreamAlias("TxRquid")
    private String txRquid;
    @XStreamAlias("DoNotSendEMSQueue")
    private boolean doNotSendEMSQueue;
    @XStreamAlias("NotifyPhone")
    private String notifyPhone = StringUtils.EMPTY;

    public FEPChannel getChannel() {
        return channel;
    }

    public void setChannel(FEPChannel channel) {
        this.channel = channel;
    }

    public SubSystem getSubSys() {
        return subSys;
    }

    public void setSubSys(SubSystem subSys) {
        this.subSys = subSys;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public MessageFlow getMessageFlowType() {
        return messageFlowType;
    }

    public void setMessageFlowType(MessageFlow messageFlowType) {
        this.messageFlowType = messageFlowType;
    }

    public ProgramFlow getProgramFlowType() {
        return programFlowType;
    }

    public void setProgramFlowType(ProgramFlow programFlowType) {
        this.programFlowType = programFlowType;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getTroutBank() {
        return troutBank;
    }

    public void setTroutBank(String troutBank) {
        this.troutBank = troutBank;
    }

    public String getTroutActno() {
        return troutActno;
    }

    public void setTroutActno(String troutActno) {
        this.troutActno = troutActno;
    }

    public String getTrinBank() {
        return trinBank;
    }

    public void setTrinBank(String trinBank) {
        this.trinBank = trinBank;
    }

    public String getTrinActno() {
        return trinActno;
    }

    public void setTrinActno(String trinActno) {
        this.trinActno = trinActno;
    }

    public String getChact() {
        return chact;
    }

    public void setChact(String chact) {
        this.chact = chact;
    }

    public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public FEPReturnCode getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(FEPReturnCode returnCode) {
        this.returnCode = returnCode;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public Throwable getProgramException() {
        return programException;
    }

    public void setProgramException(Throwable programException) {
        this.programException = programException;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDescription() {
        return tableDescription;
    }

    public void setTableDescription(String tableDescription) {
        this.tableDescription = tableDescription;
    }

    public String getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(String primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public DBIOMethod getIoMethd() {
        return ioMethd;
    }

    public void setIoMethd(DBIOMethod ioMethd) {
        this.ioMethd = ioMethd;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    public String getDesBkno() {
        return desBkno;
    }

    public void setDesBkno(String desBkno) {
        this.desBkno = desBkno;
    }

    public String getFiscRC() {
        return fiscRC;
    }

    public void setFiscRC(String fiscRC) {
        this.fiscRC = fiscRC;
    }

    public String getMessageGroup() {
        return messageGroup;
    }

    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    public String getMessageParm13() {
        return messageParm13;
    }

    public void setMessageParm13(String messageParm13) {
        this.messageParm13 = messageParm13;
    }

    public String getMessageParm14() {
        return messageParm14;
    }

    public void setMessageParm14(String messageParm14) {
        this.messageParm14 = messageParm14;
    }

    public String getTxUser() {
        return txUser;
    }

    public void setTxUser(String txUser) {
        this.txUser = txUser;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public boolean isNewWebATM() {
        return isNewWebATM;
    }

    public void setNewWebATM(boolean isNewWebATM) {
        this.isNewWebATM = isNewWebATM;
    }

    public String getMessageCorrelationId() {
        return messageCorrelationId;
    }

    public void setMessageCorrelationId(String messageCorrelationId) {
        this.messageCorrelationId = messageCorrelationId;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
    }

    public boolean isDoNotSendEMSQueue() {
        return doNotSendEMSQueue;
    }

    public void setDoNotSendEMSQueue(boolean doNotSendEMSQueue) {
        this.doNotSendEMSQueue = doNotSendEMSQueue;
    }

    public String getNotifyPhone() {
        return notifyPhone;
    }

    public void setNotifyPhone(String notifyPhone) {
        this.notifyPhone = notifyPhone;
    }

    public void clear() {
        this.setEj(0);
        this.setMessageId(StringUtils.EMPTY);
        this.setAtmSeq(StringUtils.EMPTY);
        this.setMessage(StringUtils.EMPTY);
        this.setRemark(StringUtils.EMPTY);
        this.setBkno(StringUtils.EMPTY);
        this.setChact(StringUtils.EMPTY);
        this.setProgramException(null);
        this.setTrinActno(StringUtils.EMPTY);
        this.setTrinBank(StringUtils.EMPTY);
        this.setTroutActno(StringUtils.EMPTY);
        this.setTroutBank(StringUtils.EMPTY);
        this.setTxDate(StringUtils.EMPTY);
        this.setStan(StringUtils.EMPTY);
        this.setStep(0);
        this.setTxRquid(StringUtils.EMPTY);
        this.setDoNotSendEMSQueue(false);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
