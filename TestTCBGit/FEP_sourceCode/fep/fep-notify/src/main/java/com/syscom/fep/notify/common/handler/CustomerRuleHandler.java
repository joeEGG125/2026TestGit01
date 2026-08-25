package com.syscom.fep.notify.common.handler;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.CustomerRuleBase;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerRuleHandler extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    public boolean proccess(LogData logData, String clazz, NotifyRequestContent templateParmVars) throws Exception {
        try {
            Class<?> clz = Class.forName(clazz);
            CustomerRuleBase customerRule = (CustomerRuleBase) SpringBeanFactoryUtil.getBean(clz);
            // logger.info("CustomerRuleHandler start to envoke ", customerRule.getClass().getSimpleName(), ".proccess()...");
            logData.setRemark(StringUtils.join("CustomerRuleHandler start to envoke ", customerRule.getClass().getSimpleName(), ".proccess()..."));
            logData.setProgramName(StringUtils.join(ProgramName, ".process"));
            logMessage(logData);
            boolean result = customerRule.process(templateParmVars);
            logData.setRemark("CustomerRuleHandler--Finish process.");
            logData.setProgramName(StringUtils.join(ProgramName, ".process"));
            logMessage(logData);
            return result;
        } catch (Exception e) {
            // logger.exceptionMsg(e, clazz + "run failed.");
            throw e;
        }
    }
}
