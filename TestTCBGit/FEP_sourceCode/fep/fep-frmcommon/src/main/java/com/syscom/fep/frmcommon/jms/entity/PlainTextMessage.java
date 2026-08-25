package com.syscom.fep.frmcommon.jms.entity;

import com.syscom.fep.frmcommon.jms.JmsKind;
import com.syscom.fep.frmcommon.jms.JmsPayload;

/**
 * 一般字串類的Message
 * 
 * @author Richard
 *
 */
public class PlainTextMessage extends JmsPayload<String> {
	private static final long serialVersionUID = 4948910506504089198L;

	public PlainTextMessage() {
		super();
	}

	public PlainTextMessage(JmsKind kind, String destination, String payload) {
		super(kind, destination, payload);
	}
}
