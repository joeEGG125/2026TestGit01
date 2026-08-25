package com.syscom.fep.service.monitor.controller;

import com.google.gson.Gson;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.service.monitor.vo.MonitorHeartbeat;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public class MonitorHeartbeatController implements MonitorConstant {
    private final LogHelper logger = LogHelperFactory.getServiceLogger();

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    @RequestMapping(value = "/recv/mon/heartbeat", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceiveHeartbeat(@RequestBody MonitorHeartbeat heartbeat) {
        return timeOccupied((args) -> {
            putMDC();
            if (heartbeat != null) {
                logger.info("Receive Heartbeat Data = [", new Gson().toJson(heartbeat), "]");
                String hostName = heartbeat.getFromHostName();
                String hostIp = heartbeat.getFromHostIp();
                String appName = heartbeat.getFromAppName();
                if (StringUtils.isBlank(hostName) || StringUtils.isBlank(hostIp) || StringUtils.isBlank(appName)) {
                    logger.error("Heartbeat skip, host name or ip or app name was empty, hostName = [", hostName, "], hostIp = [", hostIp, "], appName = [", appName, "]");
                    return REPLY_COMPLETE;
                }
                logger.info("Heartbeat response successful, hostName = [", hostName, "], hostIp = [", hostIp, "], appName = [", appName, "]");
                return REPLY_SUCCESS;
            } else {
                logger.warn("Heartbeat數據 EMPTY");
                return REPLY_COMPLETE;
            }
        }, REPLY_EXCEPTION_OCCUR, StringUtils.EMPTY);
    }
}
