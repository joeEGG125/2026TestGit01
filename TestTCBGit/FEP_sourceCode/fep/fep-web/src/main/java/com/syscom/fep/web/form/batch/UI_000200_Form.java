package com.syscom.fep.web.form.batch;

import com.syscom.fep.web.form.BaseForm;

public class UI_000200_Form extends BaseForm {
    private String task_Id;
    private String task_Command;
    private String task_Commandargs;
    private String task_Description;
    private String task_Name;
    private String task_Timeout;
    private String btnType;

    public String getTask_Id() { return task_Id; }

    public void setTask_Id(String task_Id) { this.task_Id = task_Id; }

    public String getTask_Command() { return task_Command; }

    public void setTask_Command(String task_Command) { this.task_Command = task_Command; }

    public String getTask_Commandargs() { return task_Commandargs; }

    public void setTask_Commandargs(String task_Commandargs) { this.task_Commandargs = task_Commandargs; }

    public String getTask_Description() { return task_Description; }

    public void setTask_Description(String task_Description) { this.task_Description = task_Description; }

    public String getTask_Timeout() { return task_Timeout; }

    public void setTask_Timeout(String task_Timeout) { this.task_Timeout = task_Timeout; }

    public String getBtnType() { return btnType; }

    public void setBtnType(String btnType) { this.btnType = btnType; }

    public String getTask_Name() { return task_Name; }

    public void setTask_Name(String task_Name) { this.task_Name = task_Name; }
}
