package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil.Scope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleNettyServerProcessorMultiManager<Processor extends SimpleNettyServerProcessor<?, ?>> extends SimpleNettyProcessorManager<Processor> {
    private final Class<Processor> processorClass;
    private final Map<String, Processor> channelIdToProcessorMap = new ConcurrentHashMap<>();

    public SimpleNettyServerProcessorMultiManager(Class<Processor> processorClass) {
        this.processorClass = processorClass;
    }

    @Override
    public Processor addProcessor(ChannelHandlerContext ctx) {
        this.putMDC(ctx.channel());
        Channel channel = ctx.channel();
        String channelId = channel.id().asLongText();
        Processor processor = channelIdToProcessorMap.get(channelId);
        if (processor == null) {
            try {
                processor = SpringBeanFactoryUtil.registerBean(processorClass, Scope.Prototype);
                processor.setClientIP(getRemoteIp(channel)); // 塞入Client的IP
                processor.setClientPort(getRemotePort(channel)); // 塞入Client的Port
                channelIdToProcessorMap.put(channelId, processor);
                infoMessage("[", processor.getName(), "]", channelInfo(channel), "Add processor to manager succeed, processor = [", processor, "]");
            } catch (Exception e) {
                errorMessage(e, channelInfo(channel), "Add processor to manager failed with exception occur");
            }
        } else {
            warnMessage("[", processor.getName(), "]", channelInfo(channel), "processor exist in manager, processor = [", processor, "]");
        }
        return processor;
    }

    @Override
    public Processor removeProcessor(ChannelHandlerContext ctx) {
        this.putMDC(ctx.channel());
        Channel channel = ctx.channel();
        String channelId = channel.id().asLongText();
        Processor processor = channelIdToProcessorMap.remove(channelId);
        if (processor != null) {
            infoMessage("[", processor.getName(), "]", channelInfo(channel), "Remove processor from manager succeed, processor = [", processor, "]");
        }
        return processor;
    }

    @Override
    public Processor getProcessor(ChannelHandlerContext ctx) {
        this.putMDC(ctx.channel());
        String channelId = ctx.channel().id().asLongText();
        Processor processor = channelIdToProcessorMap.get(channelId);
        return processor;
    }
}