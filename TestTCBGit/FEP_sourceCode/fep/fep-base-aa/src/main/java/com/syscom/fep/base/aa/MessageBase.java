package com.syscom.fep.base.aa;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FunctionType;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.model.Card;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Msgctl;

public abstract class MessageBase extends FEPBase {
    /**
     * 交易之MessageID
     */
    private String messageID;
    /**
     * 交易上行電文
     */
    private String txRequestMessage;
    /**
     * 交易下行電文
     */
    private String txResponseMessage;
    /**
     * 交易發動來源
     */
    private FEPChannel txChannel;
    /**
     * 交易種類
     */
    private MessageFlow messageFlowType;
    /**
     * 交易的子系統別
     */
    private SubSystem txSubSystem;
    /**
     * 控制交易是否執行
     */
    private boolean txStatus;
    /**
     * 交易的功能類別,例如提款,查詢,繳費等
     */
    private FunctionType messageFunctionType;
    /**
     * 對應AA名稱
     */
    private String aaName;
    /**
     * 交易帳號
     */
    private String txAccount;
    /**
     * 與交易關聯的MsgCtl物件
     */
    private Msgctl msgCtl;
    /**
     * 與交易關聯的FEPTxn物件
     */
    private Feptxn feptxn;
    private Feptxntcb feptxntcb;
    private FeptxnDao feptxnDao;
    private String messageCorrelationID;
    private String fpb2;
    private String fpb2New;
    private Card card; // add by kyo for hostbusiness 共同存取 Card 的資料
    private Intltxn intltxn;
    /**
     * 使用約定轉帳記號
     */
    private String tfrFlag;
    /**
     * 財金營業日
     */
    private String tbsdyFISC;
    /**
     * CHANNEL資料, Handler用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入
     */
    private Channel channel;

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTxRequestMessage() {
        return txRequestMessage;
    }

    public void setTxRequestMessage(String txRequestMessage) {
        this.txRequestMessage = txRequestMessage;
    }

    public String getTxResponseMessage() {
        return txResponseMessage;
    }

    public void setTxResponseMessage(String txResponseMessage) {
        this.txResponseMessage = txResponseMessage;
    }

    public FEPChannel getTxChannel() {
        return txChannel;
    }

    public void setTxChannel(FEPChannel txChannel) {
        this.txChannel = txChannel;
    }

    public MessageFlow getMessageFlowType() {
        return messageFlowType;
    }

    public void setMessageFlowType(MessageFlow messageFlowType) {
        this.messageFlowType = messageFlowType;
    }

    public SubSystem getTxSubSystem() {
        return txSubSystem;
    }

    public void setTxSubSystem(SubSystem txSubSystem) {
        this.txSubSystem = txSubSystem;
    }

    public boolean isTxStatus() {
        return txStatus;
    }

    public void setTxStatus(boolean txStatus) {
        this.txStatus = txStatus;
    }

    public FunctionType getMessageFunctionType() {
        return messageFunctionType;
    }

    public void setMessageFunctionType(FunctionType messageFunctionType) {
        this.messageFunctionType = messageFunctionType;
    }

    public String getAaName() {
        return aaName;
    }

    public void setAaName(String aaName) {
        this.aaName = aaName;
    }

    public String getTxAccount() {
        return txAccount;
    }

    public void setTxAccount(String txAccount) {
        this.txAccount = txAccount;
    }

    public Msgctl getMsgCtl() {
        return msgCtl;
    }

    public void setMsgCtl(Msgctl msgCtl) {
        this.msgCtl = msgCtl;
    }

    public Feptxn getFeptxn() {
        return feptxn;
    }

    public void setFeptxn(Feptxn feptxn) {
        this.feptxn = feptxn;
    }

    public FeptxnDao getFeptxnDao() {
        return feptxnDao;
    }

    public void setFeptxnDao(FeptxnDao feptxnDao) {
        this.feptxnDao = feptxnDao;
    }

    public String getMessageCorrelationID() {
        return messageCorrelationID;
    }

    public void setMessageCorrelationID(String messageCorrelationID) {
        this.messageCorrelationID = messageCorrelationID;
    }

    public String getFpb2() {
        return fpb2;
    }

    public void setFpb2(String fpb2) {
        this.fpb2 = fpb2;
    }

    public String getFpb2New() {
        return fpb2New;
    }

    public void setFpb2New(String fpb2New) {
        this.fpb2New = fpb2New;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getTfrFlag() {
        return tfrFlag;
    }

    public void setTfrFlag(String tfrFlag) {
        this.tfrFlag = tfrFlag;
    }

    public String getTbsdyFISC() {
        return tbsdyFISC;
    }

    public void setTbsdyFISC(String tbsdyFISC) {
        this.tbsdyFISC = tbsdyFISC;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

	public Feptxntcb getFeptxntcb() {
		return feptxntcb;
	}

	public void setFeptxntcb(Feptxntcb feptxntcb) {
		this.feptxntcb = feptxntcb;
	}
    
	public Intltxn getIntltxn() {
		return intltxn;
	}

	public void setIntltxn(Intltxn intltxn) {
		this.intltxn = intltxn;
	}
}
