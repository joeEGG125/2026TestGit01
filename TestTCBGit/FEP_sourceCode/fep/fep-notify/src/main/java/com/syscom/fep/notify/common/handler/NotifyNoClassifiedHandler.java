package com.syscom.fep.notify.common.handler;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.NotClassified001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotifyNoClassifiedHandler extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotClassified001Config notClassified001Config;

    public void send(LogData logData, Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(notClassified001Config.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            // logger.info("NotifyNoClassifiedHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            logData.setRemark(StringUtils.join("NotifyNoClassifiedHandler--start to envoke ", sender.getClass().getSimpleName(), ".send()..."));
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            sender.send(logData, content);
            logData.setRemark("NotifyNoClassifiedHandler--Finish sender.send().");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // e.printStackTrace()
            // logger.exceptionMsg(e,  notClassified001Config.getClassname() + " - send Exception=" + e.getMessage());
            throw e;
        }
    }

}
