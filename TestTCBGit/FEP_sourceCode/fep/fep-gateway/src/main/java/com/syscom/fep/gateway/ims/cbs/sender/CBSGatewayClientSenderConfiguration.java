package com.syscom.fep.gateway.ims.cbs.sender;

import com.ibm.ims.connect.ApiProperties;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayClientConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

public class CBSGatewayClientSenderConfiguration extends CBSGatewayClientConfiguration {
    /**
     * pingIMS的時間間隔, 單位毫秒
     */
    private long pingIMSInterval = 5000L;
    /**
     * 對應的Receiver腳位是否已經連線
     */
    private boolean receiverConnected;
    private int imsConnectTimeout = ApiProperties.TIMEOUT_5_SECONDS;

    @Override
    public final SocketType getSocketType() {
        return SocketType.Sender;
    }

    @Override
    public final void setSocketType(SocketType socketType) {
        super.setSocketType(SocketType.Sender);
    }

    public long getPingIMSInterval() {
        return pingIMSInterval;
    }

    public void setPingIMSInterval(long pingIMSInterval) {
        this.pingIMSInterval = pingIMSInterval;
    }

    public synchronized boolean isReceiverConnected() {
        return receiverConnected;
    }

    public synchronized void setReceiverConnected(boolean receiverConnected) {
        this.receiverConnected = receiverConnected;
    }

    public int getImsConnectTimeout() {
        return imsConnectTimeout;
    }

    public void setImsConnectTimeout(int imsConnectTimeout) {
        this.imsConnectTimeout = imsConnectTimeout;
    }
}
