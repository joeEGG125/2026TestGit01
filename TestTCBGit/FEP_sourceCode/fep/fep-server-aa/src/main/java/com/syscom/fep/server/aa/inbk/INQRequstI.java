package com.syscom.fep.server.aa.inbk;


import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.vo.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;

/**
 * 處理財金發動餘額查詢交易電文
 * @author Richard --> Ben
 */
public class INQRequstI extends INBKAABase {

	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;


	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 *        初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *        ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *        FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception INQRequstI
	 */
	public INQRequstI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {

			// (1) 	檢核財金電文 Header
			_rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
			String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
			if("10".equals(sFiscRc.substring(0, 2))) {
				/* 程式結束 FISC RC:Garbled Message */
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
				return StringUtils.EMPTY;
			}

			//2.AddTxData:新增交易記錄(FEPTXN& FEPTXNTCB)
			_rtnCode2 = this.addTxData();
			//程式結束
			if (_rtnCode2 != FEPReturnCode.Normal) {
				// 新增 FEPTXN、FEPTXNTCB 失敗 就結束程式
				getLogContext().setProgramName(ProgramName + ".addTxData");
				getLogContext().setMessage(_rtnCode2.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				sendEMS(getLogContext());
				return StringUtils.EMPTY;
			}

			//3.商業邏輯檢核 & 電文Body檢核
			if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
				_rtnCode = checkBusinessRule();
			}

			//4. SendToCBS: 帳務主機處理
			if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
				_rtnCode2 = this.sendToCBS();
                /*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
				則仍需組回應電文給財金 */

				if (feptxn.getFeptxnCbsTimeout() == 1) {
					/* 主機TimeOut 不組回應電文給財金, 程式結束 */
					getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
					getFiscBusiness().updateTxData();//更新交易記錄
					return StringUtils.EMPTY;
				}
			}

			//5. 	label_END_OF_FUNC

			//6.PrepareFISC:準備回財金的相關資料
			_rtnCode = this.prepareForFISC();

			//7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
			_rtnCode = this.updateTxData();
			//程式結束
			if(_rtnCode != FEPReturnCode.Normal) {
				getLogContext().setRemark("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY;
			}

			//8.SendToFISC送回覆電文到財金
			_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//9.判斷是否需傳送2160電文給財金
				/* 	“A”: 成功或失敗均需傳送
				“Y”  : 僅成功交易傳送    */
			if( "4001".equals(feptxn.getFeptxnRepRc()) ) {
				if ("Y".equals(feptxn.getFeptxnSend2160()) || "A".equals(feptxn.getFeptxnSend2160())) {
					/* 寫入2160發送資料檔 */
					_rtnCode = insertINBK2160();
				}
			}else{
				if ("A".equals(feptxn.getFeptxnSend2160())) {
					/* 寫入2160發送資料檔 */
					_rtnCode = insertINBK2160();
				}
			}

		} catch (Exception e) {
			getLogContext().setProgramException(e);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage("FiscResponse:" + this.getFiscRes().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		// 2011/03/17 modified by Ruling for 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
		return StringUtils.EMPTY;
	}


	/**
	 * 2. 新增交易記錄
	 *
	 * @return FEPReturnCode
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		try {
			//(1) 	Prepare() 交易記錄初始資料
			rtnCode = getFiscBusiness().prepareFEPTXN();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
				getLogContext().setMessage("PREPARE FEPTXN  ERROR");
				sendEMS(getLogContext());
				return rtnCode;
			}

			rtnCode = getFiscBusiness().prepareFEPTXNTCB();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
				getLogContext().setMessage("PREPARE FEPTXNTCB ERROR");
				sendEMS(getLogContext());
				return rtnCode;
			}

			//(2) 	新增交易記錄
			PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
			TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
			try {
				rtnCode = getFiscBusiness().insertFEPTxn();
				if (rtnCode != FEPReturnCode.Normal) {
					return rtnCode;
				}

				rtnCode = getFiscBusiness().insertFEPTXNTCB();
				if (rtnCode != FEPReturnCode.Normal) {
					return rtnCode;
				}

				transactionManager.commit(txStatus);
				return rtnCode;
			}catch (Exception ex) {
				getLogContext().setProgramException(ex);
				getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
				sendEMS(getLogContext());
				return FEPReturnCode.ProgramException;
			}finally {
				if ( !txStatus.isCompleted()) {
					transactionManager.rollback(txStatus);
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}


	/**
	 * 商業邏輯檢核 & 電文Body檢核
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			//(1) 	檢核MAC
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
			getLogContext().setMessage("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(getLogContext());
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkBusinessRule");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 4.SendToCBS: 帳務主機處理
	 * 
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String txType = "0";		//查詢
		try {
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(txType);
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendToCBSAndAsc");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 6.PrepareFISC:準備回財金的相關資料
	 * @return FEPReturnCode
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			if (_rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					// modified by Maxine for 2011/07/14 for 傳入LogContext
					// getFiscBusiness().getFeptxn().FEPTXN_REP_RC = TxHelper.GetRCFromErrorCode(strFISCRc, FEPChannel.FISC)
					getLogContext().setProgramName(ProgramName);
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
				}
			} else {
                if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
                    getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK); /*+REP*/
                }
			}

			//6.2 產生 Response 電文Header:
			rtnCode = getFiscBusiness().prepareHeader("0210");
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			}

			//6.3 產生 Response 電文Body:
			String wk_BITMAP = null;
			// 依據wk_BITMAP(判斷是否搬值)
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {        /*-REP*/
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}
			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				// Loop IDX from 3 to 64
				if (wk_BITMAP.charAt(i) == '1') {
					/* 參考 "財金電文整理_Response.xls" 搬值 (參考FEPTXN MAPPING 欄位)
					欄位特別說明如下:  */
					switch (i) {
						case 5: {        /* 代付單位 CD/ATM 代號 */
							getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
							break;
						}
						case 6: {        /* 可用餘額 */
							/* 11/19 配合永豐修改, 改送帳戶餘額(FEPTXN_BALB) */
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBala().toString());
							break;
						}
						case 14: {        /* 跨行手續費 */
							getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
							break;
						}
						case 37: {        /* 帳戶餘額*/
							getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						}
					}
				}
			}

			//6.4 產生 MAC
			RefString refMac = new RefString(getFiscRes().getMAC());
			rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setMAC("00000000");
			}

			//6.5 產生Bit Map
			rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
			getLogContext().setMessage("after makeBitmap RC:" + rtnCode.toString());
			logMessage(getLogContext());
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setBitMapConfiguration("0000000000000000");
			}
			rtnCode = getFiscRes().makeFISCMsg();
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareForFISC");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
	 * @return FEPReturnCode
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {

				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
				} else {// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
				}

			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {	/*初值:0*/
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); 					/*拒絕-正常*/
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1);	/*AA Close*/
			
			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != FEPReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
				return rtnCode;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateTxData");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}
	/**
	 * 9. 	判斷是否需傳送2160電文給財金
	 * @return
	 */
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().prepareInbk2160();
			if(rtnCode != FEPReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setProgramName(ProgramName + ".insertINBK2160");
				getLogContext().setRemark("寫入insertINBK2160發生錯誤!!");
				logMessage(getLogContext());
				return FEPReturnCode.INBK2160InsertError;
			}else{
				return FEPReturnCode.Normal;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".insertINBK2160");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}
}
