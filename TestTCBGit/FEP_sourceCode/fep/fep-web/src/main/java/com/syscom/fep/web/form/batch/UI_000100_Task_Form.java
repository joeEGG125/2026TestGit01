package com.syscom.fep.web.form.batch;

import com.syscom.fep.mybatis.vo.JobsContinueOnFail;
import com.syscom.fep.web.resp.BaseResp;

import java.util.ArrayList;
import java.util.HashMap;

public class UI_000100_Task_Form extends BaseResp<ArrayList<HashMap<String, Object>>> {
    private static final long serialVersionUID = 1L;

    private Integer sender;

    private int tskId;

    private int jobId;

    private String jobsSeq;

    private String jobsJobID;

    private String taskID;

    private String jobsContinueOnFail = Integer.toString(JobsContinueOnFail.Interrupt.ordinal());

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }

    public int getTskId() {
        return tskId;
    }

    public void setTskId(int tskId) {
        this.tskId = tskId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobsSeq() {
        return jobsSeq;
    }

    public void setJobsSeq(String jobsSeq) {
        this.jobsSeq = jobsSeq;
    }

    public String getJobsJobID() {
        return jobsJobID;
    }

    public void setJobsJobID(String jobsJobID) {
        this.jobsJobID = jobsJobID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getJobsContinueOnFail() {
        return jobsContinueOnFail;
    }

    public void setJobsContinueOnFail(String jobsContinueOnFail) {
        this.jobsContinueOnFail = jobsContinueOnFail;
    }
}
