package com.syscom.fep.server.aa.inbk;

import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;

public class AA3100 extends INBKAABase {
	/// #Region "共用變數宣告"
	private String ProgramName = AA3100.class.getSimpleName(); // MethodBase.GetCurrentMethod().DeclaringType.FullName;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

	// @Override
	// public String processRequestData() {
	// return null;
	// }
	/// #End Region

	/// #Region "建構式"
	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * 
	 *        初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *        ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *        FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception
	 * 
	 */

	public AA3100(FISCData txnData) throws Exception {
		super(txnData);
	}

	/// #End Region

	/// #Region "AA進入點主程式"
	/**
	 * 程式進入點
	 * 
	 * @return Response電文
	 * 
	 */

	@Override
	public String processRequestData() {
		boolean needUpdateFEPTXN = false;

		try {
			// 1.準備交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().prepareFeptxnOpc("3100");
			}

			// 2.新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().insertFEPTxn();
				needUpdateFEPTXN = true;
			}

			// 3.檢查狀態
			_rtnCode = this.checkData();

			// 4.組送財金Request 電文(OC007)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}

			// 5.送財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISCOpc();
			}

			// 6.處理財金Response電文(OC008)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.processResponse();
			}

			// 7.更新交易記錄檔
			if (needUpdateFEPTXN) {
				_rtnCode = this.updateFEPTXN();
			}

			// add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
			getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc() + ";");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
				getLogContext().setpCode(getFeptxn().getFeptxnPcode());
				getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("1"); // OPC
				getLogContext().setMessageParm13(getFiscOPCReq().getNoticeData());
				getLogContext().setMessageParm14(getFiscOPCReq().getNoticeId());

				getLogContext().setProgramName(ProgramName);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankNoticeCall, getLogContext()));

				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");// MessageBase.GetCurrentMethod().Name) ;
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramName(ProgramName);
			if (_rtnCode != CommonReturnCode.Normal) {
				getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			} else {
				getTxData().getTxObject()
						.setDescription(getFiscOPCRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(getFiscOPCRes().getResponseCode(), FEPChannel.FISC, getLogContext()));
			}

			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			// modified by maxine on 2011/07/11 for 避免送多次EMS
			getLogContext().setRemark(getTxData().getTxObject().getDescription());
			// LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
			logMessage(Level.DEBUG, getLogContext());
		}

		return "";

	}
	/// #End Region

	/// #Region "1.檢查狀態"
	/**
	 * 檢查狀態
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	private FEPReturnCode checkData() throws Exception {

		// 檢核本行OP狀態，必須為”3” (FISC NOTICE CALL完成) 才可執行本交易
		if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
			return FISCReturnCode.INBKStatusError;
		}

		return CommonReturnCode.Normal;

	}
	/// #End Region

	/// #Region "4.組送財金Request 電文(OC007)"
	/**
	 * 組送財金Request 電文(OC007)
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		rtnCode = getFiscBusiness().prepareHeader("0600");
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		RefString refMac = new RefString(getFiscOPCReq().getMAC());
		rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), refMac);
		getFiscOPCReq().setMAC(refMac.get());
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		rtnCode = getFiscOPCReq().makeFISCMsg();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		return rtnCode;

	}
	/// #End Region

	/// #Region "6.處理財金Response電文(OC008)"
	/**
	 * 處理財金Response電文(OC008)
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processResponse() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

			// 檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC並結束程式)
			rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
			if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
					|| rtnCode == FISCReturnCode.SenderIdError || rtnCode == FISCReturnCode.CheckBitMapError) {
				getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
				return rtnCode;
			}

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 檢核NOTICE_ID
			if (!getFiscOPCReq().getNoticeId().equals(getFiscOPCRes().getNoticeId())) {
				return FISCReturnCode.OriginalMessageDataError;
			}

			// 檢核MAC
			getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
			rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			return rtnCode;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processResponse"); // MethodBase.GetCurrentMethod().Name);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}
	/// #End Region

	/// #Region "7.更新交易記錄檔"
	/**
	 * 更新交易記錄檔
	 * 
	 * 
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;

		// fiscBusiness.FepTxn.FEPTXN_REP_RC = fiscOPCRes.ResponseCode
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());

		rtnCode = getFiscBusiness().updateTxData();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		} else {
			return _rtnCode;
		}

	}
	/// #End Region

}
