package com.syscom.fep.gateway.agent.atm;

import com.syscom.fep.gateway.agent.atm.job.ATMGatewayServerAgentProcessCommandJob;
import com.syscom.fep.gateway.agent.atm.job.ATMGatewayServerAgentProcessCommandJobConfig;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.scheduler.job.SchedulerJobManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.util.List;

public class ATMGatewayServerAgentJobLauncher {
    @Autowired
    private SchedulerJobManager manager;
    @Autowired
    private ATMGatewayServerAgentConfiguration configuration;

    @PostConstruct
    public void launch() {
        List<ATMGatewayServerAgentProcessCommandJobConfig> jobConfigs = configuration.getCmdJobConfig();
        if (CollectionUtils.isNotEmpty(jobConfigs)) {
            int index = 0;
            for (ATMGatewayServerAgentProcessCommandJobConfig jobConfig : jobConfigs) {
                jobConfig.setIdentity(StringUtils.join(Gateway.ATMGW, "AgentProcessCommandJob[", index++, "]"));
                if (!jobConfig.isValid()) continue;
                ATMGatewayServerAgentProcessCommandJob job = new ATMGatewayServerAgentProcessCommandJob();
                job.setConfig(jobConfig);
                manager.scheduleJob(job);
            }
        }
    }
}
