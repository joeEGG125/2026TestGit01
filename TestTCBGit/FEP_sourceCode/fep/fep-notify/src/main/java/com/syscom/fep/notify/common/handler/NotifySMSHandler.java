package com.syscom.fep.notify.common.handler;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Sms001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service

public class NotifySMSHandler extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private Sms001Config smsConfig;

    public void send(LogData logData, Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(smsConfig.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            // logger.info("NotifySMSHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            logData.setRemark(StringUtils.join("NotifySMSHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()..."));
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            sender.send(logData, content);
            // logger.info("NotifySMSHandler--Finish sender.send().");
            logData.setRemark("NotifySMSHandler--Finish sender.send().");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // e.printStackTrace()
            // logger.exceptionMsg(e, smsConfig.getClassname() + " - send Exception =" + e.getMessage());
            throw e;
        }
    }

}
