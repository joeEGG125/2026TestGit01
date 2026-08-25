package com.syscom.fep.web.resp;

import java.io.Serializable;

import com.syscom.fep.web.entity.MessageType;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BaseResp<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;

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
	private String message;
	/**
	 * 承載的資料
	 */
	private T data;
	/**
	 * Ajax執行錯誤
	 */
	private boolean ajaxErr = false;
	/**
	 * 是否進行頁面跳轉的動作
	 */
	private boolean isRedirect = false;

	public void setMessage(MessageType messageType, String message) {
		this.setMessageType(messageType);
		this.setMessage(message);
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
		if (messageType == null) {
			messageType = MessageType.INFO;
		}
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isAjaxErr() {
		return ajaxErr;
	}

	public void setAjaxErr(boolean ajaxErr) {
		this.ajaxErr = ajaxErr;
	}

	public boolean isRedirect() {
		return isRedirect;
	}

	public void setRedirect(boolean redirect) {
		isRedirect = redirect;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
