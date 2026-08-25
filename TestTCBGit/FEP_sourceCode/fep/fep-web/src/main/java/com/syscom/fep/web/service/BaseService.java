package com.syscom.fep.web.service;

import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.web.base.FEPWebBase;

import java.util.Arrays;
import java.util.Optional;

public class BaseService extends FEPWebBase {

    protected FEPChannel getFEPChannel() {
        return FEPChannel.FEP;
    }

    protected SubSystem getSubSystem() {
        return SubSystem.FEPMonitor;
    }

    protected String getInnerMessage(Throwable t) {
        return t.getMessage();
    }

    protected void sendEMS(Object... msg) {
        sendEMS(Level.ERROR, null, msg);
    }

    protected void sendEMS(Throwable t, Object... msg) {
        sendEMS(Level.ERROR, t, msg);
    }

    protected void sendEMS(Level level, Throwable t, Object... msg) {
        LogData logData = new LogData();
        logData.setChannel(this.getFEPChannel());
        logData.setSubSys(this.getSubSystem());
        if (t != null) {
            logData.setProgramException(t);
        }
        if (ArrayUtils.isNotEmpty(msg)) {
            logData.setMessage(StringUtils.join(msg));
        }
        StackTraceElement[] stackTraceElements = t != null ? t.getStackTrace() : null;
        if (ArrayUtils.isNotEmpty(stackTraceElements)) {
            Optional<String> StackTraceElementClassName = Arrays.stream(stackTraceElements)
                    .map(StackTraceElement::getClassName)
                    .filter(name -> name.toLowerCase().contains("controller"))
                    .findFirst();
            String className = StackTraceElementClassName.orElse("");
            int lastDotIndex = className.lastIndexOf(".");
            if (lastDotIndex != -1 && lastDotIndex < className.length() - 1) {
                className = className.substring(lastDotIndex + 1);
            }
            logData.setProgramName(className);
            logData.setTxUser(WebUtil.getUser().getUserId());
        }
        FEPBase.sendEMS(logData);
    }
}