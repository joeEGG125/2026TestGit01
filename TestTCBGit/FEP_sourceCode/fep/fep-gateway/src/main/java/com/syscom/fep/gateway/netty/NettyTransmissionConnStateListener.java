package com.syscom.fep.gateway.netty;

import io.netty.channel.Channel;

import java.util.EventListener;

public interface NettyTransmissionConnStateListener extends EventListener {

    void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t);

}
