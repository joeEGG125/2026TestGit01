package com.syscom.fep.gateway.ims.cbs.receiver;

import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayClientConfiguration;

public class CBSGatewayClientReceiverConfiguration extends CBSGatewayClientConfiguration {
    /**
     * 多久沒收到IMS來的電文時需要斷線重連的Timer
     */
    private long disConnectInterval = 120000;

    @Override
    public final SocketType getSocketType() {
        return SocketType.Receiver;
    }

    @Override
    public final void setSocketType(SocketType socketType) {
        super.setSocketType(SocketType.Receiver);
    }

    public long getDisConnectInterval() {
        return disConnectInterval;
    }

    public void setDisConnectInterval(long disConnectInterval) {
        this.disConnectInterval = disConnectInterval;
    }
}
