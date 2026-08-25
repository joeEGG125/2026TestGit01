package com.syscom.fep.server.gateway.ims;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.gateway.ims.processor.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IMS Gateway主程式
 * <p>
 * 2024-04-24 Richard modified 改成Sender&Receiver非同步方式
 *
 * @author Richard & Ashiang
 */
@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGateway extends FEPBase {
    private final String ProgramName;
    private final List<IMSGatewayProcessorGroup> processorGroups = new ArrayList<>();
    private final AtomicReference<IMSGatewayConnState> state = new AtomicReference<>(IMSGatewayConnState.SHUT_DOWN); // 當前的運行狀態
    private final IMSGatewayProcessorGroupConfiguration processorGroupConfiguration;

    /**
     * 初始化並啟動Sender/Receiver
     *
     * @param configuration
     * @param processorGroupConfiguration
     * @param executor
     * @param startToRun
     */
    public IMSGateway(IMSGatewayConfiguration configuration, IMSGatewayProcessorGroupConfiguration processorGroupConfiguration, ExecutorService executor, boolean startToRun) {
        this.processorGroupConfiguration = processorGroupConfiguration;
        this.ProgramName = StringUtils.join(Arrays.asList("IMSGateway", processorGroupConfiguration.getMode().name().toUpperCase()), ".");
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initialize"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter Initialize"));
        this.logMessage(this.logContext);
        String tranCode = StringUtils.EMPTY; // 給空白即可 by Ashiang 2024/01/29
        this.state.set(IMSGatewayConnState.READY_TO_RUN);
        for (int i = 0; i < processorGroupConfiguration.getSenderConfigurations().size(); i++) {
            IMSGatewayProcessorGroup processorGroup = new IMSGatewayProcessorGroup(configuration, processorGroupConfiguration.getSenderConfigurations().get(i), processorGroupConfiguration.getReceiverConfigurations().get(i), tranCode, executor);
            if (startToRun)
                processorGroup.run();
            processorGroups.add(processorGroup);
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initialize"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit Initialize"));
        this.logMessage(this.logContext);
        this.state.set(IMSGatewayConnState.RUNNING);
    }

    public void startToRun() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".startToRun"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter startToRun"));
        this.logMessage(this.logContext);
        for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
            // 這裡要設定enable為true, 可以保證所有的腳位都可以運行
            processorGroup.getReceiverConfiguration().setEnable(true);
            processorGroup.getSenderConfiguration().setEnable(true);
            processorGroup.run();
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".startToRun"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit startToRun"));
        this.logMessage(this.logContext);
    }

    /**
     * 終止Sender/Receiver
     */
    public void terminate() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter Terminate"));
        this.logMessage(this.logContext);
        this.state.set(IMSGatewayConnState.SHUTTING_DOWN);
        for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
            // while (!processorGroups.isEmpty()) {
            // IMSGatewayProcessorGroup processorGroup = processorGroups.remove(0);
            processorGroup.terminate();
            // processorGroup = null;
            // 這裡要設定enable為false
            processorGroup.getReceiverConfiguration().setEnable(false);
            processorGroup.getSenderConfiguration().setEnable(false);
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit Terminate"));
        this.logMessage(this.logContext);
        this.state.set(IMSGatewayConnState.SHUT_DOWN);
    }

    /**
     * 只要有一個連線中則表示有連線
     *
     * @return
     */
    public boolean isConnected() {
        for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
            if (processorGroup.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 全部連線中則表示全部都連線
     *
     * @return
     */
    public boolean isAllConnected() {
        for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
            if (!processorGroup.isAllConnected()) {
                return false;
            }
        }
        return true;
    }

    public IMSGatewayConnState getState() {
        return state.get();
    }

    /**
     * 獲取狀態
     *
     * @return
     */
    public String checkStatus() {
        if (state.get() != IMSGatewayConnState.RUNNING) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(processorGroups)) {
            for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
                // sender
                checkStatus(sb, processorGroup.getSenderConfiguration(), processorGroup.getSender());
                // receiver
                checkStatus(sb, processorGroup.getReceiverConfiguration(), processorGroup.getReceiver());
            }
        }
        return sb.toString();
    }

    /**
     * 獲取狀態
     *
     * @param sb
     * @param processorConfiguration
     * @param processor
     */
    private void checkStatus(StringBuilder sb, IMSGatewayProcessorConfiguration processorConfiguration, IMSGatewayProcessor processor) {
        if (processorConfiguration.isEnable()) {
            sb.append(FEPConfig.getInstance().getHostName()).append(" : ")
                    .append(StringUtils.capitalize(processorConfiguration.getMode().name())).append(StringUtils.SPACE)
                    .append("IMS(").append(processorConfiguration.getClientId()).append(")").append(StringUtils.SPACE)
                    .append(processorConfiguration.getHost()).append(":").append(processorConfiguration.getPort()).append(StringUtils.SPACE)
                    .append(processor != null && IMSGatewayConnState.isConnected(processor.getCurrentConnState()) ? "Connected" : "Disconnected")
                    .append("\r\n");
        }
    }

    public List<IMSGatewayProcessorGroup> getProcessorGroups() {
        return processorGroups;
    }

    public IMSGatewayProcessorGroupConfiguration getProcessorGroupConfiguration() {
        return processorGroupConfiguration;
    }
}
