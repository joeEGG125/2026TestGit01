package com.syscom.fep.server.aa.inbk;


import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Richard
 */
public class PurchaseRequestI extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;

	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private boolean isEC = false;
	private Nwdtxn defNWDTXN;
	private String W_TXRUST;

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 *        初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *        ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *        FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception Exception
	 */
	public PurchaseRequestI(FISCData txnData) throws Exception {
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

			//2.AddTxData:新增交易記錄(FEPTXN& ICTLTXN)
			_rtnCode2 = this.addTxData();
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
				if (("Y").equals(feptxn.getFeptxnCbsProc())) { //走3-1流程，程式結束
					return StringUtils.EMPTY;	//程式結束
				}
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

			//8.ProcessAPTOT:更新跨行代收付
			_rtnCode = this.processAPTOT();

			//9.將組好的財金電文送給財金
			_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//10.判斷是否需傳送2160電文給財金
			/* 2025/7/1 修改 for 傳送 2160電文給財金 */
			/* "A" : 成功或失敗均需傳送 */
			if("A".equals(feptxn.getFeptxnSend2160())
					&& !"4001".equals( feptxn.getFeptxnRepRc()) ) {
				_rtnCode = insertINBK2160();
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
	 * 拆解並檢核由財金發動的Request電文
	 *
	 * @return FEPReturnCode
	 *
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// '檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

		if (rtnCode !=CommonReturnCode.Normal) {  /*FISC RC:Garbled Message*/
			getFiscBusiness().setFeptxn(null);
			getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
			return rtnCode;
		}

		return rtnCode;
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

			//(2.2) 	新增交易記錄
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
	 * 3. 	CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
	 * @return FEPReturnCode
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			/* 2024/7/11 點掉檢核特約商店代號, 由CBS檢核 */
			//(1) 	檢核特約商店代號 for 2541,2542,2543
//			if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnMerchantId())){
//				if(!FISCPCode.PCode2543.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())){
//					rtnCode=getFiscBusiness().checkMerchant();
//					if(rtnCode != CommonReturnCode.Normal){
//						return rtnCode;
//					}
//				}
//			}

			//(3.1) 	檢核MAC
			//20220726 只CHECK MAC, TAC 由CBS檢核
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			this.logContext.setRemark("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

			//(3.2) 	@檢核&更新原始交易狀態  FOR  2542
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())
					&& FISCPCode.PCode2542.getValueStr().equals(getFiscReq().getProcessingCode())) {
				rtnCode = checkoriFEPTXN();
				if (("Y").equals(feptxn.getFeptxnCbsProc())) { //走3-1流程，程式結束
					return rtnCode;
				}
				if (rtnCode != FEPReturnCode.Normal) {
					// 20240701 增加checkoriFEPTXN error log
					this.logContext.setRemark("checkoriFEPTXN error:" + rtnCode.toString());
					logMessage(this.logContext);
					return rtnCode;
				}
			}

			//(3.3) 	檢核單筆限額
			if(getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
					&& !"0".equals(getTxData().getMsgCtl().getMsgctlCheckLimit().toString())){
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if(rtnCode != FEPReturnCode.Normal){
					return rtnCode;
				}
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
	 * 4. SendToCBS:送往CBS主機處理
	 *
	 * @return FEPReturnCode
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			/* 交易前置處理查詢處理 */
			String txType = getTxData().getMsgCtl().getMsgctlCbsFlag().toString();
			switch(getFiscBusiness().getFeptxn().getFeptxnPcode()){
				case "2525":
				case "2541":
				case "2542":
					txType="1";
					break;
				case "2543":
					txType="0";
					break;
			}
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
	 * @throws Exception prepareForFISCException
	 */
	private FEPReturnCode prepareForFISC() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		//(6.1)		判斷 rtnCode 是否 Normal
		if (_rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				this.logContext.setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			}
		} else {
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())){
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK); /*+REP*/
			}
		}

		//(6.2)		產生 Response 電文Header:
		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}

		//(6.3)		產生 Response 電文Body:
		String wk_BITMAP = null;
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
			/*跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組*/
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
		} else {  /*-REP*/
			wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
		}
		// 依據wk_BITMAP(判斷是否搬值)
		for (int i = 2; i <= 63; i++) {
			// Loop IDX from 3 to 64
			if (wk_BITMAP.charAt(i) == '1') {
				switch (i) {
					case 2: { /* 交易金額 */
						getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
						break;
					}
					case 5: { /* 代付單位 CD/ATM 代號 */
						getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
						break;
					}
					case 6: { /* 可用餘額 */
						/* 11/19 配合永豐修改, 改送帳戶餘額(FEPTXN_BALB) */
						getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
						break;
					}
					case 13: { /*TROUT_BKNO for 2531,2568,2569繳稅交易*/
						//20220927以主機回應回傳
						getFiscRes().setTroutBkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno7());
						break;
					}
					case 14: { /* 跨行手續費 */
						getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
						break;
					}
					case 16: { /* 狀況代號 */
						getFiscRes().setRsCode(getFiscBusiness().getFeptxn().getFeptxnRsCode());
						break;
					}
					case 21: {
						//20220927 改用CBS_TOTA.LUCKYNO
						getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
						break;
					}
					case 37: {
						getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
						break;
					}
				}
			}
		}

		//(6.4)		產生 MAC
		RefString refMac = new RefString(getFiscRes().getMAC());
		rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscRes().getMessageType(), refMac);
		getFiscRes().setMAC(refMac.get());
		if (rtnCode != FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setMAC("00000000");
		}

		//(6.5)		產生Bit Map
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
			//(7.1) 	更新 FEPTXN
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
				} else {// (2 way)
					if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
							&& !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
						getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
					} else {
						getFiscBusiness().getFeptxn().setFeptxnTxrust("D"); // 已沖銷成功
					}
				}
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
							&& !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
						isEC = false;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
					} else {
						isEC = true;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 2);
					}
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {// spec change 20101124
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 拒絕
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != FEPReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
				return rtnCode;
			}
			// (7.2)	判斷是否需更新原始交易  for  2542
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan()) && getFiscBusiness().getOriginalFEPTxn() != null) {
				if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getOriginalFEPTxn().setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D"); /*已沖正成功*/
					getFiscBusiness().getOriginalFEPTxn().setFeptxnNeedSendCbs((short)2);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnAccType((short)2);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnClrType((short)2);
					if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
						// 若更新失敗則不送回應電文, 人工處理
						rtnCode = IOReturnCode.FEPTXNUpdateError;
						return rtnCode;
					}
				}else{// -REP
					  // 授權交易需先上主機解圏, 若解圏成功則 TXRUST = “C”,
					  // 所以若TXRUST = “T”進行中, 即可將原交易之狀態改回 Active
					if ("T".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// 進行中for沖銷
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust(W_TXRUST); // 2024/7/26 修改, 恢復原交易之狀態W_TXRUST
						if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 若更新失敗則不送回應電文，人工處理
							rtnCode = IOReturnCode.FEPTXNUpdateError;
							return rtnCode;
						}
					}
				}
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
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}


	/**
	 * 3.3    檢核更新原始交易狀態
	 *
	 * @return FEPReturnCode
	 *
	 */
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String I_TX_DATE = "";
		// QueryFEPTXNByStan:
		try {
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
			getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				// 若查無原交易
				if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8);
				}
				// 查詢原交易
				rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
				if (rtnCode != FEPReturnCode.Normal) {
					rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易 spec change 20100720
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark("SearchFeptxn 無此交易");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
					return rtnCode;
				}
			}else{
				/* 2024/7/26 將原交易之狀態暫存至變數 */
				W_TXRUST = getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust();
			}

			/* 2025/3/21 修改 for 消費扣款沖正(2542)原交易為送IMS主機 */
			if (("Y").equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnCbsProc())) {
				getFiscBusiness().getFeptxn().setFeptxnCbsProc("Y");
				try {
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
					rtnCode = new CBS(hostAA, getTxData()).sendToIMS(getFiscReq().getFISCMessage().toString(),getFiscReq(),getFiscBusiness().getFiscINBKRes(),rtnCode);
				} catch (Exception ex) {
					getLogContext().setProgramException(ex);
					getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToIMS"));
					sendEMS(getLogContext());
					return FEPReturnCode.ProgramException;
				}
				if (rtnCode == FEPReturnCode.Normal) { /* 2025/2/26 修改, 在ＡＡ　將主機回覆電文傳回財金 */
					rtnCode = getFiscBusiness().sendToFISCFromCBSRespons();
					this.logContext.setRemark("sendToFISCFromCBSRespons complete");
					logMessage(Level.DEBUG, this.logContext);
				}
				return rtnCode; /*程式結束*/
			}

			/// *檢核原交易是否成功*/
			if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// /*交易成功*/
				rtnCode = FISCReturnCode.TransactionNotFound; // 4701:無此交易
				getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
				return rtnCode;
			}

			/*檢核原交易之 MAPPING 欄位是否相同*/
			if (getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue() != getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue()
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmType().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim())){
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
				return rtnCode;
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());

			/* 10/12 修改 */
			getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnReqDatetime().substring(0, 8));
			/*2024/8/19 修改 for 將原交易EJ 存入 FEPTXN_TRK3 */
			getFiscBusiness().getFeptxn().setFeptxnTrk3(getFiscBusiness().getOriginalFEPTxn().getFeptxnEjfno().toString());

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkoriFEPTXN");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 以日期搜尋 FEPTXN
	 *
	 * @return FEPReturnCode
	 *
	 */
	private FEPReturnCode searchOriginalFEPTxn(String txDate, String bkno, String stan) {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
		Bsdays aBSDAYS = new Bsdays();
		BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
		String wk_TBSDY = null;
		String wk_NBSDY = "";
		// Dim i As Int32
		try {
			db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
			getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
				aBSDAYS.setBsdaysDate(txDate);
				aBSDAYS = dbBSDAYS.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
				if (aBSDAYS == null) {
					return IOReturnCode.BSDAYSNotFound;
				}
				// ASK CONNIE
				if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
					wk_TBSDY = aBSDAYS.getBsdaysDate();
					wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
				} else {
					wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
				}
				if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
					db.setTableNameSuffix(wk_TBSDY.substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							db.setTableNameSuffix(wk_NBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness().setOriginalFEPTxn(
									db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
							if (getFiscBusiness().getOriginalFEPTxn() == null) {
								rtnCode = IOReturnCode.FEPTXNNotFound;
							}
						} else {
							rtnCode = IOReturnCode.FEPTXNNotFound;
						}
					}
				} else {
					rtnCode = IOReturnCode.FEPTXNNotFound;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "searchOriginalFEPTxn");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
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
