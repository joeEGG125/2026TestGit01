package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000100_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private String batchName;

    private String batchId;
    private String sourceHost;
    private String targetHost;

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSourceHost() {return sourceHost;}

    public void setSourceHost(String sourceHost) {this.sourceHost = sourceHost;}

    public String getTargetHost() {return targetHost;}

    public void setTargetHost(String targetHost) {this.targetHost = targetHost;}
}
