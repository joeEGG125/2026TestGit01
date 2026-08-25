package com.syscom.fep.gateway.netty.fisc.ctrl;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayCmdAction;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayManager;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayMode;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayRespType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 透過Restful接收訊息下指令給FISCGateway
 *
 * @author Richard
 */
public class FISCGatewayClientRestfulCtrl extends FISCGatewayClientCtrl {
    /**
     * 獲取GW的Monitor資料
     * <p>
     * curl -d "action=get" -X POST http://localhost:8301/recv/fisc/monitor
     *
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/monitor", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageMonitor(@RequestParam(value = "action", required = false, defaultValue = StringUtils.EMPTY) String action) {
        putMDC();
        switch (action) {
            case "get":
                return this.getMonitorData();
            default:
                return this.onMessageMonitor("get");
        }
    }

    /**
     * 操控GW
     * <p>
     * curl -d "mode=primary&action=start" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=primary&action=stop" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=secondary&action=start" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "mode=secondary&action=stop" -X POST http://localhost:8301/recv/fisc/operate
     * <p>
     * curl -d "action=check" -X POST http://localhost:8301/recv/fisc/operate
     *
     * @param mode
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/operate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageOperateGateway(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "mode", required = false, defaultValue = "all") FISCGatewayMode mode,
            @RequestParam(value = "action") FISCGatewayCmdAction action,
            @RequestParam(value = "respType", required = false) FISCGatewayRespType respType) {
        putMDC();
        switch (action) {
            case start:
            case stop:
                return this.doOperateGateway(mode, action, respType);
            case check:
                return this.checkStatus();
            default:
                return StringUtils.join("Incorrect parameter action = \"", action, "\"");
        }
    }

    /**
     * 停止FISCGW
     * <p>
     * curl -X POST http://localhost:8301/recv/fisc/terminate
     *
     * @return
     */
    @RequestMapping(value = "/recv/fisc/terminate", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageTerminate(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.clear();
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageTerminate"));
        logData.setRemark(StringUtils.join("Stop FISCGateway, operator:", operator));
        this.logMessage(logData);
        // 延時終止程序, 確保有回應OK
        Executors.newSingleThreadScheduledExecutor().schedule(() -> System.exit(0), 5, TimeUnit.SECONDS);
        return Const.REPLY_OK;
    }

    /**
     * 指定需要將電文打到那台FISC Service
     * <p>
     * curl -d "host=127.0.0.1" -X POST http://localhost:8301/recv/fisc/changefepap
     *
     * @param host
     * @return
     */
    @RequestMapping(value = "/recv/fisc/changefepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChangeFEPAP(@RequestParam(value = "host", required = false, defaultValue = StringUtils.EMPTY) String host) {
        putMDC();
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
        if (manager == null) {
            return "Cannot Change";
        }
        return manager.setToFEPAPHost(host);
    }

    /**
     * 取得當前將電文打到哪台FISC Service
     * <p>
     * curl -X POST http://localhost:8301/recv/fisc/checkfepap
     *
     * @return
     */
    @RequestMapping(value = "/recv/fisc/checkfepap", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageCheckFEPAP() {
        putMDC();
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
        if (manager == null) {
            return "Cannot Check";
        }
        return manager.getToFEPAPHost();
    }
}
