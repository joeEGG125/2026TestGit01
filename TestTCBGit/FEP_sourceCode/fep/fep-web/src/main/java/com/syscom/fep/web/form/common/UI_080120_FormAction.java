package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;

public class UI_080120_FormAction extends BaseForm {
    private String programName;
    private String nThreads;
    private Action action;

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getnThreads() {
        return nThreads;
    }

    public void setnThreads(String nThreads) {
        this.nThreads = nThreads;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public static enum Action {
        GET("查詢"),
        SET("設置"),
        RESET("重置");

        private final String description;

        private Action(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
