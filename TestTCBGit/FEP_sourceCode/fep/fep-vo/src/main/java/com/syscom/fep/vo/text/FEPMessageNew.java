package com.syscom.fep.vo.text;

public class FEPMessageNew {
    private String msgId;
    private String SOURCE_ID;
    private String DESTINATION_ID;
    private String MSG_TYPE;
    private String TERMID;
    private String EJFNO;
    private String BUSINESSDATE;
    private String CHANNEL_TX_DAT;
    private String CHANNEL_TX_TIME;
    private String CHANNEL_TX_CODE;
    private String scvRq;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSOURCE_ID() {
        return SOURCE_ID;
    }

    public void setSOURCE_ID(String SOURCE_ID) {
        this.SOURCE_ID = SOURCE_ID;
    }

    public String getDESTINATION_ID() {
        return DESTINATION_ID;
    }

    public void setDESTINATION_ID(String DESTINATION_ID) {
        this.DESTINATION_ID = DESTINATION_ID;
    }

    public String getMSG_TYPE() {
        return MSG_TYPE;
    }

    public void setMSG_TYPE(String MSG_TYPE) {
        this.MSG_TYPE = MSG_TYPE;
    }

    public String getTERMID() {
        return TERMID;
    }

    public void setTERMID(String TERMID) {
        this.TERMID = TERMID;
    }

    public String getEJFNO() {
        return EJFNO;
    }

    public void setEJFNO(String EJFNO) {
        this.EJFNO = EJFNO;
    }

    public String getBUSINESSDATE() {
        return BUSINESSDATE;
    }

    public void setBUSINESSDATE(String BUSINESSDATE) {
        this.BUSINESSDATE = BUSINESSDATE;
    }

    public String getCHANNEL_TX_DAT() {
        return CHANNEL_TX_DAT;
    }

    public void setCHANNEL_TX_DAT(String CHANNEL_TX_DAT) {
        this.CHANNEL_TX_DAT = CHANNEL_TX_DAT;
    }

    public String getCHANNEL_TX_TIME() {
        return CHANNEL_TX_TIME;
    }

    public void setCHANNEL_TX_TIME(String CHANNEL_TX_TIME) {
        this.CHANNEL_TX_TIME = CHANNEL_TX_TIME;
    }

    public String getCHANNEL_TX_CODE() {
        return CHANNEL_TX_CODE;
    }

    public void setCHANNEL_TX_CODE(String CHANNEL_TX_CODE) {
        this.CHANNEL_TX_CODE = CHANNEL_TX_CODE;
    }

    public String getScvRq() {
        return scvRq;
    }

    public void setScvRq(String scvRq) {
        this.scvRq = scvRq;
    }
}
