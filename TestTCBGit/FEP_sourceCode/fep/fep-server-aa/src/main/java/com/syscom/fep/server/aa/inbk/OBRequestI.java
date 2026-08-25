package com.syscom.fep.server.aa.inbk;


import com.syscom.fep.mybatis.configuration.DataSourceConstant;
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

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ObtltxnExtMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 負責處理財金發動的跨境電子支付交易Req電文
 * @author Richard	--> Ben 
 */
public class OBRequestI extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;

	private boolean isOB = false;
	private boolean isEC = false;
	private Obtltxn defOBTLTXN = new Obtltxn();
	private Obtltxn oriOBTLTXN = new Obtltxn();
	private ObtltxnExtMapper dbOBTLTXN = SpringBeanFactoryUtil.getBean(ObtltxnExtMapper.class);;

	public OBRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 1  拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			// (1.1) 	檢核財金電文 Header
			_rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
			String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
			// (1.2) 	判斷是否為晶片金融卡跨境電子支付交易
			//OB_DATA(Bitmap 36) 必須有值
			if (StringUtils.isNotBlank(getFiscReq().getOriData())) {
				isOB = true;
			}
			//程式結束
			if("10".equals(sFiscRc.substring(0, 2))) {
				/* 程式結束 FISC RC:Garbled Message */
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
				return StringUtils.EMPTY;
			}

			//2. 	AddTxData:新增交易記錄(FEPTXN、FEPTXNTCB & OBTLTXN)
			_rtnCode2 = this.addTxData();

			if (_rtnCode2 != FEPReturnCode.Normal) {
				// 新增 FEPTXN、FEPTXNTCB 失敗 就結束程式
				getLogContext().setProgramName(ProgramName + ".addTxData");
				getLogContext().setMessage(_rtnCode2.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				sendEMS(getLogContext());
				return StringUtils.EMPTY;
			}

			//3. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
			if (_rtnCode == FEPReturnCode.Normal) { /*CheckHeader Error*/
				_rtnCode = this.checkBusinessRule();
			}

			//4. 	SendToCBS: 帳務主機處理
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

			//6.	PrepareFISC:準備回財金的相關資料
			_rtnCode = this.prepareForFISC();


			//7.	UpdateTxData: 更新交易記錄(FEPTXN & OBTLTXN)
			_rtnCode = this.updateTxData();
			//程式結束
			if(_rtnCode != FEPReturnCode.Normal) {
				getLogContext().setRemark("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY;
			}

			//8.	ProcessAPTOT:更新跨行代收付
			_rtnCode = this.processAPTOT();

			//9.	SendToFISC送回覆電文到財金
			_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//10.判斷是否需傳送2160電文給財金
			/* 2025/7/1 修改 for 傳送 2160電文給財金 */
			/* "A" : 成功或失敗均需傳送 */
			if("A".equals(feptxn.getFeptxnSend2160())
					&& !"4001".equals( feptxn.getFeptxnRepRc()) ) {
				_rtnCode =insertINBK2160();
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}

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
			//(2.1) 	Prepare() 交易記錄初始資料
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

			RefBase<Obtltxn> defOBTLTXNBase = new RefBase<>(defOBTLTXN);
			RefBase<Obtltxn> oriOBTLTXNBase = new RefBase<>(oriOBTLTXN);
			rtnCode = getFiscBusiness().prepareObtltxn(defOBTLTXNBase, oriOBTLTXNBase, MessageFlow.Request);
			defOBTLTXN = defOBTLTXNBase.get();
			oriOBTLTXN = oriOBTLTXNBase.get();
			if (rtnCode != FEPReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName + ".PrepareObtltxn");
				getLogContext().setRemark("PREPARE Obtltxn ERROR");
				sendEMS(getLogContext());
				return rtnCode;
			}
			//(2.2) 	以 TRANSACTION 新增交易記錄
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

				if (dbOBTLTXN.insertSelective(defOBTLTXN) < 1) {
					return IOReturnCode.UpdateFail;
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
	 * 3. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			//(3.1) 檢核單筆限額
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != FEPReturnCode.Normal) {
					getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
					getLogContext().setRemark("超過單筆限額");
					logMessage(getLogContext());
					return rtnCode;
				}
			}
			//3.2 檢核財金MAC
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
			getLogContext().setRemark("after checkFiscMac RC:" + rtnCode.toString());
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
	 * 4. 	sendToCBS: 帳務主機處理
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String txType = "";		//上CBS入扣帳
		try {
			/* 2025/1/24 應合庫要求, 2555/25256一律送記帳 */
			txType = "1";
			//20221028一律使用MSGCTL_TWCBSTXID
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			this.getTxData().setObtlTxn(defOBTLTXN);
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
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		//(6.1)	判斷 rtnCode 是否 Normal
		if (_rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
				getLogContext().setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			}
		} else {
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK); /*+REP*/
			}
		}

		//(6.2)	產生 Response 電文Header:
		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}

		//(6.3) 產生 Response 電文Body:
		String wk_BITMAP = null;
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
			// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
		} else {// -REP
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
		}

		for (int i = 2; i <= 63; i++) {
			if (wk_BITMAP.charAt(i) == '1') {
				switch (i) {
					case 2: // 交易金額
						getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
						break;
					case 5: // 代付單位 CD/ATM 代號
						getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
						break;
				}
			}
		}

		//(6.4)	產生 MAC
		RefString refMac = new RefString(getFiscRes().getMAC());
		rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscRes().getMessageType(), refMac);
		getFiscRes().setMAC(refMac.get());
		if (rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setMAC("00000000");
		}

		//(6.5)	產生Bit Map
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
	 * 7. 	UpdateTxData: 更新交易記錄(FEPTXN & OBTLTXN)
	 * (1) 	更新 FEPTXN
	 * (2) 	判斷是否需更新 OBTLTXN
	 * (3) 	判斷是否需更新原始交易  for 2556
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// (1) 更新 FEPTXN
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // B Pending
				} else {// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A 成功
				}

				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					isEC = false;
					getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
				}
			} else if (FeptxnTxrust.Initial.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絕
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /// *AA Close*/

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != FEPReturnCode.Normal) {
				// 若更新失敗則不送回應電文, 人工處理
				transactionManager.rollback(txStatus);
				return rtnCode;
			}

			// (2) 判斷是否需更新 OBTLTXN
			if (isOB) {
				defOBTLTXN.setObtltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defOBTLTXN.setObtltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defOBTLTXN.setObtltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defOBTLTXN.setObtltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defOBTLTXN.setObtltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
				defOBTLTXN.setObtltxnMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
				defOBTLTXN.setObtltxnCardSeq(getFiscBusiness().getFeptxn().getFeptxnCardSeq());
				defOBTLTXN.setObtltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());

				if (dbOBTLTXN.updateByPrimaryKeySelective(defOBTLTXN) < 1) {
					// 若更新失敗則不送回應電文, 人工處理
					transactionManager.rollback(txStatus);
					getLogContext().setRemark("updateTxData-更新OBTLTXN失敗");
					logMessage(Level.INFO, getLogContext());
					return IOReturnCode.UpdateFail;
				}
			}
			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateTxData");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 更新跨行代收付
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processOBAptot(isEC);
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}

	/**
	 * 10. 	判斷是否需傳送2160電文給財金
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
