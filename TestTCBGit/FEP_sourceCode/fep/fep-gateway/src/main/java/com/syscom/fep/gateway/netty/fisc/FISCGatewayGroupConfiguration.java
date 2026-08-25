package com.syscom.fep.gateway.netty.fisc;

/**
 * 針對FISCGatewayGroup的配置信息
 *
 * @author Richard
 */
public class FISCGatewayGroupConfiguration {
    /**
     * 當FISC任意一個腳位斷線後, 是否斷開所有FISC連線
     */
    private boolean disconnectAllAfterDisconnectAnyPort = true;

    public boolean isDisconnectAllAfterDisconnectAnyPort() {
        return disconnectAllAfterDisconnectAnyPort;
    }

    public void setDisconnectAllAfterDisconnectAnyPort(boolean disconnectAllAfterDisconnectAnyPort) {
        this.disconnectAllAfterDisconnectAnyPort = disconnectAllAfterDisconnectAnyPort;
    }
}
