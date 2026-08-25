package com.syscom.fep.invoker.netty;

import io.netty.channel.ChannelHandlerContext;

public abstract class SimpleNettyProcessorManager<Processor> extends SimpleNettyBaseMethod {

    public abstract Processor addProcessor(ChannelHandlerContext ctx);

    public abstract Processor removeProcessor(ChannelHandlerContext ctx);

    public abstract Processor getProcessor(ChannelHandlerContext ctx);

}
