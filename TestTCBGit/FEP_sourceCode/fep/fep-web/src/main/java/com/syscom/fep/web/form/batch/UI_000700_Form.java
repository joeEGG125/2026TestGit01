package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000700_Form extends BaseForm {
    /**
     * 批次執行日期
     */
    private String batchExecuteDate;
    /**
     * 批次簡稱
     */
    private String batchName;

    public String getBatchExecuteDate() {
        return batchExecuteDate;
    }

    public void setBatchExecuteDate(String batchExecuteDate) {
        this.batchExecuteDate = batchExecuteDate;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }
}
