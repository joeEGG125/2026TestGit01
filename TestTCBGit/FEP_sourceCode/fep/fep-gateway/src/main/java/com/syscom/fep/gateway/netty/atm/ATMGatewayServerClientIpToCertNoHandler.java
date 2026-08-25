package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.gateway.netty.NettyTransmissionSslHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class ATMGatewayServerClientIpToCertNoHandler extends NettyTransmissionSslHandler {
    @Autowired
    private ATMGatewayServerConfiguration atmGatewayServerConfiguration;

    @Override
    public String getCertAlias(String clientIp) {
        // 如果是壓力測試, 則進行同步化處理
        if (atmGatewayServerConfiguration.isFetchCertSync()) {
            synchronized (this) {
                return super.getCertAlias(clientIp);
            }
        }
        return super.getCertAlias(clientIp);
    }

    @Override
    public Integer getCertIndex(String clientIp) {
        // 如果是壓力測試, 則進行同步化處理
        if (atmGatewayServerConfiguration.isFetchCertSync()) {
            synchronized (this) {
                return super.getCertIndex(clientIp);
            }
        }
        return super.getCertIndex(clientIp);
    }
}
