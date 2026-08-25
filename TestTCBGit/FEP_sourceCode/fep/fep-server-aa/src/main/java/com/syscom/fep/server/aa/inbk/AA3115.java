package com.syscom.fep.server.aa.inbk;

import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCReturnCode;

public class AA3115 extends INBKAABase {
	/// #Region "共用變數宣告"
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private String rtnMessage = "";

	/**
	 * 
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * 
	 *                初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *                ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *                FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception
	 * 
	 */
	/// #Region "建構式"
	public AA3115(FISCData txnData) throws Exception {
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

		try {
			// 1.準備交易記錄檔
			_rtnCode = getFiscBusiness().prepareFeptxnOpc("3115");

			// 2.新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().insertFEPTxn();
			}

			// 3.檢查狀態
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.checkStatus();
			}

			// 4.組送財金Request 電文(OC065)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}

			// 5.送財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISCOpc();
			}

			// 6.處理財金Response電文(OC066)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.processResponse();
			}

			// 7.更新交易記錄檔
			_rtnCode = this.updateFEPTXN();

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");// MessageBase.GetCurrentMethod().Name)
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramName(ProgramName);
			if (_rtnCode != CommonReturnCode.Normal) {
				getTxData().getTxObject()
						.setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			} else {
				getTxData().getTxObject()
						.setDescription(getFiscOPCRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(
								getFiscOPCRes().getResponseCode(), FEPChannel.FISC, getLogContext()));
			}

			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(rtnMessage);
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(getTxData().getTxObject().getDescription());
			logMessage(Level.DEBUG, getLogContext());
		}

		return "";

	}
	/// #End Region

	/// #Region "1.檢查狀態"
	/**
	 * 檢查狀態 ''' <summary> ''' 檢查狀態 ''' </summary> ''' <returns></returns> '''
	 * <remarks></remarks>
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	private FEPReturnCode checkStatus() throws Exception {
		// '檢核本行OP狀態，必須等於3，FISC NOTICE CALL完成才可執行本交易
		// '0 - 日終 HOUSE KEEPING 完成
		// '1 - FISC Wakeup Call 完成或M-BANK Wakeup Call完成
		// '2 - FISC Key-Syn Call完成
		// '3 - FISC Notice Call完成
		// '9 - 參加單位關機交易完成
		// 'D - 參加單位不營業
		// 檢核本行OP狀態，必須為”3” (FISC NOTICE CALL完成) 才可執行本交易
		if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
			return FISCReturnCode.INBKStatusError;
		}

		return CommonReturnCode.Normal;

	}
	/// #End Region

	/// #Region "4.組送財金Request 電文(OC065)"
	/**
	 * 組送財金Request 電文(OC007) ''' <summary> ''' 組送財金Request 電文(OC065) ''' </summary>
	 * ''' <returns></returns> ''' <remarks></remarks>
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 電文Header
		rtnCode = getFiscBusiness().prepareHeader("0600");
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 產生Bitmap
		rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(),
				MessageFlow.Request);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 產生財金Flatfile電文
		rtnCode = getFiscOPCReq().makeFISCMsg();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		return rtnCode;

	}
	/// #End Region

	/// #Region "6.處理財金Response電文(OC066)"
	/**
	 * 處理財金Response電文(OC066) ''' <summary> ''' 處理財金Response電文(OC066) ''' </summary>
	 * ''' <returns></returns> ''' <remarks></remarks>
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processResponse() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			// FISC RESPONSE
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

			// 檢核Header
			rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
			if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate
					|| rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
					|| rtnCode == FISCReturnCode.SenderIdError || rtnCode == FISCReturnCode.CheckBitMapError) {
				getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
				return rtnCode;
			}

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
	 * 更新交易記錄檔 ''' <summary> ''' 更新交易記錄檔 ''' </summary> ''' <returns></returns> '''
	 * <remarks></remarks> ''' <modifier>HusanYin</modifier> ''' <reason>修正Const
	 * RC</reason> ''' <date>2010/11/25</date> ''' </modify>
	 * 
	 */
	private FEPReturnCode updateFEPTXN() {

		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;

		getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
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
