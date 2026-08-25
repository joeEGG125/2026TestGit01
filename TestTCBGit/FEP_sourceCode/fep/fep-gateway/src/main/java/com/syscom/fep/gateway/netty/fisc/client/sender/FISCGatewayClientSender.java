package com.syscom.fep.gateway.netty.fisc.client.sender;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClient;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayClientSender
        extends FISCGatewayClient<FISCGatewayClientSenderConfiguration, FISCGatewayClientSenderChannelInboundHandlerAdapter, FISCGatewayClientSenderProcessRequest> {

    @Override
    protected void initData() {
        super.initData();
        this.logContext.setSubSys(SubSystem.INBK);
        this.logContext.setChannel(FEPChannel.FISC);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initData"));
        this.logContext.setRemark("Begin Service.");
        this.logMessage(this.logContext);
    }
}
