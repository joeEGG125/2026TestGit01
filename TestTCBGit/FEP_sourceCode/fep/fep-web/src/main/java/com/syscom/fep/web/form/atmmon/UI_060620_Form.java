package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060620_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String time;

    private String subSystemDdl;

    private String dtTransactDate;

    private String txTransactTimeBEG;

    private String txTransactTimeEND;

    private String txtIP;

    private String txtATMNo;
    
    private String txtArErdescription;

    private String applicationDdl;

    private String arNo;

    private String lblDate;
    private String errorCode;
    private Boolean notify;
    private String lblTime;
    private String externalCode;
    private String severity;
    private String lblHost;
    private String exSubCode;
    private String description;
    private String remark;
    private String responsible;
    private String notifyMail;
    private String action;
    private String arMessage;
    private String apMessage;
    private String EJ;
    private String ATMNO;
    private String ATMSeq;
    private String STAN;
    private String SBK;
    private String DesBkno;
    private String FiscRC;
    private String Channel;
    private String ProgramName;
    private String TxUser;
    private String MessageId;
    private String TxSource;
    private String TxErrDesc;

    public String getApMessage() {
        return apMessage;
    }

    public void setApMessage(String apMessage) {
        this.apMessage = apMessage;
    }

    public String getEJ() {
        return EJ;
    }

    public void setEJ(String EJ) {
        this.EJ = EJ;
    }

    public String getATMNO() {
        return ATMNO;
    }

    public void setATMNO(String ATMNO) {
        this.ATMNO = ATMNO;
    }

    public String getATMSeq() {
        return ATMSeq;
    }

    public void setATMSeq(String ATMSeq) {
        this.ATMSeq = ATMSeq;
    }

    public String getSTAN() {
        return STAN;
    }

    public void setSTAN(String STAN) {
        this.STAN = STAN;
    }

    public String getSBK() {
        return SBK;
    }

    public void setSBK(String SBK) {
        this.SBK = SBK;
    }

    public String getDesBkno() {
        return DesBkno;
    }

    public void setDesBkno(String desBkno) {
        DesBkno = desBkno;
    }

    public String getFiscRC() {
        return FiscRC;
    }

    public void setFiscRC(String fiscRC) {
        FiscRC = fiscRC;
    }

    public String getChannel() {
        return Channel;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public String getProgramName() {
        return ProgramName;
    }

    public void setProgramName(String programName) {
        ProgramName = programName;
    }

    public String getTxUser() {
        return TxUser;
    }

    public void setTxUser(String txUser) {
        TxUser = txUser;
    }

    public String getMessageId() {
        return MessageId;
    }

    public void setMessageId(String messageId) {
        MessageId = messageId;
    }

    public String getTxSource() {
        return TxSource;
    }

    public void setTxSource(String txSource) {
        TxSource = txSource;
    }

    public String getTxErrDesc() {
        return TxErrDesc;
    }

    public void setTxErrDesc(String txErrDesc) {
        TxErrDesc = txErrDesc;
    }

    public String getArMessage() {
        return arMessage;
    }

    public void setArMessage(String arMessage) {
        this.arMessage = arMessage;
    }

    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getExSubCode() {
        return exSubCode;
    }

    public void setExSubCode(String exSubCode) {
        this.exSubCode = exSubCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getNotifyMail() {
        return notifyMail;
    }

    public void setNotifyMail(String notifyMail) {
        this.notifyMail = notifyMail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String lylSys;
    private String lblSubSys;
    private Integer pageNo;


    public String getArNo() {
        return arNo;
    }

    public void setArNo(String arNo) {
        this.arNo = arNo;
    }

    public UI_060620_Form() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSubSystemDdl() {
        return subSystemDdl;
    }

    public void setSubSystemDdl(String subSystemDdl) {
        this.subSystemDdl = subSystemDdl;
    }

    public String getDtTransactDate() {
        return dtTransactDate;
    }

    public void setDtTransactDate(String dtTransactDate) {
        this.dtTransactDate = dtTransactDate;
    }

    public String getTxTransactTimeBEG() {
        return txTransactTimeBEG;
    }

    public void setTxTransactTimeBEG(String txTransactTimeBEG) {
        this.txTransactTimeBEG = txTransactTimeBEG;
    }

    public String getTxTransactTimeEND() {
        return txTransactTimeEND;
    }

    public void setTxTransactTimeEND(String txTransactTimeEND) {
        this.txTransactTimeEND = txTransactTimeEND;
    }

    public String getTxtATMNo() {
        return txtATMNo;
    }

    public void setTxtATMNo(String txtATMNo) {
        this.txtATMNo = txtATMNo;
    }

    public String getApplicationDdl() {
        return applicationDdl;
    }

    public void setApplicationDdl(String applicationDdl) {
        this.applicationDdl = applicationDdl;
    }

    public String getTxtIP() {
        return txtIP;
    }

    public void setTxtIP(String txtIP) {
        this.txtIP = txtIP;
    }

    public String getLblDate() {
        return lblDate;
    }

    public void setLblDate(String lblDate) {
        this.lblDate = lblDate;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getLblTime() {
        return lblTime;
    }

    public void setLblTime(String lblTime) {
        this.lblTime = lblTime;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String getLblHost() {
        return lblHost;
    }

    public void setLblHost(String lblHost) {
        this.lblHost = lblHost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLylSys() {
        return lylSys;
    }

    public void setLylSys(String lylSys) {
        this.lylSys = lylSys;
    }

    public String getLblSubSys() {
        return lblSubSys;
    }

    public void setLblSubSys(String lblSubSys) {
        this.lblSubSys = lblSubSys;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

	public String getTxtArErdescription() {
		return txtArErdescription;
	}

	public void setTxtArErdescription(String txtArErdescription) {
		this.txtArErdescription = txtArErdescription;
	}



}
