package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.enums.ATMTXCD;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

public class IVRAdapter extends AdapterBase {

	private static String ProgramName = "IVRAdapter";
	private static String IVRMSGID = "VERIFYTPIN";
	private MessageBase _txData;
	private String messageFromIVR;
	private String _pinBlock;
	private Feptxn _feptxn;

	public String getMessageFromIVR() {
		return messageFromIVR;
	}

	public void setMessageFromIVR(String value) {
		messageFromIVR = value;
	}

	private String messageToIVR;

	public String getMessageToIVR() {
		return messageToIVR;
	}

	public void setMessageToIVR(String value) {
		messageToIVR = value;
	}

	public IVRAdapter(MessageBase txData, Feptxn feptxn, String pinBlock) {
		_txData = txData;
		_pinBlock = pinBlock;
		_feptxn = feptxn;
	}

	@Override
	public FEPReturnCode sendReceive() {
		FEPReturnCode rtnCode = CommonReturnCode.CBSResponseError;

		_txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
		_txData.getLogContext().setMessageFlowType(MessageFlow.Request);
		_txData.getLogContext().setMessage(getMessageToIVR());
		_txData.getLogContext().setProgramName(ProgramName + ".sendReceive");
		this.logMessage(_txData.getLogContext());
		try {
			setMessageFromIVR(send(getMessageToIVR()));
			return FEPReturnCode.Normal;
		}

		catch (Exception ex) {

			_txData.getLogContext().setProgramException(ex);
			sendEMS(_txData.getLogContext());
			rtnCode = CommonReturnCode.ProgramException;
		} finally {
			_txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
			_txData.getLogContext().setProgramName(ProgramName + ".sendReceive");
			// modified By Maxine on 2011/12/19 for 增加判斷避免MessageFromNB為null時拋異常
			if (getMessageFromIVR() == null) {
				_txData.getLogContext().setMessage("");
			} else {
				_txData.getLogContext().setMessage(getMessageFromIVR());
			}
			_txData.getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logMessage(_txData.getLogContext());
		}

		return rtnCode;
	}

	public final String prepareIVRRequest() {
		StringBuilder rtn = new StringBuilder();
		rtn.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		rtn.append("<IVR>");
		rtn.append("<RqHeader>");
		rtn.append("<MsgID>" + IVRMSGID + "</MsgID>");
		rtn.append("<MsgType>" + BINPROD.Gift + "</MsgType>");
		rtn.append("<ChlName>" + FEPChannel.FEP.name() + "</ChlName>");
		rtn.append("<ChlEJNo>" + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + StringUtils.leftPad(_feptxn.getFeptxnEjfno().toString(), 12, '0') + "</ChlEJNo>");
		rtn.append("<ChlSendTime>" + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS)+ "</ChlSendTime>");
		rtn.append("<TxnID>" + ATMTXCD.G51.name() + "</TxnID>");
		rtn.append("<BranchID>" + _feptxn.getFeptxnAtmno() + "</BranchID>");
		rtn.append("<TermID></TermID>");
		rtn.append("<UserID></UserID>");
		rtn.append("</RqHeader>");
		rtn.append("<SvcRq>");
		rtn.append("<Rq>");
		rtn.append("<IDNo>" + _feptxn.getFeptxnIdno() + "</IDNo>");
		rtn.append("<AccountNo></AccountNo>");
		rtn.append("<PINBlock>" + _pinBlock + "</PINBlock>");
		rtn.append("<MAC></MAC>");
		rtn.append("</Rq>");
		rtn.append("</SvcRq>");
		rtn.append("</IVR>");

		setMessageToIVR(rtn.toString());
		
		return getMessageToIVR();
	}

	private String send(String reqData) {
		// 2021-11-18 Richard modified for IVR只有永豐才有, 你IVRAdapter先不用實作直接回IVRResponse的string即可 by Ashiang
		StringBuilder rtn = new StringBuilder();
		rtn.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		rtn.append("<IVR>");
		rtn.append("<RsStatCode>SUCCESS</RsStatCode>");
		rtn.append("</IVR>");
		return rtn.toString();
	}

}
