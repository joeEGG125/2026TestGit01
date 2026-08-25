package com.syscom.fep.web.entity.batch;

import com.syscom.fep.mybatis.model.Batch;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaintainBatch implements Serializable {
    private Batch batch;
    private String detail = StringUtils.EMPTY;
    private String batchName = StringUtils.EMPTY;
    private final List<MaintainTask> tasks = new ArrayList<>();

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public List<MaintainTask> getTasks() {
        return tasks;
    }

    /**
     * 在新增模式下, 組建UI上所需要的數據
     *
     * @return
     */
    public ArrayList<HashMap<String, Object>> makeGridData() {
        if (this.tasks.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < this.tasks.size(); i++) {
            MaintainTask task = tasks.get(i);
            HashMap<String, Object> map = new HashMap<>();
            map.put("JOBS_JOBID", i);
            map.put("JOBTASK_TASKID", task.getTask().getTaskId());
            map.put("TASK_ID", task.getTask().getTaskId());
            map.put("TASK_NAME", task.getTask().getTaskName());
            map.put("JOBS_SEQ", (i + 1));
            map.put("JOBS_DESCRIPTION", task.getTask().getTaskDescription());
            map.put("JOBS_CONTINUEONFAIL", task.getJobsContinueonfail());
            list.add(map);
        }
        return list;
    }
}
