package com.syscom.fep.notify.controller;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.MDCKeyConst;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.notify.service.NotifyRuleService;
import com.syscom.fep.notify.service.NotifyRuleSetService;
import com.syscom.fep.notify.service.NotifyTemplateService;
import com.syscom.fep.notify.service.SystemVarsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @Autowired
    private NotifyRuleSetService notifyRuleSetService;

    @Autowired
    private NotifyRuleService notifyRuleService;

    @Autowired
    private SystemVarsService systemVarsService;

    @GetMapping("cleanAllCache")
    public void cleanAllCache() {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setServiceUrl("/cleanAllCache");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanAllCache"));
        logMessage(logData);
        notifyTemplateService.cleanNotifyTemplateCache(logData);
        notifyRuleSetService.cleanNotifyRuleSetCache(logData);
        notifyRuleService.cleanNotifyRuleCache(logData);
        systemVarsService.cleanSystemVarsCache();
    }

    @GetMapping("cleanSystemVarsCache")
    public void cleanSystemVarsCache() {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setServiceUrl("/cleanSystemVarsCache");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanSystemVarsCache"));
        logMessage(logData);
        systemVarsService.cleanSystemVarsCache();
    }

    @GetMapping("cleanRuleSetCache")
    public void cleanRuleSetCache() {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setServiceUrl("/cleanRuleSetCache");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanRuleSetCache"));
        logMessage(logData);
        notifyRuleSetService.cleanNotifyRuleSetCache(logData);
    }

    @GetMapping("cleanNoifyTemplateCache")
    public void cleanNoifyTemplateCache() {
        LogMDC.put(MDCKeyConst.MDC_PROFILE, SvrConst.SVR_NOTIFY);
        LogData logData = new LogData();
        logData.setServiceUrl("/cleanNoifyTemplateCache");
        logData.setRemark(StringUtils.join(SvrConst.SVR_NOTIFY, " Receive Request"));
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNoifyTemplateCache"));
        logMessage(logData);
        notifyTemplateService.cleanNotifyTemplateCache(logData);
    }
}