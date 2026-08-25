package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;

public class FEDIAdapter extends AdapterBase {
	@SuppressWarnings("unused")
	private MessageBase txData;
	private String messageFromFedi;
	private String messageToFedi;

	public FEDIAdapter(MessageBase txData) {
		this.txData = txData;
	}

	@Override
	public FEPReturnCode sendReceive() {
		return null;
	}

	public FEPReturnCode sendReceive(String serverURL) {
		return null;
	}

	public String getMessageFromFedi() {
		return messageFromFedi;
	}

	public void setMessageFromFedi(String messageFromFedi) {
		this.messageFromFedi = messageFromFedi;
	}

	public String getMessageToFedi() {
		return messageToFedi;
	}

	public void setMessageToFedi(String messageToFedi) {
		this.messageToFedi = messageToFedi;
	}
}
