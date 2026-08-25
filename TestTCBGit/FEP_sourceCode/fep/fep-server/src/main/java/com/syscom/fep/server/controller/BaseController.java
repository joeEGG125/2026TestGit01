package com.syscom.fep.server.controller;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.GWConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.invoker.netty.SimpleNettyServerProcessor;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.mybatis.util.EjfnoGenerator;
import com.syscom.fep.vo.communication.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseController extends SimpleNettyServerProcessor<String, String> {
    @Autowired
    private SysconfExtMapper sysconfMapper;
    @Autowired
    private EjfnoGenerator ejfnoGenerator;

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    protected abstract String processRequestData(ProgramFlow programFlow, String messageIn);

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        //LogData logData = new LogData();
        if (state == SimpleNettyConnState.CLIENT_CONNECTED) {
            // logData.setRemark(StringUtils.join(this.getName(), " Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Connected"));
            // logData.setMessage(StringUtils.EMPTY);
            // logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            // logData.setProgramException(null);
            // logMessage(logData);
            LogHelperFactory.getTraceLogger().trace(StringUtils.join(this.getName(), " Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Connected"));
        } else if (state == SimpleNettyConnState.CLIENT_DISCONNECTED) {
            // logData.setRemark(StringUtils.join(this.getName(), " Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Disconnected"));
            // logData.setMessage(StringUtils.EMPTY);
            // logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            // logData.setProgramException(null);
            // logMessage(Level.WARN, logData);
            LogHelperFactory.getTraceLogger().trace(StringUtils.join(this.getName(), " Client(IP:", this.getClientIP(), ",Port:", this.getClientPort(), ")Disconnected"));
        }
    }

    /**
     * 處理進來的電文並回應
     *
     * @param ctx
     * @param messageIn
     * @return
     * @throws Exception
     */
    @Override
    public final String processRequestData(ChannelHandlerContext ctx, String messageIn) throws Exception {
        // Socket進來的電文
        return processRequestData(ProgramFlow.SocketIn, messageIn);
    }

    /**
     * 根據進來的ProgramFlow決定出去的ProgramFlow是什麼
     *
     * @param programFlowIn
     * @return
     */
    protected ProgramFlow getOutProgramFlow(ProgramFlow programFlowIn) {
        if (programFlowIn == ProgramFlow.SocketIn) {
            return ProgramFlow.SocketOut;
        }
        return ProgramFlow.RESTFulOut;
    }

    /**
     * GW進來的查詢Sysconf資料的電文, 收到的是ToFEPCommuSysconf對應的XML字串, 送出的是ToGWCommuSysconf對應的XML字串
     *
     * @param logData
     * @param toFEPCommuSysconf
     * @return
     */
    protected String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPCommuSysconf toFEPCommuSysconf) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPCommuSysconf);
        ToGWCommuSysconf toGWCommuSysconf = new ToGWCommuSysconf();
        try {
            Sysconf sysconf = sysconfMapper.selectByPrimaryKey(toFEPCommuSysconf.getSysconfSubsysno(), toFEPCommuSysconf.getSysconfName());
            if (sysconf != null) {
                toGWCommuSysconf.setSysconfValue(sysconf.getSysconfValue());
            } else {
                // 查詢不到資料
                toGWCommuSysconf.setErrmsg(
                        StringUtils.join("Cannot selectByPrimaryKey, ",
                                "Subsysno = [", toFEPCommuSysconf.getSysconfSubsysno(), "], ",
                                "SysconfName = [", toFEPCommuSysconf.getSysconfName(), "]"));
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuSysconf, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuSysconf);
        return toGWCommuSysconf.toString();
    }

    /**
     * GW進來的沒有請求參數查詢資料的電文, 收到的是ToFEPCommuAction對應的XML字串, 送出的是ToGWCommuAction對應的XML字串
     *
     * @param logData
     * @param toFEPCommuAction
     * @return
     */
    protected String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPCommuAction toFEPCommuAction) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPCommuAction);
        ToGWCommuAction toGWCommuAction = new ToGWCommuAction();
        try {
            switch (toFEPCommuAction.getAction()) {
                case GENERATE_EJFNO:
                    toGWCommuAction.setEjfno(ejfnoGenerator.generate());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuAction, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuAction);
        return toGWCommuAction.toString();
    }

    /**
     * GW進來查詢Zone資料的電文, 收到的是ToFEPCommuZone對應的XML字串, 送出的是ToGWCommuZone對應的XML字串
     *
     * @param logData
     * @param toFEPCommuZone
     * @return
     */
    protected String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPCommuZone toFEPCommuZone) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPCommuZone);
        ToGWCommuZone toGWCommuZone = new ToGWCommuZone();
        try {
            Zone zone = FEPCache.getZone(toFEPCommuZone.getZoneCode());
            if (zone != null) {
                toGWCommuZone.setZoneCbsMode(zone.getZoneCbsMode());
                toGWCommuZone.setZoneTbsdy(zone.getZoneTbsdy());
            } else {
                // 查詢不到資料
                toGWCommuZone.setErrmsg(StringUtils.join("Cannot get Zone, ZoneCode = [", toFEPCommuZone.getZoneCode(), "]"));
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuZone, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuZone);
        return toGWCommuZone.toString();
    }

    /**
     * GW進來查詢Config資料的電文, 收到的是ToFEPCommuConfig對應的XML字串, 送出的是ToGWCommuConfig對應的XML字串
     *
     * @param logData
     * @param toFEPCommuConfig
     * @return
     */
    protected String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPCommuConfig toFEPCommuConfig) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPCommuConfig);
        ToGWCommuConfig toGWCommuConfig = new ToGWCommuConfig();
        try {
            List<Integer> valueList = MathUtil.splitByPow2(toFEPCommuConfig.getConfigType());
            for (int value : valueList) {
                ToFEPCommuConfig.ConfigType configType = ToFEPCommuConfig.ConfigType.fromValue(value);
                switch (configType) {
                    case CMN:
                        toGWCommuConfig.setCmn(new ToGWCommuConfig.CMN());
                        toGWCommuConfig.getCmn().setKeepAliveInterval(CMNConfig.getInstance().getKeepAliveInterval());
                        toGWCommuConfig.getCmn().setKeepAliveTime(CMNConfig.getInstance().getKeepAliveTime());
                        //toGWCommuConfig.getCmn().setListenBacklog(CMNConfig.getInstance().getListenBacklog());
                        break;
                    case GW:
                        toGWCommuConfig.setGw(new ToGWCommuConfig.GW());
                        toGWCommuConfig.getGw().setAaTimeout(GWConfig.getInstance().getAATimeout());
                        toGWCommuConfig.getGw().setAtmCertNo(GWConfig.getInstance().getATMCertNo());
                        toGWCommuConfig.getGw().setAtmCertNoOld(GWConfig.getInstance().getATMCertNo_Old());
                        break;
                    // case INBK:
                    //     toGWCommuConfig.setInbk(new ToGWCommuConfig.INBK());

                    //    break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuConfig, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuConfig);
        return toGWCommuConfig.toString();
    }

    protected void loggingLogData(ProgramFlow programFlow, MessageFlow messageFlow, LogData logData, BaseCommu baseCommu) {
        // logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
        // logData.setMessageFlowType(messageFlow);
        // logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        // logData.setRemark(StringUtils.join(this.getName(), messageFlow == MessageFlow.Request ? " Receive Request OK" : " Send Response OK"));
        // logData.setMessage(baseCommu.toString());
        // this.logMessage(logData);
        LogHelperFactory.getTraceLogger().trace(StringUtils.join(this.getName(), messageFlow == MessageFlow.Request ? " Receive Request OK" : " Send Response OK"));
    }

    protected void handleException(ProgramFlow programFlow, LogData logData, BaseCommu baseCommu, Exception e) {
        baseCommu.setCode(FEPReturnCode.ProgramException);
        baseCommu.setErrmsg(e.getMessage());
        logData.setProgramException(e);
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setRemark(StringUtils.join(this.getName(), " Get Response with exception occur, ", e.getMessage()));
        logData.setMessage(baseCommu.toString());
        this.logMessage(logData);
        logData.setProgramException(e);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setRemark(e.getMessage());
        logData.setMessage(baseCommu.toString());
        sendEMS(logData);
    }
}