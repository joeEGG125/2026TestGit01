package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.IB_OE_I012;
import com.syscom.fep.vo.text.ims.IB_OE_O012;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存跨境電子支付Confirm交易電文
 * 
 * @author Joseph
 *
 */

public class IBOEI012 extends ACBSAction {

	public IBOEI012(MessageBase txData) {
		super(txData, new IB_OE_I012());
	}

//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
	Vatxn vatxn=new Vatxn(); //暫時使用

	/**
	 * 組CBS 原存交易Request電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		//組CBS 原存交易Request電文, 電文內容格式請參照: D_I8_合庫FEP_主機電文規格-原存行跨境電子支付交易V1.0(111xxxx).doc
		IB_OE_I012 cbsTita = new IB_OE_I012();
		// HEADER
		cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
		cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
		cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnTraceEjfno()),8,"0")); // FEP電子日誌序號 長度8
		cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
		cbsTita.setMSG_CAT("02");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給02 */
		cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
		cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
		cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
		if("1".equals(txType)){
			cbsTita.setPROCESS_TYPE("ACCT"); /* 帳務 */
		}else{
			cbsTita.setPROCESS_TYPE("RVS"); /* 沖正 */
		}
//		else if("2".equals(txType)){
//			cbsTita.setPROCESS_TYPE("RVS"); /* 沖正 */
//		}
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
		cbsTita.setCARDTYPE("K"); // 交易卡片型態 長度1 /* K:晶片卡 */
		cbsTita.setRESPONSE_CODE(feptxn.getFeptxnConRc()); // 回應代號(RC) 長度4
		cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
		cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
		/* CBS Request DETAIL */
		cbsTita.setICCHIPSTAN(StringUtils.repeat("0",8)); // IC卡交易序號
		cbsTita.setTERM_CHECKNO(StringUtils.repeat(" ",8)); // 端末設備查核碼
		cbsTita.setTERMTXN_DATETIME(StringUtils.repeat("0",14)); // 交易日期時間
		cbsTita.setICMEMO(StringUtils.repeat(" ",30)); // IC卡備註欄
		cbsTita.setTXNICCTAC(StringUtils.repeat(" ",10)); // 交易驗證碼
		cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號
		cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
		Obtltxn obtltxn = this.getFiscData().getObtlTxn();
		if("1".equals(txType)){ // 退款要入帳
			cbsTita.setBIT36_EMERCHID(obtltxn.getObtltxnMerchantId()); // 特店代號
			cbsTita.setBIT36_OENTDAMT(obtltxn.getObtltxnTotTwdAmt()); // 消費者台幣支付金額
			cbsTita.setBIT36_OETWNFEE(obtltxn.getObtltxnTwnFee()); // 台方手續費
			cbsTita.setBIT36_OGSLCRT(obtltxn.getObtltxnSetExrate()); // 結算匯率
			cbsTita.setBIT36_TMSTAN1(obtltxn.getObtltxnOriStan()); // 原交易序號
			cbsTita.setBIT36_OENTAFEE(obtltxn.getObtltxnTotTwdFee()); // 台幣總手續費
			cbsTita.setBIT36_OGTXAMT(obtltxn.getObtltxnTotForAmt()); // 外幣總金額
			cbsTita.setBIT36_OEFCAFEE(obtltxn.getObtltxnTotForFee()); // 外幣總手續費
		}
		else{
			cbsTita.setBIT36_EMERCHID(StringUtils.repeat(" ",15)); // 特店代號
			cbsTita.setBIT36_OENTDAMT(BigDecimal.ZERO); // 消費者台幣支付金額
			cbsTita.setBIT36_OETWNFEE(BigDecimal.ZERO); // 台方手續費
			cbsTita.setBIT36_OGSLCRT(StringUtils.repeat("0",8)); // 結算匯率
			cbsTita.setBIT36_TMSTAN1(StringUtils.repeat(" ",10)); // 原交易序號
			cbsTita.setBIT36_OENTAFEE(BigDecimal.ZERO); // 台幣總手續費
			cbsTita.setBIT36_OGTXAMT(BigDecimal.ZERO); // 外幣總金額
			cbsTita.setBIT36_OEFCAFEE(BigDecimal.ZERO); // 外幣總手續費
		}
		cbsTita.setDRVS(StringUtils.repeat(" ", 300)); // 保留欄位
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
		/* 電文內容格式請參照TOTA電文格式(IB_OE_O012) */
		/* 拆解主機回應電文 */
		IB_OE_O012 tota = new IB_OE_O012();
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
	private FEPReturnCode updateFEPTxn(IB_OE_O012 cbsTota, String type) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
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
			if(type.equals("2")){ /* 沖正 */
				feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/
			}else if (type.equals("1")){ /* 入帳 */
				feptxn.setFeptxnAccType((short)0); /*未記帳*/
			}
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			/* CBS回覆成功 */
			if(type.equals("2")){ /* 沖正 */
				if ("Y".equals(cbsTota.getIMSRVS_FLAG())) { // 主機記帳狀況
					feptxn.setFeptxnAccType((short) 2);  //已更正
				} else if ("N".equals(cbsTota.getIMSRVS_FLAG())) {
					feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/
				}
			}else if (type.equals("1")){ /* 入帳 */
				if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
					feptxn.setFeptxnAccType((short) 1);  //已記帳
				} else if ("N".equals(cbsTota.getIMSACCT_FLAG())) {
					feptxn.setFeptxnAccType((short) 0); /*未記帳*/
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
