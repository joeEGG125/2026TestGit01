package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 處理財金發動資金調撥服務交易電文
 * @author Sarah
 */
public class FXRequestI extends INBKAABase {
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

	private boolean isEC = false;

	public FXRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * <summary>
	 * ''' 程式進入點
	 * ''' </summary>
	 * ''' <returns>Response電文</returns>
	 * ''' <remarks></remarks>
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.檢核財金電文 Header
			/*拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束*/
			rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
			String sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
			if ("10".equals(sFiscRc.substring(0, 2))) {
				//程式結束
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
				return StringUtils.EMPTY;
			}
			
			// 2.addTxData:新增交易記錄(FEPTXN、FEPTXNTCB)
			rtnCode2 = this.addTxData();
			if (rtnCode2 != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + ".addTxData");
				getLogContext().setMessage(rtnCode2.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	//程式結束
			}

			// 3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = checkBusinessRule();
			}

			// 4.SendToCBS: 帳務主機處理
			if (rtnCode == FEPReturnCode.Normal) {
				/*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
				則仍需組回應電文給財金 */
				rtnCode2 = this.sendToCBS();
				if (feptxn.getFeptxnCbsTimeout() == 1) {
					/* 主機TimeOut 不組回應電文給財金, 程式結束 */
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  //Reject-abnormal
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));  //AA Close
					getFiscBusiness().updateTxData();//更新交易記錄
					return StringUtils.EMPTY;
				}
			}

			// 5.label_END_OF_FUNC
			// 6.PrepareFISC:準備回財金的相關資料
			rtnCode = this.prepareForFISC();

			// 7.UpdateTxData: 更新交易記錄(FEPTXN)
			rtnCode = this.updateTxData();
			if (rtnCode != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setRemark("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY; //程式結束
			}

			// 8.ProcessAPTOT:更新跨行代收付
			rtnCode = this.processAPTOT();

			// 9.將組好的財金電文送給財金
			rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			// 10.判斷是否需傳送2160電文給財金
			/* 2025/7/1 修改 for 傳送 2160電文給財金 */
			/* "A" : 成功或失敗均需傳送 */
			if("A".equals(feptxn.getFeptxnSend2160())
					&& !"4001".equals(feptxn.getFeptxnRepRc())) {
				rtnCode = insertINBK2160();
			}

		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 2. addTxData: 新增交易記錄
	 * @return FEPReturnCode
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode ;
		try {
			//2.1	Prepare() 交易記錄初始資料
			rtnCode = getFiscBusiness().prepareFEPTXN();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
				getLogContext().setRemark("PREPARE FEPTXN  ERROR");
				sendEMS(getLogContext());
				return rtnCode;
			}
			rtnCode = getFiscBusiness().prepareFEPTXNTCB();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
				getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
				sendEMS(getLogContext());
				return rtnCode;
			}

			//2.2 以TRANSACTION 新增交易記錄
			// 兩個Table以上需同步新增，如有任何錯誤，請ROLLBACK & 寫EMS
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
			} catch (Exception ex) {
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
	 * 3. checkBusinessRule:　商業邏輯檢核
	 * @return FEPReturnCode
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			//(3.1)		檢核財金電文
			rtnCode = getFiscBusiness().CheckRequestFromFISC();
			if (rtnCode != FEPReturnCode.Normal) {
				//檢核財金電文錯誤, 不送帳務主機
				getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
				getLogContext().setRemark("after CheckRequestFromFISC RC:" + rtnCode.toString());
				logMessage(getLogContext());
				return rtnCode;
			}

			//(3.2)		檢核MAC
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			if (rtnCode != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
				getLogContext().setRemark("after checkFiscMac RC:" + rtnCode.toString());
				logMessage(getLogContext());
				return rtnCode;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}
	/**
	 * 4. SendToCBS/ASC(if need): 帳務主機處理
	 * @return FEPReturnCode
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			String txType = "0"; //檢核
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
	 * 6. 	PrepareFISC:準備回財金的相關資料
	 * @return FEPReturnCode
	 */
	private FEPReturnCode prepareForFISC() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		//6.1 判斷 rtnCode 是否 Normal
		if (this.rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
				getLogContext().setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(this.rtnCode, FEPChannel.FISC, getLogContext()));
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
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
		} else {// -REP
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
		}

		// 依據wk_BITMAP(判斷是否搬值)
		for (int i = 2; i <= 63; i++) {
			// Loop IDX from 3 to 64
			if (wk_BITMAP.charAt(i) == '1') {
				switch (i) {
					case 2: {  /* 交易金額 */
						getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
						break;
					}
					case 5: { /* 代付單位 CD/ATM 代號 */
						getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(), 8, "0"));
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
		getLogContext().setRemark("after makeBitmap RC:" + rtnCode.toString());
		logMessage(getLogContext());
		if (rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setBitMapConfiguration("0000000000000000");
		}
		rtnCode = getFiscRes().makeFISCMsg();
		return rtnCode;
	}

	/**
	 * 7. 	UpdateTxData: 更新交易記錄(FEPTXN )
	 * @return FEPReturnCode
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {	// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1);	// Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B");	// Pending
				} else {	// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A");	// 成功
				}
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					isEC = false;
					getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /* 初值:0 */
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");  /* 拒絕-正常 */
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);	// F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1);	// AA Close

			rtnCode = getFiscBusiness().updateTxData();
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
	 * 8. 	ProcessAPTOT:更新跨行代收付
	 * @return FEPReturnCode
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
				&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processICAptot(isEC);
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}

	/**
	 * 10. 	判斷是否需傳送2160電文給財金
	 * @return FEPReturnCode
	 */
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().prepareInbk2160();
			if(rtnCode != FEPReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setProgramName(ProgramName + ".insertINBK2160");
				getLogContext().setRemark("寫入INBK2160發生錯誤!!");
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
