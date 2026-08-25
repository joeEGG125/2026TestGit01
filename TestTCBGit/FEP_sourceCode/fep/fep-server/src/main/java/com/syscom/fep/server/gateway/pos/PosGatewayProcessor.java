package com.syscom.fep.server.gateway.pos;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.invoker.netty.SimpleNettyServerProcessor;
import com.syscom.fep.server.common.handler.POSHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

@StackTracePointCut(caller = SvrConst.SVR_POS_GATEWAY)
public class PosGatewayProcessor extends SimpleNettyServerProcessor<String, String> {

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
        if (state == SimpleNettyConnState.CLIENT_CONNECTED) {
            this.logContext.setRemark(StringUtils.join("POS Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Connected"));
            this.logContext.setMessage(StringUtils.EMPTY);
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logContext.setProgramException(null);
            logMessage(this.logContext);
        } else if (state == SimpleNettyConnState.CLIENT_DISCONNECTED) {
            this.logContext.setRemark(StringUtils.join("POS Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Disconnected"));
            this.logContext.setMessage(StringUtils.EMPTY);
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayOut);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logContext.setProgramException(null);
            sendEMS(this.logContext);
        }
    }

    /**
     * 程式或者服務名稱
     *
     * @return
     */
    @Override
    public String getName() {
        return SvrConst.SVR_POS_GATEWAY;
    }

    /**
     * 處理進來的電文並回應
     *
     * @param ctx
     * @param message
     * @return
     * @throws Exception
     */
    @Override
    public String processRequestData(ChannelHandlerContext ctx, String message) throws Exception {
        this.logContext.clear();
        // logging
        this.logContext.setSubSys(SubSystem.GW);
        this.logContext.setChannel(FEPChannel.POS);
        this.logContext.setMessageFlowType(MessageFlow.Request);
        this.logContext.setProgramFlowType(ProgramFlow.PosGateWayIn);
        this.logContext.setMessage(message);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        this.logContext.setRemark(StringUtils.join(this.getName(), " received message from POS Client"));
        this.logMessage(this.logContext);
        String response = null;
        try {
            // logging
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.POS);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramFlowType(ProgramFlow.PosGateWayIn);
            this.logContext.setMessage(message);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            this.logContext.setRemark(StringUtils.join(this.getName(), " start to call POSHandler"));
            this.logMessage(this.logContext);
            // call PosHandler
            POSHandler handler = new POSHandler();
            response = handler.dispatch(FEPChannel.POS, message);
            // logging
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.POS);
            this.logContext.setMessageFlowType(MessageFlow.Response);
            this.logContext.setProgramFlowType(ProgramFlow.PosGateWayOut);
            this.logContext.setMessage(response);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            this.logContext.setRemark(StringUtils.join(this.getName(), " got response from POSHandler"));
            this.logMessage(this.logContext);
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            // logging
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.POS);
            this.logContext.setMessageFlowType(MessageFlow.Response);
            this.logContext.setProgramFlowType(ProgramFlow.PosGateWayOut);
            this.logContext.setMessage(response);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            this.logContext.setRemark(StringUtils.join(this.getName(), " response message to POS Client"));
            this.logMessage(this.logContext);
        }
        return response;
    }
}
