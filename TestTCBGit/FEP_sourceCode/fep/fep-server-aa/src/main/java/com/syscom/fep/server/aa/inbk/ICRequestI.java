package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動晶片金融卡跨國交易電文
 * @author Richard	-> Ben (SA=Sarah)
 */
public class ICRequestI extends INBKAABase {
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

	private Ictltxn defICTLTXN = new Ictltxn();
	private Ictltxn oriICTLTXN = new Ictltxn();
	private final IctltxnExtMapper dbICTLTXN = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
	private final boolean isEC = false;
	private String W_UpdateOriIC;
	String W_TXRUST;
	
	public ICRequestI(FISCData txnData) throws Exception {
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
			//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
			String sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
			if("10".equals(sFiscRc.substring(0, 2))) {
				//程式結束
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
				return StringUtils.EMPTY;
			}
			
			//2.AddTxData:新增交易記錄( FEPTXN & FEPTXNTCB & ICTLTXN )
			rtnCode2 = this.addTxData();
			/* 2025/2/19 修改 for 晶片跨國沖正(2546/2572)查無原交易, 送IMS主機 */
			if(rtnCode2 != FEPReturnCode.Normal && ("N").equals(feptxn.getFeptxnCbsProc()) ) {
				getLogContext().setProgramName(ProgramName + ".addTxData");
				getLogContext().setMessage(rtnCode2.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	//程式結束
			}

			//3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = checkBusinessRule();
				if (("Y").equals(feptxn.getFeptxnCbsProc())) { //走3-1流程，程式結束
					return StringUtils.EMPTY;	//程式結束
				}
			}

			//4.SendToCBS: 帳務主機處理
			if(rtnCode == FEPReturnCode.Normal) {
				/*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
				則仍需組回應電文給財金 */
				rtnCode2 = this.sendToCBS();
				//程式結束
				if (feptxn.getFeptxnCbsTimeout() == 1) {
					/* 主機TimeOut 不組回應電文給財金, 程式結束 */
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
					getFiscBusiness().updateTxData();//更新交易記錄
					return StringUtils.EMPTY;
				}
			}
			
			//6.PrepareFISC:準備回財金的相關資料
			//6.1 判斷 rtnCode 是否 Normal
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext()));
				}	
			}else {
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);		 /*+REP*/
			}

			//6.2 產生 Response 電文Header:
			rtnCode2 = getFiscBusiness().prepareHeader("0210");
			if (rtnCode2 != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
			}
			
			//6.3 產生 Response 電文Body:
			String wk_BITMAP;
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// +REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {
				// -REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: // 交易金額
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
							break;
						case 5: // 代付單位 CD/ATM 代號
							getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
							break;
						case 6: // 可用餘額
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						case 14: // 跨行手續費
							getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
							break;
						case 21: // 促銷訊息
							// 240626 修改促銷訊息的抓取方式
							getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
							break;
					}
				}
			}

			//6.4 產生 MAC
			RefString refMac = new RefString(getFiscRes().getMAC());
			rtnCode2 = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFISCICMAC(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode2 != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
				getFiscRes().setMAC("00000000");
			}

			
			//6.5 產生Bit Map
			rtnCode2 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
			this.logContext.setRemark("after makeBitmap RC:" + rtnCode2.toString());
			logMessage(this.logContext);
			if (rtnCode2 != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode2.getValue());
				getFiscRes().setBitMapConfiguration("0000000000000000");
			}
			rtnCode2 = getFiscRes().makeFISCMsg();

			//7.UpdateTxData: 更新交易記錄(FEPTXN& ICTLTXN)
			rtnCode = this.updateTxData();
			//程式結束
			if(rtnCode != FEPReturnCode.Normal) {
				getLogContext().setRemark("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY;	
			}
			
			//8.ProcessAPTOT:更新跨行代收付
			rtnCode = this.processAPTOT();
			
			//9.SendToFISC送回覆電文到財金
			rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//10.判斷是否需傳送2160電文給財金
			/* 2025/7/1 修改 for 傳送 2160電文給財金 */
			/* "A" : 成功或失敗均需傳送 */
			if("A".equals(feptxn.getFeptxnSend2160())
					&& !"4001".equals( feptxn.getFeptxnRepRc()) ) {
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
		return "";
	}

		/**
	 * 新增交易記錄
	 * 
	 * @return FEPReturnCode
	 * ˇ
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode ;

		try {
			//2.1	Prepare() 交易記錄初始資料
			rtnCode = getFiscBusiness().prepareFEPTXN_IC();
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


			RefBase<Ictltxn> ictltxnRefBase = new RefBase<>(defICTLTXN);
			RefBase<Ictltxn> oriictltxnRefBase = new RefBase<>(oriICTLTXN);
			rtnCode = getFiscBusiness().prepareIctltxn(ictltxnRefBase, oriictltxnRefBase, MessageFlow.Request);
			defICTLTXN = ictltxnRefBase.get();
			oriICTLTXN = oriictltxnRefBase.get();
			if (("N").equals(feptxn.getFeptxnCbsProc())) { /* 2025/2/19 修改 for 晶片跨國沖正(2546/2572)查無原交易, 送IMS主機 */
				if (rtnCode != FEPReturnCode.Normal) {
					getLogContext().setProgramName(ProgramName + ".PrepareIctltxn");
					getLogContext().setRemark("PREPARE ICtltxn ERROR");
					sendEMS(getLogContext());
					return rtnCode;
				}
			}

			//2.2 以TRANSACTION 新增交易記錄
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

				if (("N").equals(feptxn.getFeptxnCbsProc())) { /* 2025/2/19 修改 for 晶片跨國沖正(2546/2572)查無原交易, 不寫ICTLTXN */
					if (dbICTLTXN.insertSelective(defICTLTXN) < 1) {
						rtnCode = IOReturnCode.UpdateFail;
						return rtnCode;
					}
				}

				// 2010-11-01 by kyo for Business 屬性為Assign因此造成例外
				// fiscBusiness.IntlTxn = defICTLTXN
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
	 * 商業邏輯檢核
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
//			//3.1 檢核特約商店代號for 2545,2546
//			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnMerchantId())) {
//				rtnCode = getFiscBusiness().checkMerchant();
//				if (rtnCode != FEPReturnCode.Normal) {
//					this.logContext.setMessage("checkMerchant error:" + rtnCode.toString());
//					logMessage(this.logContext);
//					return rtnCode;
//				}
//			}

			//3.1 檢核提款金額及單筆限額
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != FEPReturnCode.Normal) {
					// 20240701 增加checkTransLimit error log
					this.logContext.setRemark("checkTransLimit error:" + rtnCode.toString() + getFiscBusiness().getFeptxn().getFeptxnTxAmt() + "*" + getFiscBusiness().getFeptxn().getFeptxnExrate());
					this.logContext.setMessage("ICPCLimit:" + INBKConfig.getInstance().getICPCLimit());
					logMessage(this.logContext);
					return rtnCode;
				}
			}

			//3.2 檢核MAC
			// 24-05-20 改用CheckFISCICMAC
			rtnCode = encHelper.checkFISCICMAC(getFiscReq().getMessageType(), getFiscReq().getMAC());
			this.logContext.setRemark("after checkFiscICMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
			if (("Y").equals(feptxn.getFeptxnCbsProc())) {
				/* 2025/3/18 修改 for 晶片跨國沖正(2546/2572)驗MAC失敗, 3-1 需要用台幣押MAC*/
				feptxn.setFeptxnTxCur("TWD");
				feptxn.setFeptxnTxAmt(feptxn.getFeptxnTxAmtAct());
				/* 2025/2/19 修改 for 晶片跨國沖正(2546/2572)查無原交易, 送IMS主機 */
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
			}else{ /*FEPTXN_CBS_PROC = "N"*/
				if (rtnCode != FEPReturnCode.Normal) {
					return rtnCode;
				}
			}


			//3.3 檢核&更新原始交易狀態(for 2572/2546)
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				rtnCode = checkoriFEPTXN();
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
	 * 檢核和更新原始交易狀態
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String I_TX_DATE = "";
		try {
			/*FEPTXN_ORI_STAN   有值  */
			if ("2546".equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && oriICTLTXN != null && defICTLTXN != null){
				long oriDate = FormatUtil.parseDataTime(oriICTLTXN.getIctltxnTxDate(),FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).getTime();
				long txDate = FormatUtil.parseDataTime(defICTLTXN.getIctltxnTxDate(),FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).getTime();
				long daysDifference = (txDate - oriDate) / ( 1000 * 60 * 60 * 24 );
				if ( daysDifference > 90 ){
					rtnCode = FISCReturnCode.TransactionNotFound;		//4701:無此交易
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark("ICtltxn原交易 交易日> 90天");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
				W_UpdateOriIC = "Y";						// FEPTXN不存在時, 檢核ICTLTXN
				W_TXRUST = oriICTLTXN.getIctltxnTxrust();	// 保留原交易狀態

				/* 檢核原交易是否成功 -- 比對 ICTLTXN */
				if ( !("A").equals(W_TXRUST) && !("B").equals(W_TXRUST) ){ //交易成功
					rtnCode = FISCReturnCode.StatusNotMatch; // 8117:交易狀態有誤
					getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
					getLogContext().setRemark(StringUtils.join("原交易不成功, ICTLTXN_TXRUST=", W_TXRUST ));
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}

				/* 檢核原交易之 MAPPING 欄位是否相同--比對 ICTLTXN */
				if ( !defICTLTXN.getIctltxnBkno().equals(oriICTLTXN.getIctltxnBkno())
						|| !defICTLTXN.getIctltxnTxDatetimeFisc().equals(oriICTLTXN.getIctltxnTxDatetimeFisc())
						|| defICTLTXN.getIctltxnSetAmt().compareTo(oriICTLTXN.getIctltxnSetAmt()) != 0
						|| !defICTLTXN.getIctltxnAtmno().equals(oriICTLTXN.getIctltxnAtmno())
						|| !defICTLTXN.getIctltxnTroutActno().equals(oriICTLTXN.getIctltxnTroutActno()))
				{
					rtnCode = FISCReturnCode.OriginalMessageDataError; // FISC RC: MAPPING 欄位資料不符
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark(StringUtils.join("比對 ICTLTXN 與原交易欄位不相同, 原Bkno=", oriICTLTXN.getIctltxnBkno(),"|",defICTLTXN.getIctltxnBkno(),
									", 原Tx_Datetime_Fisc=", oriICTLTXN.getIctltxnTxDatetimeFisc(),"|",defICTLTXN.getIctltxnTxDatetimeFisc(),
									", 原Set_Amt=", oriICTLTXN.getIctltxnSetAmt(),"|",defICTLTXN.getIctltxnSetAmt(),
									", 原Atm_no=", oriICTLTXN.getIctltxnAtmno(),"|",defICTLTXN.getIctltxnAtmno(),
									", 原Trout_Actno=", oriICTLTXN.getIctltxnTroutActno(),"|",defICTLTXN.getIctltxnTroutActno()));
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
				oriICTLTXN.setIctltxnTxrust("T"); // 沖銷或授權完成進行中
				dbICTLTXN.updateByPrimaryKeySelective(oriICTLTXN);

				getFiscBusiness().getFeptxn().setFeptxnTroutActno(oriICTLTXN.getIctltxnTroutActno());
				getFiscBusiness().getFeptxn().setFeptxnDueDate(oriICTLTXN.getIctltxnTxDatetimeFisc().substring(0, 8));
			}else {
				feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
				getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
				getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
				getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
				// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
				getFiscBusiness().setOriginalFEPTxn(feptxnDao.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

				if (getFiscBusiness().getOriginalFEPTxn() == null) {
					I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8); // for 2572/2546

					getLogContext().setRemark(StringUtils.join("SearchFeptxn 以FEPTXN_TX_DATETIME_FISC[1,8]=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8), " FEPTXN_BKNO=",
							getFiscBusiness().getFeptxn().getFeptxnBkno(), " FEPTXN_ORI_STAN=", getFiscBusiness().getFeptxn().getFeptxnOriStan(), " 找原交易"));
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());

					rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
					if (rtnCode != FEPReturnCode.Normal) {
						rtnCode = FISCReturnCode.TransactionNotFound;
						getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
						getLogContext().setRemark("SearchFeptxn 找不到原交易");
						getLogContext().setProgramName(ProgramName);
						logMessage(Level.INFO, getLogContext());
						return rtnCode;
					} else {
						//將SearchFEPTXN 之資料存至 oriFEPTXN
						feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
					}
				}

				// 檢核原交易是否成功
				if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {
					rtnCode = FISCReturnCode.StatusNotMatch; // 交易狀態有誤
					getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
					getLogContext().setRemark(StringUtils.join("原交易不成功, FEPTXN_TXRUST=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()));
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}

				// 檢核原交易之 MAPPING 欄位是否相同
				if (!getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno().equals(getFiscBusiness().getFeptxn().getFeptxnDesBkno())
						//20240628 不判斷IcTac,Icmark from SPEC
						//					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().equals(getFiscBusiness().getFeptxn().getFeptxnIcTac())
						//					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().equals(getFiscBusiness().getFeptxn().getFeptxnIcmark())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().equals(getFiscBusiness().getFeptxn().getFeptxnMerchantId())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().equals(getFiscBusiness().getFeptxn().getFeptxnAtmType())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().equals(getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().equals(getFiscBusiness().getFeptxn().getFeptxnAtmChk())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().equals(getFiscBusiness().getFeptxn().getFeptxnIcSeqno())
						|| getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().doubleValue() != getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().doubleValue()
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno().trim())
						|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim())) {
					rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext()
							.setRemark(StringUtils.join("與原交易欄位不相同, DES_BKNO=", getFiscBusiness().getFeptxn().getFeptxnDesBkno(), " 原DES_BKNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno(),
									", TX_DATETIME_FISC=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc(), " 原TX_DATETIME_FISC=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc(),
									", TX_AMT_ACT=", getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString(), " 原TX_AMT_ACT=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().toString(),
									", ATMNO=", getFiscBusiness().getFeptxn().getFeptxnAtmno(), " 原ATMNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno(), ", TROUT_ACTNO=",
									getFiscBusiness().getFeptxn().getFeptxnTroutActno(), " 原TROUT_ACTNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno()));
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
				feptxnDao.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
				getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
				feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());

				getFiscBusiness().getFeptxn().setFeptxnTroutActno(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno());
				getFiscBusiness().getFeptxn().setFeptxnTroutKind(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutKind());
				getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().substring(0, 8));
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriFEPTXN"));
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
		Bsdays aBSDAYS = new Bsdays();
		BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
		String wk_TBSDY ;
		String wk_NBSDY = "";
		// Dim i As Int32
		stan ="0002357";
		try {
			feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
			getFiscBusiness().setOriginalFEPTxn(feptxnDao.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
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
					feptxnDao.setTableNameSuffix(wk_TBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness().setOriginalFEPTxn(feptxnDao.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							feptxnDao.setTableNameSuffix(wk_NBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness()
									.setOriginalFEPTxn(feptxnDao.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
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
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".searchOriginalFEPTxn"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 6. SendToCBS/ASC(if need): 帳務主機處理
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode sendToCBS() {
		String txType;
		try {
			if("2505".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())){
				txType="0";
			}else{
				txType="1";
			}
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			this.getTxData().setIctlTxn(defICTLTXN);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(txType);
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * UpdateTxData部份
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
				} else {// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");// 拒絕
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != FEPReturnCode.Normal) {// 若更新失敗則不送回應電文，人工處理
				return rtnCode;
			}

			// 判斷是否需更新 ICTLTXN

			// defICTLTXN.ICTLTXN_TX_CUR_ACT = fiscBusiness.FepTxn.FEPTXN_TX_CUR_ACT
			// defICTLTXN.ICTLTXN_TX_AMT_ACT = fiscBusiness.FepTxn.FEPTXN_TX_AMT_ACT
			// defICTLTXN.ICTLTXN_EXRATE = fiscBusiness.FepTxn.FEPTXN_EXRATE
			defICTLTXN.setIctltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
			defICTLTXN.setIctltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
			defICTLTXN.setIctltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
			defICTLTXN.setIctltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
			defICTLTXN.setIctltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				defICTLTXN.setIctltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			}
			if (dbICTLTXN.updateByPrimaryKeySelective(defICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
				rtnCode = IOReturnCode.UpdateFail;
				return rtnCode;
			}


			// (3) 判斷是否需更新原始交易 for 2572/2546
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				/* 2025/1/20 修改 for QRCODE 跨國消費扣款沖正交易 */
				if ( "Y".equals(W_UpdateOriIC)) {
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						oriICTLTXN.setIctltxnTxrust("D"); //已沖正成功
					}else{ //-REP
						oriICTLTXN.setIctltxnTxrust(W_TXRUST); //2025/1/20 修改 for QRCODE 跨國消費扣款沖正交易
					}
					if (dbICTLTXN.updateByPrimaryKeySelective(oriICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
						rtnCode = IOReturnCode.UpdateFail;
						return rtnCode;
					}
				}else{
					if (getFiscBusiness().getOriginalFEPTxn() != null) {
						feptxnDao.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData"));
						if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D");
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());

							// 國際卡交易需同時更新ICTLTXN
							oriICTLTXN.setIctltxnTxrust("D");
							if (dbICTLTXN.updateByPrimaryKeySelective(oriICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
								rtnCode = IOReturnCode.UpdateFail;
								return rtnCode;
							}

							if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
								// 若更新失敗則不送回應電文, 人工處理
								rtnCode = IOReturnCode.FEPTXNUpdateError;
								return rtnCode;
							}
						} else {// -REP
							// 授權交易需先上主機解圏, 若解圏成功則 TXRUST =“C”
							// 所以若TXRUST =“T”進行中, 即可將原交易之狀態改回 Active
							if ("T".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// 進行中for沖銷
								getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 將原始交易之狀態改為Active
								if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
									// 若更新失敗則不送回應電文，人工處理
									rtnCode = IOReturnCode.FEPTXNUpdateError;
									return rtnCode;
								}
							}
						}
					}
				}
			}
			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateTxData");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}finally {
			if ( !txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
		}
	}

	/**
	 * 更新跨行代收付
	 * 
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
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
