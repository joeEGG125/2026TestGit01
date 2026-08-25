package com.syscom.fep.web.form.batch;

import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.web.entity.batch.BatchNotify;
import com.syscom.fep.web.form.BaseForm;

import java.util.HashMap;
import java.util.List;

public class UI_000100_Main_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private Batch batch;

    private String radioType;

    private String groupChk;

    private String mdChk;

    private String mChk;

    private String wmChk;

    private String mwChk;

    private String dateTime;

    private int jobId;

    private BatchNotify notify;

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    public String getGroupChk() {
        return groupChk;
    }

    public void setGroupChk(String groupChk) {
        this.groupChk = groupChk;
    }

    public String getMdChk() {
        return mdChk;
    }

    public void setMdChk(String mdChk) {
        this.mdChk = mdChk;
    }

    public String getmChk() {
        return mChk;
    }

    public void setmChk(String mChk) {
        this.mChk = mChk;
    }

    public String getWmChk() {
        return wmChk;
    }

    public void setWmChk(String wmChk) {
        this.wmChk = wmChk;
    }

    public String getMwChk() {
        return mwChk;
    }

    public void setMwChk(String mwChk) {
        this.mwChk = mwChk;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public BatchNotify getNotify() {
        return notify;
    }

    public void setNotify(BatchNotify notify) {
        this.notify = notify;
    }
}
