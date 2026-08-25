package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.entity.InitKeyStatus;
import com.syscom.fep.gateway.entity.SocketStatus;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServerManager;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.vo.communication.ToATMCommuAtmmstr;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ATMGatewayServerProcessRequestManager extends NettyTransmissionChannelProcessRequestServerManager<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequest> {
    private final Map<String, ATMGatewayServerProcessRequest> atmNoToProcessRequestMap = new ConcurrentHashMap<>();

    public void addATMGatewayServerProcessRequest(ChannelHandlerContext ctx, String atmNo, ATMGatewayServerProcessRequest processRequest) {
        Channel channel = ctx.channel();
        ATMGatewayServerProcessRequest p = this.atmNoToProcessRequestMap.get(atmNo);
        if (p == null) {
            this.atmNoToProcessRequestMap.put(atmNo, processRequest);
            NettyTransmissionUtil.infoMessage(channel, "Add addATMGatewayServerProcessRequest to manager succeed, atmNo = [", atmNo, "], processRequest = [", processRequest, "]");
        } else {
            NettyTransmissionUtil.warnMessage(channel, "ATMGatewayServerProcessRequest exist in manager, atmNo = [", atmNo, "], processRequest = [", p, "]");
        }
    }

    public ATMGatewayServerProcessRequest removeATMGatewayServerProcessRequest(ChannelHandlerContext ctx, String atmNo) {
        ATMGatewayServerProcessRequest processRequest = this.atmNoToProcessRequestMap.remove(atmNo);
        if (processRequest != null) {
            NettyTransmissionUtil.infoMessage(ctx.channel(), "Remove ATMGatewayServerProcessRequest from manager succeed, atmNo = [", atmNo, "], processRequest = [", processRequest, "]");
        } else {
            NettyTransmissionUtil.warnMessage(ctx.channel(), "Already remove ATMGatewayServerProcessRequest from manager, atmNo = [", atmNo, "], cannot remove again!!");
        }
        return processRequest;
    }

    public ATMGatewayServerProcessRequest getATMGatewayServerProcessRequest(String atmNo) {
        return this.atmNoToProcessRequestMap.get(atmNo);
    }

    public void checkAtmmstr(ChannelHandlerContext ctx, LogData logData, String remoteIp, int remotePort, boolean byPassIP, ToATMCommuAtmmstr atmmstr) throws Exception {
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".accept"));
        logData.setRemark(StringUtils.join("ATM IP:", remoteIp, ", ATM PORT:", remotePort, " 檢查ATMMSTR主檔", NettyTransmissionUtil.getDateTimeStr()));
        logMessage(logData);
        if (atmmstr == null) {
            throw ExceptionUtil.createException("ATM IP:", remoteIp, " 不存在於ATMMSTR主檔中");
        } else if (StringUtils.isNotBlank(atmmstr.getErrmsg())) {
            throw ExceptionUtil.createException("ATM IP:", remoteIp, ", ", atmmstr.getErrmsg());
        }
        // 有此筆ATM主檔,先記連線成功的LOG
        String atmNo = atmmstr.getAtmAtmno();
        if (StringUtils.isBlank(atmNo)) {
            throw ExceptionUtil.createException("ATM IP:", remoteIp, " 存在於ATMMSTR主檔中, 但是ATMNO為空白");
        }
        SocketStatus socketStatus = SocketStatus.parse(atmmstr.getAtmstatSocket());
        if (socketStatus == SocketStatus.Close) {
            throw ExceptionUtil.createException("ATM No:", atmNo, " Socket狀態已關閉,請從監控重新Reset此台ATM");
        }
        short atmFepConnection = atmmstr.getAtmFepConnection();
        if (atmFepConnection != 1) {
            throw ExceptionUtil.createException("ATM No:", atmNo, " 不允許連線");
        }
        String atmZone = atmmstr.getAtmZone();
        boolean needCheckMac = atmmstr.isAtmCheckMac();
        short desCheckErrorCount = atmmstr.getAtmstatSec();
        InitKeyStatus initKey = InitKeyStatus.parse(atmmstr.getAtmstatInikey());
        String atmIp = atmmstr.getAtmIp();
        if (!atmIp.equals(remoteIp)) {
            LogHelperFactory.getGeneralLogger().warn("Different IP in db = [", atmIp, "], and remoteIp = [", remoteIp, "] for atmNo = [", atmNo, "]");
        }
        String certAlias = atmmstr.getAtmCertAlias();
        logData.setChannel(FEPChannel.ATM);
        logData.setAtmNo(atmNo);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join(
                "ATMGW ATM(",
                "NO:", atmNo,
                ", IP:", remoteIp,
                ", Port:", remotePort,
                ", NeedCheckMac:", needCheckMac,
                ", SocketStatus:", socketStatus,
                ", DESErrorCount:", desCheckErrorCount,
                ", InitKey:", initKey,
                ", CertAlias:", certAlias,
                ") Connect OK!"));
        logMessage(logData);
        // 這裡check通過後, 就增加到manager中
        ATMGatewayServerProcessRequest channelProcessRequest = this.addNettyChannelProcessRequest(ctx);
        channelProcessRequest.setAtmNo(atmNo);
        channelProcessRequest.setAtmZone(atmZone);
        channelProcessRequest.setByPassIP(byPassIP);
        channelProcessRequest.setClientIP(remoteIp);
        channelProcessRequest.setClientPort(remotePort);
        channelProcessRequest.setInitKey(initKey);
        channelProcessRequest.setNeedCheckMac(needCheckMac);
        channelProcessRequest.setSocketStatus(socketStatus);
        channelProcessRequest.setNeedUpdateAtmstat(true);
        channelProcessRequest.setEncCheckErrorCount(desCheckErrorCount);
        channelProcessRequest.setCertAlias(certAlias);
        channelProcessRequest.setAtmFepConnection(atmFepConnection);
        ATMGatewayServerProcessRequest processRequest = this.getATMGatewayServerProcessRequest(atmNo);
        if (processRequest != null && !configuration.isDuplicateClientIp()) {
            logData.setRemark(StringUtils.join("ATMNo ", atmNo, " 重新連線!Port:", remotePort));
            logMessage(logData);
            processRequest.closeConnection();
            this.removeATMGatewayServerProcessRequest(ctx, atmNo);
        }
        this.addATMGatewayServerProcessRequest(ctx, atmNo, channelProcessRequest);
        channelProcessRequest.setAtmmstr(atmmstr); // 2026-03-24 Richard add 這裡塞入ATM主檔資料, 方便壓測模式下歸還
        channelProcessRequest.updateAtmmstr(); // 更新ATM主檔ATMPIP及ATMPPort
        channelProcessRequest.updateAtmstat(AtmStatus.Connected); // 更新ATM狀態檔ATM已連線
    }
}