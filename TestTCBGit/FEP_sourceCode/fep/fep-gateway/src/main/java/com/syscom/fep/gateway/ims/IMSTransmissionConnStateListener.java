package com.syscom.fep.gateway.ims;

import java.util.EventListener;

public interface IMSTransmissionConnStateListener<Configuration extends IMSTransmissionConfiguration> extends EventListener {

    /**
     * 接收連線狀態發生變化
     *
     * @param configuration
     * @param state
     * @param t
     */
    void connStateChanged(Configuration configuration, IMSTransmissionConnState state, Throwable t);

}
