package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.t24.T24PreClass;

public class T24Data extends MessageBase  {

    private String txMessage;
    private T24PreClass txObject;

    public String getTxMessage() {
        return txMessage;
    }

    public void setTxMessage(String txMessage) {
        this.txMessage = txMessage;
    }

    public T24PreClass getTxObject() {
        return txObject;
    }

    public void setTxObject(T24PreClass txObject) {
        this.txObject = txObject;
    }
}
