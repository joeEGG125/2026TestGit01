package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.vo.enums.UnisysType;

public class UnisysAdapter extends AdapterBase{

    private String TxId;
    public final String getTxId() {
        return TxId;
    }
    public final void setTxId(String value) {
        TxId = value;
    }
    private String messageFromUnisys;
    private String MessageToUnisys;
    public final String getMessageToUnisys() {
        return MessageToUnisys;
    }
    public final void setMessageToUnisys(String value) {
        MessageToUnisys = value;
    }
    public UnisysType hostType;

    public UnisysType getHostType() {
        return hostType;
    }

    public void setHostType(UnisysType hostType) {
        this.hostType = hostType;
    }

    public String getMessageFromUnisys() {
        return messageFromUnisys;
    }

    public void setMessageFromUnisys(String messageFromUnisys) {
        this.messageFromUnisys = messageFromUnisys;
    }

    @SuppressWarnings("unused")
	private MessageBase _txData;


   /* private MessageCallback Callback;  todo
    private MessageQueue replyQueue;*/
    public UnisysAdapter(MessageBase txData) {
        _txData = txData;
        //ToQueue = txData.TxSubSystem == SubSystem.RM ?
        //                            new MessageQueue(CMNConfig.Instance().TO_ATMP_RM) :
        //                            new MessageQueue(CMNConfig.Instance().TO_ATMP);
        //RcvQueue.Formatter = new XmlMessageFormatter(new Type[] { typeof(string) });
        //ToQueue.Formatter = new XmlMessageFormatter(new Type[] { typeof(string) });
/*        Callback = new MessageCallback(ReveiveCompleted);  todo
        //InitialSocket();
        replyQueue = new MessageQueue(replyQueueName);*/
    }

    @Override
    public FEPReturnCode sendReceive() {
        return null;
    }


}
