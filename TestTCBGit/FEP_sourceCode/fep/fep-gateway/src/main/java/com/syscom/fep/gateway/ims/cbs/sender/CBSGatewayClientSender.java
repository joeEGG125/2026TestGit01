package com.syscom.fep.gateway.ims.cbs.sender;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.gateway.ims.IMSTransmission;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

@StackTracePointCut(caller = SvrConst.SVR_CBS_GATEWAY)
public class CBSGatewayClientSender extends IMSTransmission<CBSGatewayClientSenderConfiguration, CBSGatewayClientSenderProcessRequest> {
    /**
     * 初始化
     */
    @Override
    protected void initialization() {
        this.logData.setSubSys(SubSystem.INBK);
        this.logData.setChannel(FEPChannel.CBS);
        this.logData.setProgramName(StringUtils.join(ProgramName, ".initialization"));
        this.logData.setRemark(StringUtils.join("Begin Service, ", this.configuration.forLogging()));
        FEPBase.logMessage(Level.INFO, this.logData);
    }
}
