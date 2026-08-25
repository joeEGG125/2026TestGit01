package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;
import org.apache.commons.lang.StringUtils;

public class UI_000110_Form extends BaseForm {
    private String batchName = StringUtils.EMPTY;
    private String batchId;
    private String batchStartJobId;
    private String batchCurrentId;
    private boolean autoRefresh;

    public String getBatchCurrentId() { return batchCurrentId; }

    public void setBatchCurrentId(String batchCurrentId) { this.batchCurrentId = batchCurrentId; }

    public String getBatchStartJobId() { return batchStartJobId; }

    public void setBatchStartJobId(String batchStartJobId) { this.batchStartJobId = batchStartJobId; }

    public String getBatchName() { return batchName; }

    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getBatchId() { return batchId; }

    public void setBatchId(String batchId) { this.batchId = batchId; }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
}
