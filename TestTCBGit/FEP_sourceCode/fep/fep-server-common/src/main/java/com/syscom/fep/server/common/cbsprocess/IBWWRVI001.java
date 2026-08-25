package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.text.ims.IB_WW_RV_I001;
import com.syscom.fep.vo.text.ims.IB_WW_RV_O001;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存行國際卡沖銷Request交易電文
 * 
 * @author Joseph
 *
 */

public class IBWWRVI001 extends ACBSAction {

	public IBWWRVI001(MessageBase txData) {
		super(txData, new IB_WW_RV_O001());
	}

//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);

	/**
	 * 組CBS 原存交易Request電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		//組CBS 原存交易Request電文, 電文內容格式請參照: D_I9_T2_合庫FEP_主機電文規格-原存行-跨國沖銷交易V1.0(1111114).doc
		IB_WW_RV_I001 cbsTita = new IB_WW_RV_I001();
		Intltxn intltxn = this.getFiscData().getIntlTxn();
		// HEADER
		cbsTita.setIMS_TRANS("MFEPFWD0"); // 主機業務別 長度8 2024/12/16 配合主機修改
		cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
		cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()),8,"0")); // FEP電子日誌序號 長度8
		cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
		cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給0O */
		cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
		cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
		cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
		cbsTita.setPROCESS_TYPE("ACCT"); /* 沖正 */
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
		cbsTita.setTERMINAL_TYPE(intltxn.getIntltxnPosMode()); // 端末設備型態 長度4
		cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
		cbsTita.setCARDTYPE("2"); // 交易卡片型態 長度1 /* 跨國磁條24XX */
		cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
		cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
		cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
		/* CBS Request DETAIL */
		cbsTita.setICCHIPSTAN(StringUtils.repeat("0",1)); // IC卡交易序號
		cbsTita.setTERM_CHECKNO(StringUtils.repeat(" ",8)); // 端末設備查核碼
		cbsTita.setTERMTXN_DATETIME(StringUtils.repeat("0",14)); // 交易日期時間
		cbsTita.setICMEMO(StringUtils.repeat(" ",30)); // IC卡備註欄
		cbsTita.setTXNICCTAC(StringUtils.repeat(" ",10)); // 交易驗證碼
//		cbsTita.setTXNAMT(BigDecimal.ZERO); // 交易金額
		/* 2024/8/23 修改 */
		cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());		//卡片提款帳號
		/* 2025/4/18將轉出帳號寫入卡片帳號 */
		feptxn.setFeptxnMajorActno(feptxn.getFeptxnTroutActno());
		cbsTita.setTRK2(feptxn.getFeptxnTrk2()); // 磁軌資料
		cbsTita.setPINBLOCK(StringUtils.repeat("40",8)); // 提款密碼 不需轉ASCII 8*2
		/* 2024/8/21 修改 */
		cbsTita.setBIT36_OGSLAMT(intltxn.getIntltxnSetAmt()); // 清算金額
		cbsTita.setBIT36_OGTXAMT(new BigDecimal(this.getInbkRequest().getOriData().substring(42,54))); // 原始交易金額
		cbsTita.setBIT36_OGTXCCD(this.getInbkRequest().getOriData().substring(66,69)); // 原始交易幣別碼
		cbsTita.setBIT36_OGSLCCD(this.getInbkRequest().getOriData().substring(69,72)); // 清算幣別碼
		cbsTita.setBIT36_OGSLCRT(this.getInbkRequest().getOriData().substring(72,80)); // 清算匯率
		cbsTita.setBIT36_RPSLAMT(new BigDecimal(this.getInbkRequest().getOriData().substring(148,160))); // 實際完成之交易清算金額
		cbsTita.setBIT36_TMSTAN1(this.getInbkRequest().getOriData().substring(160,168)); //原提款之交易序號
		/* 2025/2/12 修改 for 跨國提款沖銷交易(2430/2470) */
		cbsTita.setTXNAMT(feptxn.getFeptxnTxAmtAct()); // 交易金額
		cbsTita.setBIT36_OGSLAMT(feptxn.getFeptxnTxAmtSet()); // 清算金額
		cbsTita.setORG_B006RATE(String.format("%07d", feptxn.getFeptxnExrate().movePointRight(4).intValue())); // 原提款交易「合庫美元匯率」,無小數點、左補零至 7 碼
		cbsTita.setORG_RVTXNUSE(feptxn.getFeptxnMerchantId()); // 原提款交易「沖銷使用欄位」
		/* 2024/8/19  修改 for  跨國提款沖銷交易(2430/2470) */
		if(StringUtils.isNumeric(feptxn.getFeptxnTrk3())) {
			Feptxntcb orifeptxntcb = new Feptxntcb();
			orifeptxntcb = feptxnDao.selectByPrimaryKeyForFeptxntcb(feptxn.getFeptxnDueDate(), Integer.valueOf(feptxn.getFeptxnTrk3()));
			if (orifeptxntcb != null) {
				/* 2024/8/19 修改, 將原跨國提款交易主機下送的告誡註記, 上送給主機 */
				//
				if (StringUtils.isNotBlank(orifeptxntcb.getFeptxntcbCiwaflg())) {
					cbsTita.setORG_CIWAFLG(orifeptxntcb.getFeptxntcbCiwaflg());  //告誡註記 有值 → 原值
				} else {
					cbsTita.setORG_CIWAFLG(" ");  // 無值 → 一個空白字元
				}
			}
		}
		cbsTita.setDRVS(StringUtils.repeat(" ", 172)); // 保留欄位
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
		/* 電文內容格式請參照TOTA電文格式(IB_WW_RV_O001) */
		/* 拆解主機回應電文 */
		IB_WW_RV_O001 tota = new IB_WW_RV_O001();
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
	private FEPReturnCode updateFEPTxn(IB_WW_RV_O001 cbsTota, String type) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
		feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
		feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
		feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */
		feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
		/* 2025/1/17 新增, 寫入以下欄位 */
		feptxn.setFeptxnTxCurAct(CurrencyType.TWD.name());
		feptxn.setFeptxnTxAmtAct(cbsTota.getTXNAMT()); /* 實際扣客戶金額(轉台幣) */
		feptxn.setFeptxnFeeCur(CurrencyType.TWD.name());
		feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 跨國提款-客戶手續費 */
		feptxn.setFeptxnExrate(cbsTota.getB006RATE()); /* 合庫美元匯率 */
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
		// IMSRC_TCB = "000" or empty表交易成功
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		/* 2024/7/1 更新 FEPTXNTCB */
		rtnCode = this.UpdateFEPTXNTCB(cbsTota);
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnAccType((short) 0); //未記帳
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			/* CBS回覆成功 */
			/* 主機回傳民國年須轉成西元年 */
			String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
			if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
					|| (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
				imsbusinessDate = "00000000";
			} else {
				imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
			}
			feptxn.setFeptxnTbsdy(imsbusinessDate);
			/* 2022/11/21 寫入帳務分行 */
			feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
			// 記帳類別
			if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
				feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
			} else {
				feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
			}
			if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
				feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
				/* 寫入簡訊資料檔 */
				this.insertSMSMSG(cbsTota);
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

	/**
	 * 新增交易通知
	 * 
	 * @param cbsTota
	 * @throws ParseException
	 */
	private void insertSMSMSG(IB_WW_RV_O001 cbsTota) throws ParseException {
//		String txDate = feptxn.getFeptxnTxDate();
//		Integer ejfno = feptxn.getFeptxnEjfno();
//		Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//		// 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//		// 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//		if (smsmsg == null) {
//			smsmsg = new Smsmsg();
//			smsmsg.setSmsmsgTxDate(txDate);
//			smsmsg.setSmsmsgEjfno(ejfno);
//			smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//			smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//			smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//			smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//			smsmsg.setSmsmsgZone("TWN");
//			smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//			smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//			smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//			smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//			smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//			smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//			smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//			smsmsg.setSmsmsgNotifyFg("Y");
//			smsmsg.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//			smsmsg.setSmsmsgChannel(feptxn.getFeptxnChannel());
//			Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(
//					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//			smsmsg.setUpdateTime(datenow);
//			smsmsg.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//			if (smsmsgExtMapper.insert(smsmsg) <= 0) {
//				getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//				this.logMessage(getLogContext());
//			}
//		}
	}
}
