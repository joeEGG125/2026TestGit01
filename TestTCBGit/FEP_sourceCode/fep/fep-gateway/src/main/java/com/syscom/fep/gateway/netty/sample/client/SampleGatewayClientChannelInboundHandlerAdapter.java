package com.syscom.fep.gateway.netty.sample.client;

import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterClient;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class SampleGatewayClientChannelInboundHandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterClient<SampleGatewayClientConfiguration, SampleGatewayClientProcessRequest> {}
