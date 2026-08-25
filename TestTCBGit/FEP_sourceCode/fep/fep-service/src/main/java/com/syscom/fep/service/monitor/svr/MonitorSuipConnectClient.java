package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClientShort;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

/**
 * 連接suip的Socket物件
 *
 * @author Richard
 */
public class MonitorSuipConnectClient extends SimpleNettyClientShort<MonitorSuipConnectClientConfiguration, String, String> {

    @Override
    public String getName() {
        return StringUtils.join(SvrConst.SVR_APPMON, "SuipConnectClient");
    }

    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return StringUtil.toHex(bytes);
    }

    @Override
    protected byte[] messageOutToBytes(String s) {
        return ConvertUtil.hexToBytes(s);
    }

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {}
}
