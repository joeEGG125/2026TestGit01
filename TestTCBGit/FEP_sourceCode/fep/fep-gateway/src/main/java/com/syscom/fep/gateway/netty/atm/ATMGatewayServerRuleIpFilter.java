package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.NettyTransmissionServerRuleIpFilter;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.vo.communication.ToATMCommuAtmmstr;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

public class ATMGatewayServerRuleIpFilter extends NettyTransmissionServerRuleIpFilter<ATMGatewayServerConfiguration> {
    // @Autowired
    // private GatewayCommuHelper commuHelper;
    @Autowired
    private ATMGatewayServerProcessRequestManager manager;
    @Autowired
    private GatewayUtil gatewayUtil;

    @Override
    protected boolean accept(ChannelHandlerContext ctx, String remoteIp, int remotePort) {
        // 如果是按照ATMNO來檢核, 則這裡先直接放行
        if (configuration.isCheckAtmmstrByAtmNo()) {
            return true;
        }
        return this.accept(ctx, new LogData(), remoteIp, remotePort);
    }

    public boolean accept(ChannelHandlerContext ctx, LogData logData, String remoteIp, int remotePort) {
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".accept"));
        logData.setRemark(StringUtils.join("開始檢查, BypassCheckATMIP:", this.configuration.getBypassCheckAtmIp(), ", ATM IP:", remoteIp, ", ATM PORT:", remotePort, NettyTransmissionUtil.getDateTimeStr()));
        FEPBase.logMessage(Level.INFO, logData);
        boolean byPassIP = false;
        LogHelperFactory.getTraceLogger().trace("BypassCheckATMIP:", this.configuration.getBypassCheckAtmIp(), ", ATM IP:", remoteIp, ", ATM PORT:", remotePort, NettyTransmissionUtil.getDateTimeStr());
        try {
            // 監控ATMGW的Client不檢查ATM主檔
            if (ATMGatewayServerConfiguration.BYPASSCHECKATMIP_ALL.equalsIgnoreCase(this.configuration.getBypassCheckAtmIp()) || this.configuration.getBypassCheckAtmIp().contains(remoteIp)) {
                byPassIP = true;
                logData.setSubSys(SubSystem.GW);
                logData.setChannel(FEPChannel.ATM);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                logData.setProgramName(StringUtils.join(ProgramName, ".accept"));
                logData.setRemark(StringUtils.join("ATM IP:", remoteIp, ", ATM PORT:", remotePort, " 連線成功但不檢查主檔", NettyTransmissionUtil.getDateTimeStr()));
                FEPBase.logMessage(Level.INFO, logData);
            } else {
                this.checkAtmmStr(logData, ctx, remoteIp, remotePort, byPassIP);
            }
        } catch (Exception e) {
            // 設定這個Channel需要拒絕
            NettyTransmissionUtil.setChannelReject(ctx.channel(), true);
            // 更新失敗次數
            ATMGatewayServerBlackListHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class, false);
            if (handler != null)
                handler.incrementFailedTimes(remoteIp);
            // logging
            NettyTransmissionUtil.errorMessage(ctx.channel(), "Client rejected, ", e.getMessage());
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".accept"));
            FEPBase.sendEMS(logData);
            return false;
        }
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Client accepted", NettyTransmissionUtil.getDateTimeStr());
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".accept"));
        logData.setRemark(StringUtils.join("ATM accepted, ATM IP:", remoteIp, ", ATM PORT:", remotePort, NettyTransmissionUtil.getDateTimeStr()));
        FEPBase.logMessage(Level.INFO, logData);
        return true;
    }

    private void checkAtmmStr(LogData logData, ChannelHandlerContext ctx, String remoteIp, int remotePort, boolean byPassIP) throws Exception {
        ToATMCommuAtmmstr atmmstr;
        // 2026-03-24 Richard modified 壓測模式下透過ATMGatewayServerLoadTestHandler取預先載入的ATM主檔資料
        if (configuration.isLoadTestMode()) {
            atmmstr = ATMGatewayServerLoadTestHandler.fetchATMMSTR(StringUtils.join(remoteIp, ":", remotePort));
        } else {
            // ToATMCommuAtmmstr atmmstr = this.commuHelper.getAtmmstrByAtmIp(logData, remoteIp, gatewayUtil.getTimeout(this.configuration));
            ATMGatewayServerToFEPATM toFEPATM = ATMGatewayServerToFEPATM.getInstance(); // SpringBeanFactoryUtil.getBean(ATMGatewayServerToFEPATM.class);
            atmmstr = toFEPATM.getAtmmstrByAtmIp(logData, remoteIp, gatewayUtil.getTimeout(this.configuration));
        }
        manager.checkAtmmstr(ctx, logData, remoteIp, remotePort, byPassIP, atmmstr);
    }
}
