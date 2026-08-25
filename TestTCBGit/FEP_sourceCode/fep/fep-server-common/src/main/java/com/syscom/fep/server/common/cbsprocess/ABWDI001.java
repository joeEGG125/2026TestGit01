package com.syscom.fep.server.common.cbsprocess;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_WD_I001;
import com.syscom.fep.vo.text.ims.AB_WD_O001;

public class ABWDI001 extends ACBSAction {

	public ABWDI001(MessageBase txData) {
		super(txData, new AB_WD_O001());
	}

	ATMGeneralRequest atmReq = this.getAtmRequest();

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_WD_I001) */
		// Header
		AB_WD_I001 cbstita = new AB_WD_I001();
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));

		boolean isSysstatHbknoEqFeptxnTroutBkno = SysStatus.getPropertyValue().getSysstatHbkno()
				.equals(feptxn.getFeptxnTroutBkno());
		// TXN_FLOW
		if (feptxn.getFeptxnFiscFlag() == 0 ) {
			cbstita.setIMS_TRANS("MFEPAT00");
			cbstita.setTXN_FLOW("C"); // 自行
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
		} else {
			cbstita.setIMS_TRANS("MFEPAF00");
			cbstita.setTXN_FLOW("A"); // 代理
			if(StringUtils.equals(txType, "1")) {
				cbstita.setMSG_CAT("10");
			}else {
				cbstita.setMSG_CAT(atmReq.getMSGTYP());
			}
		}
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("ACCT");
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbstita.setTXNSTAN(feptxn.getFeptxnStan());
		cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
		cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
		cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
		
		switch (feptxn.getFeptxnTxCode()) {
			case "W3":
			case "W4":
				cbstita.setCARDTYPE("Q");  //QR-CODE
				break;
			case "WF":
			case "WP":
				cbstita.setCARDTYPE("X"); //指靜脈提款、全民普發現金
				break;
			default:
				cbstita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
				break;
		}
		if (isSysstatHbknoEqFeptxnTroutBkno || "WP".equals(feptxn.getFeptxnTxCode().trim())) {
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		} else {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		}
		//ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno());
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
		cbstita.setICMEMO(getAtmData().getTxRequestMessage().substring(446,506)); // IC卡備註欄
		if(feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())){
			cbstita.setTXNICCTAC(getAtmData().getTxRequestMessage().substring(758,778)); // LL+DATA
		}else{
			cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
		}
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		// PINBLOCK(W2、WF)，此欄位設定為不轉ASCII，需給值
		cbstita.setPINBLOCK(StringUtils.repeat("40",8));

		// 不同 FSCODE 的處理
		switch (feptxn.getFeptxnTxCode()) {
			case "W2":
				cbstita.setFROMACT(atmReq.getFADATA().substring(2, 18));
				//W2 特殊註記，Q1(用QRCODE被掃)；Q2(用QRCODE主掃)
				cbstita.setSPECIAL_FLAG(feptxn.getFeptxnActivityCode());
				//自行卡才放
				if(feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
						&& StringUtils.isNotBlank(feptxn.getFeptxnPinblock())) {
					cbstita.setPINBLOCK(feptxn.getFeptxnPinblock());
				}
				break;
			case "WF":
				//自行卡才放(WF目前是自行交易)
				cbstita.setCUSTOMER_ID(feptxn.getFeptxnIdno());
				break;
			case "WP":
				//全民普發現金(WP是自行交易)
				cbstita.setCUSTOMER_ID(feptxn.getFeptxnIdno());
				cbstita.setWP_2566_STAN (feptxntcb.getFeptxntcb2566Stan());
				cbstita.setWP_HEALTHCARD(feptxntcb.getFeptxntcbHealthcard());
				break;
			case "US":
			case "JP":
				 //匯率格式：X(8)
				cbstita.setEXCHANGE_RATE(StringUtils.substring(atmReq.getTADATA(), 0, 8));
				cbstita.setCUSTOMER_NATIONTYPE(StringUtils.substring(atmReq.getTADATA(), 10, 11)); // 身分別
				cbstita.setFCWDAMT(feptxn.getFeptxnTxAmt());
				break;
			default:
		}
		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	/**
	 * 拆解CBS回應電文
	 *
	 * @param cbsTota
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		/* 電文內容格式請參照TOTA電文格式(AB_WD_O001) */
		/* 拆解主機回應電文 */
		AB_WD_O001 tota = new AB_WD_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新交易 */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

	/**
	 * 更新交易
	 *
	 * @param cbsTota
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(AB_WD_O001 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		
		rtnCode = prepareUpdateIMSData(getTota());
    	if(rtnCode != FEPReturnCode.Normal) {
    		getLogContext().setMessage("after prepare Update IMS Data, RC:" + rtnCode.toString());
    		sendEMS(getLogContext());
    	}else if (!cbsTota.getIMSRC_TCB().equals("000") || 
				(!cbsTota.getIMSRC4_FISC().equals("4001") &&  !cbsTota.getIMSRC4_FISC().equals("4002")
				&& !cbsTota.getIMSRC4_FISC().equals("4007") && StringUtils.isNotBlank(cbsTota.getIMSRC4_FISC())) ) {
			rtnCode = FEPReturnCode.CBSCheckError;
		}
    	
    	//提款人ID，外幣提款US,JP使用, 水單報表使用
    	if(StringUtils.isNotBlank(cbsTota.getCID_NO())) {
    		feptxn.setFeptxnIdno(cbsTota.getCID_NO());
    	}
		
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        	int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);
        	
			if (insertCount <= 0 || insertCount2 <= 0) {
				rtnCode = FEPReturnCode.FEPTXNUpdateError;
				transactionManager.rollback(txStatus);
			}else {
				transactionManager.commit(txStatus);
			}
            
        } catch (Exception ex) { // 新增失敗
        	transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }
        return rtnCode;
	}

}
