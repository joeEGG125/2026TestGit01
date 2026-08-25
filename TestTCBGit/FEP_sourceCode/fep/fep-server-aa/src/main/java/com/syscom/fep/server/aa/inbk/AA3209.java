package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.server.common.TxHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import com.syscom.fep.vo.enums.FISCReturnCode;

public class AA3209 extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

	public AA3209(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金發動的Request電文Header
			_rtnCode = getFiscBusiness().checkHeader(getFiscOPCReq(), false);

			// 2.SendEMS
			// LogContext.ReturnCode = _rtnCode
			// LogContext.Remark = fiscOPCReq.FISCMessage
			// SendEMS(LogContext)

			// 3.判斷是否為Garbled Message
			if (_rtnCode == FISCReturnCode.MessageTypeError || _rtnCode == FISCReturnCode.TraceNumberDuplicate
					|| _rtnCode == FISCReturnCode.OriginalMessageError || _rtnCode == FISCReturnCode.STANError
					|| _rtnCode == FISCReturnCode.SenderIdError || _rtnCode == FISCReturnCode.CheckBitMapError) {
				getFiscBusiness().sendGarbledMessage(getFiscOPCReq().getEj(), _rtnCode, getFiscOPCReq());
				return "";
			}

			// 4.準備交易記錄檔＆新增交易記錄檔
			_rtnCode = prepareAndInsertFEPTXN();

			// add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
			getLogContext().setRemark(StringUtils.join("_rtnCode=", _rtnCode.toString(), ";"));
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			if (_rtnCode == CommonReturnCode.Normal) {
				getLogContext().setpCode(getFeptxn().getFeptxnPcode());
				getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
				getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc()); // RC:1001 ~ 1006
				getLogContext().setMessageGroup("1"); // OPC

				getLogContext().setMessageParm13(getFeptxn().getFeptxnRemark());
				getLogContext().setProgramName(ProgramName);
				getLogContext().setRemark(
						TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.FISCGarbledMessage, getLogContext()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());

			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscOPCReq().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Request);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
		}

		return "";
	}

	/**
	 * 準備交易記錄檔＆新增交易記錄檔
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareAndInsertFEPTXN() {
		FEPReturnCode rtnCode = null;
		try {
			rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			} else {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
				getFiscBusiness().getFeptxn().setFeptxnRemark(
						StringUtils.join(EbcdicConverter.fromHex(CCSID.English, getFiscOPCReq().getBasicHeader().substring(0, 90)),
								getFiscOPCReq().getBasicHeader().substring(90, 98))); // 將 HEX 轉成 ASCII 存入
				rtnCode = getFiscBusiness().insertFEPTxn();
				if (rtnCode == CommonReturnCode.Normal) {
					return rtnCode;
					// Return _rtnCode
				} else {
					return rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareAndInsertFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
