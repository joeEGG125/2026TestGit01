package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import io.netty.channel.ChannelHandlerContext;

public class SimpleNettyServerProcessorSingletonManager<Processor extends SimpleNettyServerProcessor<?, ?>> extends SimpleNettyProcessorManager<Processor> {
    private Processor processor;

    public SimpleNettyServerProcessorSingletonManager(Class<Processor> processorClass) {
        processor = SpringBeanFactoryUtil.registerBean(processorClass);
    }

    @Override
    public Processor addProcessor(ChannelHandlerContext ctx) {
        this.putMDC(ctx.channel());
        Processor processor =  getProcessor();
        processor.setClientIP(getRemoteIp(ctx.channel())); // 塞入Client的IP
        processor.setClientPort(getRemotePort(ctx.channel())); // 塞入Client的Port
        return processor;
    }

    @Override
    public Processor removeProcessor(ChannelHandlerContext ctx) {
        return getProcessor();
    }

    @Override
    public Processor getProcessor(ChannelHandlerContext ctx) {
        return getProcessor();
    }

    public Processor getProcessor() {
        return processor;
    }
}