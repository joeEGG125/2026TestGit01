package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000120_Form extends BaseForm {
    private String reRun;
    private String batchId;
    private String batchStartJobId;
    private String batchCurrentId;
    private boolean autoRefresh;

    public String getBatchId() { return batchId; }

    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getBatchStartJobId() { return batchStartJobId; }

    public void setBatchStartJobId(String batchStartJobId) { this.batchStartJobId = batchStartJobId; }

    public String getBatchCurrentId() { return batchCurrentId; }

    public void setBatchCurrentId(String batchCurrentId) { this.batchCurrentId = batchCurrentId; }

    public String getReRun() { return reRun; }

    public void setReRun(String reRun) { this.reRun = reRun; }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
}
