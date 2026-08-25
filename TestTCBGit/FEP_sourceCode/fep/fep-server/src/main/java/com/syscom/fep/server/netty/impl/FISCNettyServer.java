package com.syscom.fep.server.netty.impl;

import com.syscom.fep.invoker.netty.SimpleNettyBaseCommuByteToMessageDecoder;
import com.syscom.fep.server.controller.restful.FISCController;
import com.syscom.fep.server.netty.BaseNettyServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.ByteToMessageDecoder;

public class FISCNettyServer extends BaseNettyServer<FISCNettyServerConfiguration, FISCController> {
    /**
     * fiscgw送給server-fisc的交易, 照理是不用等回應的
     * 也就是fiscgw送給service-fisc的所有交易, 都是one way
     * AA回respnse給fiscgw是透過fiscadapter, 不是走原路回
     *
     * @param channel
     * @param s
     */
    @Override
    protected void responseMessageToClient(Channel channel, String s) {}

    /**
     * 由子類去實作電文解碼器, 用來處理一些特殊的電文
     *
     * @return
     */
    @Override
    protected ByteToMessageDecoder getByteToMessageDecoder() {
        return new SimpleNettyBaseCommuByteToMessageDecoder(this);
    }
}
