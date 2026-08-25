package com.syscom.fep.jms.entity;

import com.syscom.fep.frmcommon.jms.JmsKind;
import com.syscom.fep.frmcommon.jms.JmsPayload;

import java.io.Serializable;

/**
 * 參考.NET創建的Message
 * 
 * @author Richard
 *
 * @param <T>
 */
public class MsMessage<T extends Serializable> extends JmsPayload<T> {
	private static final long serialVersionUID = 2650906703242448082L;

	private String label;

	private boolean recoverable;

	public MsMessage() {}

	public MsMessage(JmsKind kind, String destination, T payload) {
		super(kind, destination, payload);
	}

	public MsMessage(String label, T body) {
		this.label = label;
		super.setPayload(body);
	}

	public MsMessage(String label, T body, boolean recoverable) {
		this(label, body);
		this.recoverable = recoverable;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public T getBody() {
		return super.getPayload();
	}

	public void setBody(T body) {
		super.setPayload(body);
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}
}
