package com.syscom.fep.invoker.netty;

import io.netty.channel.Channel;

import java.util.EventListener;

public interface SimpleNettyConnStateListener extends EventListener {

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t);

}
