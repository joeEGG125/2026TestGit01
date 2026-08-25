package com.syscom.fep.ws.client;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.ws.client.entity.WsClientConfig;
import com.syscom.fep.ws.client.entity.WsClientType;

public abstract class WsSimpleClient<WsRequest, WsResponse, MessageOut, MessageIn> implements WsClient<WsRequest, WsResponse, MessageOut, MessageIn> {
    private final WsClientConfig config;

    public WsSimpleClient(WsClientConfig config) {
        this.config = config;
    }

    /**
     * 發送請求電文, 接收回應電文
     *
     * @param messageOut
     * @return
     */
    @Override
    public MessageIn sendReceive(MessageOut messageOut) throws Exception {
        boolean isLogEnable = this.isLogEnable();
        WsClientType wsClientType = this.getWsClientType();
        String uri = this.getURI();
        try {
            if (isLogEnable) logHelper.info("[", wsClientType, "][", uri, "]", Const.MESSAGE_OUT, this.outToString(messageOut));
            WsRequest request = this.createWsRequest(messageOut);
            WsResponse response = this.sendReceiveRequest(request);
            MessageIn messageIn = this.transferToMessageIn(response);
            if (isLogEnable) logHelper.info("[", wsClientType, "][", uri, "]", Const.MESSAGE_IN, this.inToString(messageIn));
            return messageIn;
        } catch (Exception e) {
            logHelper.exceptionMsg(e, "[", wsClientType, "][", uri, "]sendReceive failed, message = [", e.getMessage(), "]");
            throw e;
        }
    }

    protected abstract WsResponse sendReceiveRequest(WsRequest request) throws Exception;

    protected String getURI() {
        return this.config.getUri();
    }
}
