package com.syscom.fep.gateway.agent.fisc.job;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.scheduler.job.impl.ProcessCommandJob;
import org.apache.commons.lang3.StringUtils;

public class FISCGatewayClientAgentProcessCommandJob extends ProcessCommandJob<FISCGatewayClientAgentProcessCommandJobConfig> {
    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, StringUtils.join(Gateway.FISCGW.name()));
    }
}
