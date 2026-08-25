package com.syscom.fep.gateway.netty.sample.server;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

public class SampleGatewayServerProcessRequest extends NettyTransmissionChannelProcessRequestServer<SampleGatewayServerConfiguration> {

    /**
     * 處理Client進來的電文
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        this.logContext.clear();
        String message = StringUtil.toHex(bytes);
        // logging
        this.logContext.setSubSys(SubSystem.GW);
        this.logContext.setChannel(FEPChannel.ATM);
        this.logContext.setMessageFlowType(MessageFlow.Request);
        this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        this.logContext.setMessage(message);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
        this.logContext.setRemark(StringUtils.join(Gateway.SAMPLEGW, " received message from Client"));
        this.logMessage(this.logContext);
        try {
            Thread.sleep(5000); // just for test模擬業務處理時間
            // TODO just for test
            NettyTransmissionUtil.sendHexMessage(this, this.configuration, ctx.channel(), message);
            // logging
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setMessage(message);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            this.logContext.setRemark(StringUtils.join(Gateway.SAMPLEGW, " send message to Client"));
            this.logMessage(this.logContext);
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            sendEMS(this.logContext);
        }
    }
}
