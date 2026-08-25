package com.syscom.fep.web.form.inbk;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;

public class UI_013114_Form extends BaseForm {

	private static final long serialVersionUID = 1L;
	private String rbtn_ATMNo;
	private String aTMNoTxt;
	private String aTMWorkStatusDdl;
	private String aTMServiceStatusDdl;
	
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


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessage(MessageType messageType, String message) {
        this.setMessageType(messageType);
        this.setMessage(message);
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getRbtn_ATMNo() {
		return rbtn_ATMNo;
	}

	public String getaTMNoTxt() {
		return aTMNoTxt;
	}

	public String getaTMWorkStatusDdl() {
		return aTMWorkStatusDdl;
	}

	public String getaTMServiceStatusDdl() {
		return aTMServiceStatusDdl;
	}

	public void setRbtn_ATMNo(String rbtn_ATMNo) {
		this.rbtn_ATMNo = rbtn_ATMNo;
	}

	public void setaTMNoTxt(String aTMNoTxt) {
		this.aTMNoTxt = aTMNoTxt;
	}

	public void setaTMWorkStatusDdl(String aTMWorkStatusDdl) {
		this.aTMWorkStatusDdl = aTMWorkStatusDdl;
	}

	public void setaTMServiceStatusDdl(String aTMServiceStatusDdl) {
		this.aTMServiceStatusDdl = aTMServiceStatusDdl;
	}
    
    
}
