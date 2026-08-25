package com.syscom.fep.ws.client.fisc;

import com.syscom.fep.ws.client.WsGatewayClient;
import com.syscom.fep.ws.client.entity.WsClientConfig;
import com.syscom.fep.ws.client.entity.fisc.FISCMessageOut;
import com.syscom.fep.ws.client.entity.WsClientType;
import com.syscom.fep.ws.client.fisc.wsdl.ObjectFactory;
import com.syscom.fep.ws.client.fisc.wsdl.SendReceive;
import com.syscom.fep.ws.client.fisc.wsdl.SendReceiveResponse;
import org.apache.commons.lang3.StringUtils;

public class WsFISCClient extends WsGatewayClient<SendReceive, SendReceiveResponse, FISCMessageOut, String> {

    public WsFISCClient(WsClientConfig clientConfig) {
        super(clientConfig);
    }

    @Override
    public SendReceive createWsRequest(FISCMessageOut messageOut) {
        SendReceive request = new SendReceive();
        request.setData(new ObjectFactory().createSendReceiveData(messageOut.getMessage()));
        request.setTimeout(messageOut.getTimeout());
        return request;
    }

    @Override
    public String transferToMessageIn(SendReceiveResponse response) {
        return response.getSendReceiveResult().getValue();
    }

    @Override
    protected String getURI() {
        String uri = super.getURI();
        // 如果db中沒有設置，則返回預設的測試url
        return StringUtils.isBlank(uri) ? "http://localhost:18738/FISCGWWS" : uri;
    }

    @Override
    protected String getSoapActionCallbackURI() {
        return "http://tempuri.org/IFISCGWWS/SendReceive";
    }

    @Override
    protected String getWsdlPackage() {
        return "com.syscom.fep.ws.client.fisc.wsdl";
    }

    @Override
    public WsClientType getWsClientType() {
        return WsClientType.FISC;
    }
}
