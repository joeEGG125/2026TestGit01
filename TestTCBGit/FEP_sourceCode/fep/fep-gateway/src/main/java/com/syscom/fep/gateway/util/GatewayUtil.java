package com.syscom.fep.gateway.util;

import com.syscom.fep.gateway.netty.NettyTransmissionConfiguration;
import org.springframework.stereotype.Component;

@Component
public class GatewayUtil {
    /**
     * 計算呼叫FEP Service via Socket的超時時間
     *
     * @param configuration
     * @return
     */
    public int getTimeout(NettyTransmissionConfiguration configuration) {
        return configuration.getTimeout() + 5 * 1000;
    }
}
