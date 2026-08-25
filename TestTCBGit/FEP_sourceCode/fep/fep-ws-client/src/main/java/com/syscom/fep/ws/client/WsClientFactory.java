package com.syscom.fep.ws.client;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.ws.client.entity.WsClientConfig;
import com.syscom.fep.ws.client.entity.WsClientType;
import com.syscom.fep.ws.client.entity.fisc.FISCMessageOut;
import com.syscom.fep.ws.client.fisc.WsFISCClient;
import com.syscom.fep.ws.client.t24.WsT24Client;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class WsClientFactory {

    public <MessageOut, MessageIn> MessageIn sendReceive(WsClientType clientType, String uri, MessageOut messageOut) throws Exception {
        WsClientConfig clientConfig = new WsClientConfig();
        clientConfig.setClientType(clientType);
        clientConfig.setUri(uri);
        return this.sendReceive(clientConfig, messageOut);
    }

    @SuppressWarnings("unchecked")
    public <MessageOut, MessageIn> MessageIn sendReceive(WsClientConfig clientConfig, MessageOut messageOut) throws Exception {
        switch (clientConfig.getClientType()) {
            case FISC:
                WsFISCClient fiscClient = new WsFISCClient(clientConfig);
                return (MessageIn) fiscClient.sendReceive((FISCMessageOut) messageOut);
            case T24:
                WsT24Client t24Client = new WsT24Client(clientConfig);
                return (MessageIn) t24Client.sendReceive((String) messageOut);
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported WsClientType = [", clientConfig.getClientType().name(), "]");
        }
    }
}
