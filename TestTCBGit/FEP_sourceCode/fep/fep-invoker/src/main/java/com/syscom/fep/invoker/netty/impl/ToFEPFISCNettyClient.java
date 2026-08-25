package com.syscom.fep.invoker.netty.impl;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.invoker.netty.SimpleNettyBaseCommuByteToMessageDecoder;
import com.syscom.fep.invoker.netty.SimpleNettyClientShort;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.vo.communication.BaseCommu;
import io.netty.channel.Channel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;

/**
 * 發送socket到FEP FISC
 *
 * @author Richard
 */
public class ToFEPFISCNettyClient extends SimpleNettyClientShort<ToFEPFISCNettyClientConfiguration, String, BaseCommu> {

    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] messageOutToBytes(BaseCommu messageOut) {
        return ConvertUtil.toBytes(messageOut.toString(), StandardCharsets.UTF_8);
    }

    /**
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {}

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
