package com.syscom.fep.service.ems.vo;

import com.syscom.fep.base.enums.FEPReturnCode;
import org.apache.commons.lang3.StringUtils;

public class EMSApMessageData {
    private String txErrDesc = StringUtils.EMPTY;
    private String exSubCode = StringUtils.EMPTY;
    private String exStack = StringUtils.EMPTY;
    private String exSource = StringUtils.EMPTY;
    private String txExternalCode = StringUtils.EMPTY;
    private String txPK = StringUtils.EMPTY;
    private String txSource = StringUtils.EMPTY;
    private boolean notification;
    private String rcString = StringUtils.EMPTY;
    private String ej = StringUtils.EMPTY;
    private String atmNo = StringUtils.EMPTY;
    private String atmSeq = StringUtils.EMPTY;
    private String stan = StringUtils.EMPTY;
    private String pcode = StringUtils.EMPTY;
    private String messageId = StringUtils.EMPTY;
    private String programName = StringUtils.EMPTY;
    private String bkno = StringUtils.EMPTY;
    private String desBkno = StringUtils.EMPTY;
    private String troutBank = StringUtils.EMPTY;
    private String trinBank = StringUtils.EMPTY;
    private String notifyMail = StringUtils.EMPTY;
    private String notifyPhone = StringUtils.EMPTY;
    private String messageName = StringUtils.EMPTY;
    private FEPReturnCode rtnCode;
    private String remark = StringUtils.EMPTY;

    public String getTxErrDesc() {
        return txErrDesc;
    }

    public void setTxErrDesc(String txErrDesc) {
        this.txErrDesc = txErrDesc;
    }

    public String getExSubCode() {
        return exSubCode;
    }

    public void setExSubCode(String exSubCode) {
        this.exSubCode = exSubCode;
    }

    public String getExStack() {
        return exStack;
    }

    public void setExStack(String exStack) {
        this.exStack = exStack;
    }

    public String getExSource() {
        return exSource;
    }

    public void setExSource(String exSource) {
        this.exSource = exSource;
    }

    public String getTxExternalCode() {
        return txExternalCode;
    }

    public void setTxExternalCode(String txExternalCode) {
        this.txExternalCode = txExternalCode;
    }

    public String getTxPK() {
        return txPK;
    }

    public void setTxPK(String txPK) {
        this.txPK = txPK;
    }

    public String getTxSource() {
        return txSource;
    }

    public void setTxSource(String txSource) {
        this.txSource = txSource;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getRcString() {
        return rcString;
    }

    public void setRcString(String rcString) {
        this.rcString = rcString;
    }

    public String getEj() {
        return ej;
    }

    public void setEj(String ej) {
        this.ej = ej;
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

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getPcode() {
        return pcode;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public String getDesBkno() {
        return desBkno;
    }

    public void setDesBkno(String desBkno) {
        this.desBkno = desBkno;
    }

    public String getTroutBank() {
        return troutBank;
    }

    public void setTroutBank(String troutBank) {
        this.troutBank = troutBank;
    }

    public String getTrinBank() {
        return trinBank;
    }

    public void setTrinBank(String trinBank) {
        this.trinBank = trinBank;
    }

    public String getNotifyMail() {
        return notifyMail;
    }

    public void setNotifyMail(String notifyMail) {
        this.notifyMail = notifyMail;
    }

    public String getNotifyPhone() {
        return notifyPhone;
    }

    public void setNotifyPhone(String notifyPhone) {
        this.notifyPhone = notifyPhone;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public FEPReturnCode getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(FEPReturnCode rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
