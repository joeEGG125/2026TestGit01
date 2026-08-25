package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.PostConstruct;

/**
 * 用來接收另一台FISCGW送來的KeepAlive訊息並回應
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public class FISCGatewayKeepAliveRestfulServer extends FEPBase {
    @Autowired
    private FISCGatewayKeepAliveServerConfiguration configuration;

    @PostConstruct
    public void init() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
        logContext.setChannel(FEPChannel.FISC);
        logContext.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logContext.setMessageFlowType(MessageFlow.Request);
        logContext.setProgramName(StringUtils.join(ProgramName, ".init"));
        logContext.setRemark("KeepAlive Restful Server initialize");
        this.logMessage(logContext);
    }

    /**
     * 可以使用如下指令啟動FISCGW
     * <p>
     * curl -d "message=HELLO" -X POST http://localhost:8301/recv/keepalive
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/recv/keepalive", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String sendReceiveKeepAlive(@RequestParam(value = "message") String message) {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
        if (this.configuration.isLogging()) {
            logContext.clear();
            logContext.setChannel(FEPChannel.FISC);
            logContext.setProgramFlowType(ProgramFlow.FISCGatewayIn);
            logContext.setMessageFlowType(MessageFlow.Request);
            logContext.setProgramName(StringUtils.join(ProgramName, ".sendReceiveKeepAlive"));
            logContext.setMessage(message);
            logContext.setRemark("Receive KeepAlive request message");
            logMessage(Level.DEBUG, logContext);
        }
        String response = "Unrecognizable Message";
        // Client進來的KeepAlive Req電文: HELLO
        // 並回應KeepAlive Res電文: HELLOOK
        if (GatewayCodeConstant.FISCGWKeepAliveRequest.equals(message)) {
            response = GatewayCodeConstant.FISCGWKeepAliveResponse;
            if (this.configuration.isLogging()) {
                logContext.setChannel(FEPChannel.FISC);
                logContext.setProgramFlowType(ProgramFlow.FISCGatewayOut);
                logContext.setMessageFlowType(MessageFlow.Response);
                logContext.setProgramName(StringUtils.join(ProgramName, ".sendReceiveKeepAlive"));
                logContext.setMessage(message);
                logContext.setRemark("Response KeepAlive ack message");
                logMessage(Level.DEBUG, logContext);
            }
        } else {
            if (this.configuration.isLogging()) {
                logContext.setChannel(FEPChannel.FISC);
                logContext.setProgramFlowType(ProgramFlow.FISCGatewayOut);
                logContext.setMessageFlowType(MessageFlow.Response);
                logContext.setProgramName(StringUtils.join(ProgramName, ".sendReceiveKeepAlive"));
                logContext.setMessage(message);
                logContext.setRemark("Unrecognizable Message");
                logMessage(Level.WARN, logContext);
            }
        }
        return response;
    }
}
