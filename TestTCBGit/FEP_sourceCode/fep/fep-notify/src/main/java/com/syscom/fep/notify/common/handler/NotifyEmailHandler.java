package com.syscom.fep.notify.common.handler;


import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Email001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotifyEmailHandler extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private Email001Config email001Config;

    public void send(LogData logData, Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(email001Config.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            // logger.info("NotifyEmailHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            logData.setRemark(StringUtils.join("NotifyEmailHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()..."));
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            sender.send(logData, content);
            // logger.info("NotifyEmailHandler--Finish sender.send().");
            logData.setRemark("NotifyEmailHandler--Finish sender.send().");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // e.printStackTrace()
            // logger.exceptionMsg(e,  email001Config.getClassname() + " - send Exception =" + e.getMessage());
            throw e;
        }
    }
}
