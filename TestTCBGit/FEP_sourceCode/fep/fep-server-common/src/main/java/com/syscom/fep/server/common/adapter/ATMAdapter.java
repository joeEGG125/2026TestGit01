package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiverManager;
import org.apache.commons.lang3.StringUtils;

public class ATMAdapter extends AdapterBase {
	private static final String ProgramName = ATMAdapter.class.getSimpleName();
	private MessageBase txData;
	private String atmNo;
	private String messageToATM;
	private String messageFromATM;

	public ATMAdapter(MessageBase txData) {
		this.txData = txData;
	}

	public String getAtmNo() {
		return atmNo;
	}

	public void setAtmNo(String atmNo) {
		this.atmNo = atmNo;
	}

	public String getMessageToATM() {
		return messageToATM;
	}

	public void setMessageToATM(String messageToATM) {
		this.messageToATM = messageToATM;
	}

	public String getMessageFromATM() {
		return messageFromATM;
	}

	public void setMessageFromATM(String messageFromATM) {
		this.messageFromATM = messageFromATM;
	}

	public FEPReturnCode sendReceive() {
		this.messageToATM = !"0F0F0F".equals(this.messageToATM.substring(0, 6)) ? this.addHeader(this.messageToATM) : this.messageToATM;
		this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
		this.txData.getLogContext().setMessage(this.messageToATM);
		this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
		this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
		this.sendMsg(this.txData.getMessageCorrelationID(), this.messageToATM);
		this.logMessage(this.txData.getLogContext());
		return FEPReturnCode.Normal;
	}

	private String addHeader(String data) {
		StringBuilder sb = new StringBuilder();
		// 第0 – 2 BYTE ==Hex(0F),Hex(0F),Hex(0F)
		sb.append("0F0F0F");
		// 第3 – 5 BYTE長度
		sb.append(StringUtils.leftPad(String.valueOf(data.length() / 2 + 12), 6, '0'));
		// 第6 BYTE 本筆資料中共含有幾筆TITA/TOTA,固定塞01
		sb.append("01");
		// 第7 – 9 BYTE XMTNO 從零起編
		sb.append("303031");
		// 第10 BYTE資料型態,0F 為DATA TITA,TOTA
		sb.append("0F");
		// 第 11 BYTE HEX 0F
		sb.append("0F");
		sb.append(data);
		return sb.toString();
	}

	private void sendMsg(String cid, String reqData) {
		MessageAsynchronousWaitReceiverManager.messageArrived(this, cid, reqData);
	}
}
