package com.syscom.fep.web.form.common;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_080070_Form extends BaseForm {

    private String logTimeBegin;
    
    private String logTimeBeginTime;
    
    private String logTimeEnd;
    
    private String logTimeEndTime;
    
    private String ddlUser;
    
    private String txtProgram_ID;
    
    private String txtProgram_Name;
    
    private String displayShowAudit;
    
    private String dtBegin;
    
    private String dtEnd;
    
    /**
     * 執行結果
     */
    private boolean result = true;
    /**
     * 訊息類別
     */
    private MessageType messageType;
    /**
     * 訊息內容
     */
    private String message = "";




    public String getLogTimeBegin() {
		return logTimeBegin;
	}

	public void setLogTimeBegin(String logTimeBegin) {
		this.logTimeBegin = logTimeBegin;
	}

	public String getLogTimeBeginTime() {
		return logTimeBeginTime;
	}

	public void setLogTimeBeginTime(String logTimeBeginTime) {
		this.logTimeBeginTime = logTimeBeginTime;
	}

	public String getLogTimeEnd() {
		return logTimeEnd;
	}

	public void setLogTimeEnd(String logTimeEnd) {
		this.logTimeEnd = logTimeEnd;
	}

	public String getLogTimeEndTime() {
		return logTimeEndTime;
	}

	public void setLogTimeEndTime(String logTimeEndTime) {
		this.logTimeEndTime = logTimeEndTime;
	}

	public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        if (messageType == MessageType.DANGER) {
            this.result = false;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
    }

	public String getDdlUser() {
		return ddlUser;
	}

	public void setDdlUser(String ddlUser) {
		this.ddlUser = ddlUser;
	}

	public String getTxtProgram_ID() {
		return txtProgram_ID;
	}

	public void setTxtProgram_ID(String txtProgram_ID) {
		this.txtProgram_ID = txtProgram_ID;
	}

	public String getTxtProgram_Name() {
		return txtProgram_Name;
	}

	public void setTxtProgram_Name(String txtProgram_Name) {
		this.txtProgram_Name = txtProgram_Name;
	}


	public String getDisplayShowAudit() {
		return displayShowAudit;
	}

	public void setDisplayShowAudit(String displayShowAudit) {
		this.displayShowAudit = displayShowAudit;
	}

	public String getDtBegin() {
		return dtBegin;
	}

	public void setDtBegin(String dtBegin) {
		this.dtBegin = dtBegin;
	}

	public String getDtEnd() {
		return dtEnd;
	}

	public void setDtEnd(String dtEnd) {
		this.dtEnd = dtEnd;
	}
}
