package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;

// ''' <summary>
// ''' 接收 UI010102之KEY ID，執行參加單位變更基碼通知交易
// ''' 本支負責處理電文如下
// ''' REQUEST ：OC027
// ''' RESPONSE：OC028
// ''' </summary>
// ''' <remarks>
// '''AA程式撰寫原則:
// '''AA的程式主要為控制交易流程,Main為AA程式的進入點,在Main中的程式為控制交易的過程該如何進行
// '''請不要在Main中去撰寫實際的處理細節,儘可能將交易過程中的每一個"步驟",以副程式的方式來撰寫,
// '''而這些步驟,如果可以共用的話,請將該步驟寫在相關的Business物件中(例如本程式中的CheckBusinessRule,ATMBusiness中的PrepareFEPTXN)
// '''如果該步驟只有該AA會用到的話,再寫至自己AA中的類別中
// ''' </remarks>
// ''' <history>
// ''' <modify>
// ''' <modifier>Henny</modifier>
// ''' <reason>AA Template</reason>
// ''' <date>2009/12/10</date>
// ''' </modify>
// ''' <modify>
// ''' <modifier>henny Chen</modifier>
// ''' <reason>coding review後調整程式</reason>
// ''' <date>2010/3/12</date>
// ''' </modify>
// ''' <modify>
// ''' <modifier>henny Chen</modifier>
// ''' <reason>邏輯調整</reason>
// ''' <date>2010/3/19</date>
// ''' </modify>
// ''' </history>
// ''' <history>
// ''' <modify>
// ''' <modifier>Ruling</modifier>
// ''' <reason>修正將財金的RC回給UI</reason>
// ''' <date>2011/2/16</date>
// ''' </modify>
// ''' </history>
public class AA0102 extends INBKAABase {
	// #Region "共用變數宣告"
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

	// "建構式"
	// ''' <summary>
	// ''' AA的建構式,在這邊初始化及設定其他相關變數
	// ''' </summary>
	// ''' <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
	// ''' <remarks>
	// ''' 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	// ''' ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	// ''' FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	// ''' </remarks>
	public AA0102(FISCData txnData) throws Exception {
		super(txnData);
	}

	// "AA進入點主程式"
	// ''' <summary>
	// ''' 程式進入點
	// ''' </summary>
	// ''' <returns>Response電文</returns>
	// ''' <remarks></remarks>
	@Override
	public String processRequestData() {
		boolean needUpdateFEPTXN = false;
		try {
			// 1.準備交易記錄檔
			_rtnCode = getFiscBusiness().prepareFeptxnOpc("0102");

			// 2.新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().insertFEPTxn();
				needUpdateFEPTXN = true;
			}

			// 3.組送財金Request 電文(OC027)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}

			// 4.送財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISCOpc();
			}

			// 5.處理財金Response電文(OC028)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.processResponse();
			}

			// 6.更新交易記錄檔
			if (needUpdateFEPTXN) {
				_rtnCode = this.updateFeptxn();
			}

			// add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
			getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc() + ";");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			if ("NormalRC.FISC_OK".equals(getFeptxn().getFeptxnRepRc())) {
				getLogContext().setpCode(getFeptxn().getFeptxnPcode());
				getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("1"); // OPC
				switch (getFiscOPCReq().getKEYID()) {
					case "01":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(OPC MAC KEY)");
						break;
					case "02":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(CD/ATM MAC KEY)");
						break;
					case "03":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(RM MAC KEY)");
						break;
					case "04":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(OPC 3-DES MAC KEY)");
						break;
					case "05":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(CD/ATM 3-DES MAC KEY)");
						break;
					case "06":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(RM 3-DES MAC KEY)");
						break;
					case "11":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(PP KEY)");
						break;
					case "12":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(3-DES PPKEY)");
						break;
					case "20":
						getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(TR-31 Key Block PP KEY)");
						break;
				}
				getLogContext().setProgramName(ProgramName);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankChangeKeyRequest, getLogContext()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
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
			getLogContext().setMessage(getFiscOPCReq().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			// modified by maxine on 2011/07/11 for 避免送多次EMS
			getLogContext().setRemark(getTxData().getTxObject().getDescription());
			// LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	// "3.組送財金Request 電文(OC027)"
	// ''' <summary>
	// ''' 組送財金Request 電文(OC027)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>

	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		rtnCode = getFiscBusiness().prepareHeader("0800");
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

	// "5.處理財金Response電文(OC028)"
	// ''' <summary>
	// ''' 處理財金Response電文(OC028)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processResponse() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

			// '檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC並結束程式)
			rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
			if (rtnCode == FISCReturnCode.MessageTypeError ||
					rtnCode == FISCReturnCode.TraceNumberDuplicate ||
					rtnCode == FISCReturnCode.OriginalMessageError ||
					rtnCode == FISCReturnCode.STANError ||
					rtnCode == FISCReturnCode.SenderIdError ||
					rtnCode == FISCReturnCode.CheckBitMapError) {
				getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
				return rtnCode;
			}

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
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
			getLogContext().setProgramName(ProgramName + ".processResponse");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// "6.更新交易記錄檔"
	// ''' <summary>
	// ''' 更新FEPTXN
	// ''' </summary>
	// ''' <remarks></remarks>
	private FEPReturnCode updateFeptxn() {

		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		/* 10/17 修改, 寫入處理結果 */
		getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1);

		if (_rtnCode != CommonReturnCode.Normal){
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R");   /* 處理結果=Reject */
		}else {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A");  /* 處理結果=成功 */
		}
		rtnCode = getFiscBusiness().updateTxData();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		} else {
			return _rtnCode;
		}
	}
}
