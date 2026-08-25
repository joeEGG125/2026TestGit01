package com.syscom.fep.web.form.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.vo.JobsContinueOnFail;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.batch.BatchNotify;
import com.syscom.fep.web.form.BaseForm;

public class UI_000100_Detail_Form extends BaseForm {
    private static final long serialVersionUID = 1L;

    private Batch batch;

    private List<HashMap<String, String>> mList;

    private List<HashMap<String, String>> mdList;

    private List<HashMap<String, String>> startGroup;

    private List<HashMap<String, Object>> tasks;

    private List<Subsys> subsys;

    private List<Task> taskList;

    private String date;

    private String time;

    private String radioType;

    private String detail;

    /**
     * 訊息類別
     */
    private MessageType messageType;

    /**
     * 訊息內容
     */
    private String message = "";
    /**
     * 失敗時決定是否要繼續執行Job
     */
    private final List<Map<String, String>> jobsContinueOnFails = new ArrayList<>();
    /**
     * notify mail & phone
     */
    private BatchNotify notify;

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public List<HashMap<String, String>> getmList() {
        return mList;
    }

    public void setmList(List<HashMap<String, String>> mList) {
        this.mList = mList;
    }

    public List<HashMap<String, String>> getMdList() {
        return mdList;
    }

    public void setMdList(List<HashMap<String, String>> mdList) {
        this.mdList = mdList;
    }

    public List<HashMap<String, String>> getStartGroup() {
        return startGroup;
    }

    public void setStartGroup(List<HashMap<String, String>> startGroup) {
        this.startGroup = startGroup;
    }

    public List<Subsys> getSubsys() {
        return subsys;
    }

    public void setSubsys(List<Subsys> subsys) {
        this.subsys = subsys;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<HashMap<String, Object>> getTasks() {
        return tasks;
    }

    public void setTasks(List<HashMap<String, Object>> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void setMessage(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public List<Map<String, String>> getJobsContinueOnFails() {
        if (jobsContinueOnFails.isEmpty()) {
            for (JobsContinueOnFail value : JobsContinueOnFail.values()) {
                jobsContinueOnFails.add(new HashMap<String, String>() {{
                    put("name", value.name());
                    put("ordinal", Integer.toString(value.ordinal()));
                    put("description", value.getDescription());
                }});
            }
        }
        return jobsContinueOnFails;
    }

    public BatchNotify getNotify() {
        return notify;
    }

    public void setNotify(BatchNotify notify) {
        this.notify = notify;
    }
}
