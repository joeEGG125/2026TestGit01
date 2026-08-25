package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_XI_I002;
import com.syscom.fep.vo.text.ims.IB_XI_O002;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存轉帳Confirm交易電文
 * 
 * @author vincent
 *
 */

public class IBXII002 extends ACBSAction {

	public IBXII002(MessageBase txData) {
		super(txData, new IB_XI_I002());
	}

	/**
	 * 組CBS 原存交易Request電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		//組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格-原存行轉帳交易V1.0(111xxxx).doc
		IB_XI_I002 cbsTita = new IB_XI_I002();
		// HEADER
		cbsTita.setIMS_TRANS("MFEPXML0"); // 主機業務別 長度8 2026/7/16 配合主機修改
		cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
		cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		if (StringUtils.isNotBlank(feptxn.getFeptxnConRc())) {
			cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnTraceEjfno()),8,"0")); // FEP電子日誌序號 長度8
		}else {
			cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()),8,"0")); // FEP電子日誌序號 長度8
		}
		cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
		/* 2026/7/16 修改 */
		if (StringUtils.isNotBlank(feptxn.getFeptxnConRc())) {
			cbsTita.setMSG_CAT("02"); // 電文訊息來源類別 長度2 ,FISC-0202
		}
		if (StringUtils.equals("4001",feptxn.getFeptxnConExcpCode())) {
			cbsTita.setMSG_CAT("09"); // 電文訊息來源類別 長度2 ,人工確認,發送入帳
		}
		cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
		cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
		cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
		cbsTita.setPROCESS_TYPE("ACCT"); //帳務入賬

		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno()); // 設備代理行 長度3
		cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7
		cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());// 端末機代號 長度8
		cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType()); // 端末設備型態 長度4
		cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
		cbsTita.setCARDTYPE("X"); // 交易卡片型態 長度1 /* X:未使用卡片 */
		cbsTita.setRESPONSE_CODE(feptxn.getFeptxnConRc()); // 回應代號(RC) 長度4
		cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
		cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改

		/* CBS Request DETAIL */
		cbsTita.setICCHIPSTAN(StringUtils.repeat('0', 8)); // IC卡交易序號
		cbsTita.setTERM_CHECKNO(StringUtils.repeat(' ', 8)); // 端末設備查核碼
		cbsTita.setTERMTXN_DATETIME(StringUtils.repeat('0', 14)); // 交易日期時間
		cbsTita.setICMEMO(StringUtils.repeat(' ', 30)); // IC卡備註欄
		cbsTita.setTXNICCTAC(StringUtils.repeat(' ', 10)); // 交易驗證碼
		cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
		cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(轉出帳號)
		// 20221006 改用財金INBK Requst電文
		FISC_INBK reqINBK = this.getInbkRequest();
		cbsTita.setTransferee_Bank_ID(feptxn.getFeptxnTrinBkno7()); // 轉入行庫
		cbsTita.setTransferor_Bank_ID(feptxn.getFeptxnTroutBkno7()); // 轉出行庫
		cbsTita.setTOACT(feptxn.getFeptxnTrinActno()); // 轉入帳號
		cbsTita.setTR_SPECIAL_FLAG(StringUtils.repeat(" ",2)); // 特殊轉帳類別註記 // 二個空白


		/* 收款人姓名中文轉碼處理 */
		getLogContext().setProgramName(ProgramName + ".getCbsTita");
		getLogContext().setRemark("FeptxnToName初始 >"+ feptxn.getFeptxnToName() +"<");
		logMessage(getLogContext());
		if(StringUtils.isNotBlank(feptxn.getFeptxnToName())) {
			/* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
			cbsTita.setTONAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnToName(),160) );
		}else{
			cbsTita.setTONAME(StringUtils.rightPad("", 160, "40"));
		}
		this.logContext.setRemark("TONAME	>" + cbsTita.getTONAME() + "<");
		logMessage(this.logContext);

		/* 付款人姓名中文轉碼處理 */
		getLogContext().setProgramName(ProgramName + ".getCbsTita");
		getLogContext().setRemark("FeptxnFromName初始 >"+ feptxn.getFeptxnFromName() +"<");
		logMessage(getLogContext());
		if(StringUtils.isNotBlank(feptxn.getFeptxnFromName())) {
			/* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
			cbsTita.setFROMNAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnFromName(),160) );
		}else{
			cbsTita.setFROMNAME(StringUtils.rightPad("", 160, "40"));
		}
		this.logContext.setRemark("FROMNAME	>" + cbsTita.getFROMNAME() + "<");
		logMessage(this.logContext);

		/* FXML附言欄中文轉碼處理 */
		getLogContext().setProgramName(ProgramName + ".getCbsTita");
		getLogContext().setRemark("FeptxnFxmlMemo初始 >"+ feptxn.getFeptxnFxmlMemo() +"<");
		logMessage(getLogContext());
		if(StringUtils.isNotBlank(feptxn.getFeptxnFxmlMemo())) {
			/* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
			cbsTita.setFXMLMEMO( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnFxmlMemo(),160) );
		}else{
			cbsTita.setFXMLMEMO(StringUtils.rightPad("", 160, "40"));
		}
		this.logContext.setRemark("FXMLMEMO	>" + cbsTita.getFXMLMEMO() + "<");
		logMessage(this.logContext);

		cbsTita.setFROMCID(feptxn.getFeptxnIdno()); //財金Req電文BITMAP #42
		cbsTita.setTOCID(feptxn.getFeptxnToIdno()); //財金Req電文BITMAP #47
		cbsTita.setDRVS(StringUtils.repeat(' ', 86)); // 保留欄位
		this.setoTita(cbsTita);
		this.setTitaToString(cbsTita.makeMessage());
		this.setASCIItitaToString(cbsTita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	/**
	 * 拆解CBS TOTA電文
	 * 
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		/* 電文內容格式請參照TOTA電文格式(IB_TR_O012) */
		/* 拆解主機回應電文 */
		IB_XI_O002 tota = new IB_XI_O002();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新FEPTXN */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(IB_XI_O002 cbsTota, String type) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
		feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
		// IMSRC_TCB = "000" or empty表交易成功
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		/* 2024/7/1 更新 FEPTXNTCB */
		rtnCode = this.UpdateFEPTXNTCB(cbsTota);
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			if(type.equals("2")){
				feptxn.setFeptxnAccType((short)3); /*更正/轉入失敗*/
			}else{
				feptxn.setFeptxnAccType((short)0); /*未記帳*/
			}
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			/* CBS回覆成功 */
			if(type.equals("2")){
				if ("Y".equals(cbsTota.getIMSRVS_FLAG())) {		//主機記帳狀況
					feptxn.setFeptxnAccType((short) 2);	//已更正
				} else if("N".equals(cbsTota.getIMSRVS_FLAG())){
					feptxn.setFeptxnAccType((short) 3);	//更正/轉入失敗
				}
			} else if (StringUtils.equalsAny(type, "1", "6")) {
				if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {	//主機記帳狀況
					feptxn.setFeptxnAccType((short) 1);	//已記帳
				} else if("N".equals(cbsTota.getIMSACCT_FLAG())){
					feptxn.setFeptxnAccType((short) 0);	//未記帳
				}
			}
			rtnCode = FEPReturnCode.Normal;
		}
		// 兩個Table 同步更新，如有任何錯誤，請ROLLBACK & 寫EMS
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
			int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

			if (insertCount <= 0 || insertCount2 <= 0) {
				throw new Exception();
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
			sendEMS(getLogContext());
			rtnCode = FEPReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}
}
