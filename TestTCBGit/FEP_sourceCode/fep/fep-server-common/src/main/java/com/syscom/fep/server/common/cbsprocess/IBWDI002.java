package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.IB_WD_I002;
import com.syscom.fep.vo.text.ims.IB_WD_O002;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Calendar;
import java.util.Objects;

public class IBWDI002 extends ACBSAction{
	
	public IBWDI002(MessageBase txData) {
		super(txData, new IB_WD_O002());
	}

	/**
	 * 組送CBS 主機原存提款Confirm交易電文
	 * 
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		//組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格-原存行提款交易V1.0(111xxxx).doc
		IB_WD_I002 cbsTita = new IB_WD_I002();
		// HEADER
		cbsTita.setIMS_TRANS("MFEPFWD0");// 主機業務別 長度8 2024/12/16 配合主機修改
		cbsTita.setSYSCODE("FEP");// 處理系統代號 長度4
		cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnTraceEjfno()),8,"0")); // FEP電子日誌序號 長度8
		cbsTita.setTXN_FLOW("I");// 交易分類 長度1
		cbsTita.setMSG_CAT("02");// 電文訊息來源類別 長度2
		cbsTita.setSOURCE_CHANNEL("FIS");// CHANNEL或業務別 長度3
		cbsTita.setPCODE(this.feptxn.getFeptxnPcode());// 財金P-CODE 長度4
		cbsTita.setFSCODE(" ");// 合庫FS-CODE 長度2 原存行交易放空白
		cbsTita.setPROCESS_TYPE("RVS");//交易處理類別 長度4
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbsTita.setACQUIRER_BANK(this.feptxn.getFeptxnBkno());// 設備代理行 長度3
		cbsTita.setTXNSTAN(this.feptxn.getFeptxnStan());// 跨行交易序號 長度7
		cbsTita.setTERMINALID(this.feptxn.getFeptxnAtmno());// 端末機代號 長度8
		cbsTita.setTERMINAL_TYPE(this.feptxn.getFeptxnAtmType());// 端末設備型態 長度4
		cbsTita.setCARDISSUE_BANK(this.feptxn.getFeptxnTroutBkno());// 發卡行/扣款行 長度3
		String atmType;
		if("6071".equals(this.feptxn.getFeptxnAtmType())){
			atmType = "X"; /* 無卡提款 */
		}else{
			atmType = "K"; /* 晶片卡 */
		}
		cbsTita.setCARDTYPE(atmType);// 交易卡片型態 cbsTita.setRESPONSE_CODE(this.feptxn.getFeptxnConRc());// 回應代號(RC) 長度4
		cbsTita.setRESPONSE_CODE(this.feptxn.getFeptxnConRc());// 回應代號(RC) 長度4
		cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
		cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
		// CBS Request DETAIL
		cbsTita.setICCHIPSTAN(StringUtils.repeat('0', 8)); // IC卡交易序號
		cbsTita.setTERM_CHECKNO(StringUtils.repeat(' ', 8)); // 端末設備查核碼
		cbsTita.setTERMTXN_DATETIME(StringUtils.repeat('0', 14)); // 交易日期時間
		cbsTita.setICMEMO(StringUtils.repeat(' ', 30)); // IC卡備註欄
		cbsTita.setTXNICCTAC(StringUtils.repeat(' ', 10)); // 交易驗證碼
		cbsTita.setFROMACT(this.feptxn.getFeptxnTroutActno());// 卡片提款帳號(無卡提款序號)
		cbsTita.setTXNAMT(this.feptxn.getFeptxnTxAmt());// 交易金額
		cbsTita.setPINBLOCK(StringUtils.repeat(' ', 8));// 無卡提款密碼 8個空白
		cbsTita.setDRVS(StringUtils.repeat(' ', 372)); // 保留欄位
		this.setoTita(cbsTita);
		this.setTitaToString(cbsTita.makeMessage());
		this.setASCIItitaToString(cbsTita.makeMessageAscii());
		return FEPReturnCode.Normal;		
	}
	
	/**
	 * 拆解CBS  TOTA電文 、更新feptxn
	 * @param cbsTota
	 * @return
	 */
	public FEPReturnCode processCbsTota(String cbsTota,String type) throws Exception {
		IB_WD_O002 tota = (IB_WD_O002) this.getTota();//取得tota物件
		tota.parseCbsTele(cbsTota);//拆解CBS  TOTA電文
		this.setTota(tota);//塞入拆解後的tota讓AA取得
		
		/* 更新FEPTXN */
		FEPReturnCode rtnCode = this.updateFepTxn(tota, type);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}
	
	/**
	 * 更新Feptxn
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public FEPReturnCode updateFepTxn(IB_WD_O002 cbsTota,String type) throws Exception {
		FEPReturnCode rtnCode;
		//變更交易記錄
		this.feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
		this.feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
		this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0,1) + "4");
		this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
		// IMSRC_TCB = "000" or empty表交易成功
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		/* 2024/7/1 更新 FEPTXNTCB */
		rtnCode = this.UpdateFEPTXNTCB(cbsTota);
		if (!cbsTota.getIMSRC_TCB().equals("000")) {
			feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			/* CBS回覆成功 */
			if ("Y".equals(cbsTota.getIMSRVS_FLAG())) { // 主機記帳狀況
				feptxn.setFeptxnAccType((short) 2);  //已更正
			} else if("N".equals(cbsTota.getIMSRVS_FLAG())){
				feptxn.setFeptxnAccType((short) 3); /*更正/轉入失敗*/
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
