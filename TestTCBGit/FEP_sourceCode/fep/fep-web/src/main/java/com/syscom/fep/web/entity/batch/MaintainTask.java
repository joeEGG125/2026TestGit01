package com.syscom.fep.web.entity.batch;

import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.vo.JobsContinueOnFail;

public class MaintainTask {
    private Task task;
    private int jobsContinueonfail = JobsContinueOnFail.Interrupt.ordinal(); // 預設塞入0

    public MaintainTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getJobsContinueonfail() {
        return jobsContinueonfail;
    }

    public void setJobsContinueonfail(int jobsContinueonfail) {
        this.jobsContinueonfail = jobsContinueonfail;
    }
}
