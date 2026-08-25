package com.syscom.fep.scheduler.job.impl;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

public class ProcessCommandJobConfig extends SchedulerJobConfig {
    /**
     * 要執行的命令
     */
    private String command;
    /**
     * 是否列印呼叫Command後執行的內容
     */
    private boolean output = false;
    /**
     * Command執行的內容列印在Console中的編碼
     */
    private String outputCharsetName = StandardCharsets.UTF_8.displayName();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isOutput() {
        return output;
    }

    public void setOutput(boolean output) {
        this.output = output;
    }

    public String getOutputCharsetName() {
        return outputCharsetName;
    }

    public void setOutputCharsetName(String outputCharsetName) {
        this.outputCharsetName = outputCharsetName;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.getCronExpression()) && StringUtils.isNotBlank(this.getIdentity()) && StringUtils.isNotBlank(this.command);
    }
}
