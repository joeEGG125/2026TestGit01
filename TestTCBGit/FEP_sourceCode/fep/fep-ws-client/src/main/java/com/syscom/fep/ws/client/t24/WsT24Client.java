package com.syscom.fep.ws.client.t24;

import com.syscom.fep.ws.client.WsGatewayClient;
import com.syscom.fep.ws.client.entity.WsClientConfig;
import com.syscom.fep.ws.client.entity.WsClientType;
import com.syscom.fep.ws.client.t24.wsdl.CallOfs;
import com.syscom.fep.ws.client.t24.wsdl.CallOfsResponse;
import org.apache.commons.lang3.StringUtils;

public class WsT24Client extends WsGatewayClient<CallOfs, CallOfsResponse, String, String> {

    public WsT24Client(WsClientConfig clientConfig) {
        super(clientConfig);
    }

    @Override
    public CallOfs createWsRequest(String messageOut) {
        CallOfs request = new CallOfs();
        request.setOfsRequest(messageOut);
        return request;
    }

    @Override
    public String transferToMessageIn(CallOfsResponse response) {
        return response.getOfsResponse();
    }

    @Override
    protected String getURI() {
        String uri = super.getURI();
        // 如果db中沒有設置，則返回預設的測試url
        return StringUtils.isBlank(uri) ? "http://localhost:18738/FEP/NativeMessage.svc" : uri;
    }

    @Override
    protected String getSoapActionCallbackURI() {
        return "http://temenos.com/FEP/T24WebServicesImpl/callOfsRequest";
    }

    @Override
    protected String getWsdlPackage() {
        return "com.syscom.fep.ws.client.t24.wsdl";
    }

    @Override
    public WsClientType getWsClientType() {
        return WsClientType.T24;
    }
}
