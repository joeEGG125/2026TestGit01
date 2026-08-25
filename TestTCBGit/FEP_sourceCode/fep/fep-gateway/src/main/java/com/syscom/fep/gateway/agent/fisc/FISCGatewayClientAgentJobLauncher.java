package com.syscom.fep.gateway.agent.fisc;

import com.syscom.fep.gateway.agent.fisc.job.FISCGatewayClientAgentProcessCommandJob;
import com.syscom.fep.gateway.agent.fisc.job.FISCGatewayClientAgentProcessCommandJobConfig;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.scheduler.job.SchedulerJobManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.util.List;

public class FISCGatewayClientAgentJobLauncher {
    @Autowired
    private SchedulerJobManager manager;
    @Autowired
    private FISCGatewayClientAgentConfiguration configuration;

    @PostConstruct
    public void launch() {
        List<FISCGatewayClientAgentProcessCommandJobConfig> jobConfigs = configuration.getCmdJobConfig();
        if (CollectionUtils.isNotEmpty(jobConfigs)) {
            int index = 0;
            for (FISCGatewayClientAgentProcessCommandJobConfig jobConfig : jobConfigs) {
                jobConfig.setIdentity(StringUtils.join(Gateway.FISCGW, "AgentProcessCommandJob[", index++, "]"));
                if (!jobConfig.isValid()) continue;
                FISCGatewayClientAgentProcessCommandJob job = new FISCGatewayClientAgentProcessCommandJob();
                job.setConfig(jobConfig);
                manager.scheduleJob(job);
            }
        }
    }
}
