package com.syscom.fep.server.netty.impl;

import com.syscom.fep.invoker.netty.SimpleNettyBaseCommuByteToMessageDecoder;
import com.syscom.fep.server.controller.restful.ATMController;
import com.syscom.fep.server.netty.BaseNettyServer;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ATMNettyServer extends BaseNettyServer<ATMNettyServerConfiguration, ATMController> {
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
