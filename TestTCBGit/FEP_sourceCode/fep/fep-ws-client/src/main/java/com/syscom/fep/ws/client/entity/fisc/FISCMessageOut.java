package com.syscom.fep.ws.client.entity.fisc;

import com.syscom.fep.ws.client.entity.BaseMessage;

public class FISCMessageOut extends BaseMessage<String> {
    private static final long serialVersionUID = 1055068002501839777L;

    private int timeout;

    public FISCMessageOut(String requestData, int timeout) {
        this.message = requestData;
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
