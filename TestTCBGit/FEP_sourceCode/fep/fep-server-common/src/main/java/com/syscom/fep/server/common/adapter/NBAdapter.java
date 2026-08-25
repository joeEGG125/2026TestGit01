package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;

public class NBAdapter extends AdapterBase {

	private MessageBase _txData;
	private String MessageFromNB;

	public String getMessageFromNB() {
		return MessageFromNB;
	}

	public void setMessageFromNB(String value) {
		MessageFromNB = value;
	}

	private String MessageToNB;

	public String getMessageToNB() {
		return MessageToNB;
	}

	public void setMessageToNB(String value) {
		MessageToNB = value;
	}

	private String Action;

	public String getAction() {
		return Action;
	}

	public void setAction(String value) {
		Action = value;
	}

	private String APPUrl;

	public String getAPPUrl() {
		return APPUrl;
	}

	public void setAPPUrl(String value) {
		APPUrl = value;
	}

	public NBAdapter(MessageBase txData) {
		_txData = txData;

	}

	@Override
	public FEPReturnCode sendReceive() {
		FEPReturnCode rtnCode = CommonReturnCode.CBSResponseError;

		_txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
		_txData.getLogContext().setMessageFlowType(MessageFlow.Request);
		_txData.getLogContext().setMessage(getMessageToNB().replace("|", "!"));
		_txData.getLogContext().setProgramName(ProgramName + ".sendReceive");
		_txData.getLogContext().setRemark("NBAdapter Receive AA Message");
		this.logMessage(_txData.getLogContext());

		try {
			setMessageFromNB(send(getMessageToNB()));

			// Trace.WriteLine("SendReceive() MessageFromNB:" + MessageFromNB);

			rtnCode = FEPReturnCode.Normal;
		}
		/*
		 * catch (WebException ex) { todo
		 * if (ex.Status == WebExceptionStatus.Timeout) {// 逾時未收到訊息
		 * rtnCode = CommonReturnCode.HostResponseTimeout;
		 * } else {
		 * rtnCode = CommonReturnCode.CBSResponseError;
		 * }
		 * _txData.getLogContext().setProgramException(ex);
		 * _txData.getLogContext().setReturnCode(rtnCode);
		 * sendEMS(_txData.getLogContext());
		 * 
		 * }
		 * catch (SocketTimeoutException ex) {
		 * rtnCode = CommonReturnCode.HostResponseTimeout;
		 * _txData.getLogContext().setReturnCode(rtnCode);
		 * _txData.getLogContext().setProgramException(ex);
		 * sendEMS(_txData.getLogContext());
		 * }
		 */
		catch (Exception ex) {
			rtnCode = CommonReturnCode.ProgramException;
			_txData.getLogContext().setReturnCode(rtnCode);
			_txData.getLogContext().setProgramException(ex);
			sendEMS(_txData.getLogContext());
		} finally {
			_txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
			_txData.getLogContext().setProgramName(ProgramName + ".sendReceive");
			// modified By Maxine on 2011/12/19 for 增加判斷避免MessageFromNB為null時拋異常
			if (getMessageFromNB() == null) {
				_txData.getLogContext().setMessage("");
			} else {
				_txData.getLogContext().setMessage(getMessageFromNB().replace("|", "!"));
			}
			_txData.getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logMessage(_txData.getLogContext());
		}

		return rtnCode;
	}

	private String send(String reqData) {
		String rtn = "";
		return rtn;
	}
}
