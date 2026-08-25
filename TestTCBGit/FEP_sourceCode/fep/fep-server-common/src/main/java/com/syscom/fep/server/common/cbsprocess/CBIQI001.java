package com.syscom.fep.server.common.cbsprocess;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.CB_IQ_I001;
import com.syscom.fep.vo.text.ims.CB_IQ_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;

public class CBIQI001 extends ACBSAction {

	public CBIQI001(MessageBase txType) {
		super(txType, new CB_IQ_O001());
	}

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_IQ_I001) */
		// Header
		CB_IQ_I001 cbsTita = new CB_IQ_I001();
		if ("EAT".equals(feptxn.getFeptxnChannel())) {
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq = this.getEATMRequest().getBody().getRq().getSvcRq();
			cbsTita.setMSG_CAT(atmReq.getMSGTYP());
			cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
			cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄(未轉ASC，為原始電文)
			cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); // TAC(未轉ASC，為原始電文值)
		} else {
			ATMGeneralRequest atmReq = this.getAtmRequest();
			cbsTita.setMSG_CAT(atmReq.getMSGTYP());
			cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
			cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄(未轉ASC，為原始電文)
			cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); // TAC(未轉ASC，為原始電文值)
		}
		cbsTita.setIMS_TRANS("MFEPAT00");
		cbsTita.setSYSCODE("FEP");
		cbsTita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
		cbsTita.setTXN_FLOW("C"); // 自行
		cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbsTita.setPCODE(feptxn.getFeptxnPcode());
		cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbsTita.setPROCESS_TYPE("CHK");
		// 財金營業日(西元年須轉民國年)
		String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
		if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			feptxnTbsdyFisc = "000000";
		} else {
			feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
		}
		cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
		cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
		cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
		cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
		cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
		cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
		cbsTita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno());
		// Detail
		cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片帳號
		// I2(同ID其他帳戶查詢餘額)、I4(金融卡存款前置查詢的存入帳號)
		if ("I2".equals(feptxn.getFeptxnTxCode()) || "I4".equals(feptxn.getFeptxnTxCode())) {
			cbsTita.setINQELSEACT(feptxn.getFeptxnTxActno());
			// I4交易要提供存款之存入銀行
			if ("I4".equals(feptxn.getFeptxnTxCode())) {
				cbsTita.setI4_CDMBANK(feptxn.getFeptxnTrinBkno());
			}
		}
		this.setoTita(cbsTita);
		this.setTitaToString(cbsTita.makeMessage());
		this.setASCIItitaToString(cbsTita.makeMessageAscii());

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
		/* 電文內容格式請參照TOTA電文格式(AB_IQ_O001) */
		/* 拆解主機回應電文 */
		CB_IQ_O001 tota = new CB_IQ_O001();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);

		/* 更新交易 */
		FEPReturnCode rtnCode = this.updateFEPTxn(tota, cbsTota);

		/* 回覆FEP */
		// 處理 CBS 回應
		return rtnCode;
	}

    /**
     * 更新交易
     * @param cbsTota
     * @param totaStr 原始下送電文
     * @return FEPReturnCode
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(CB_IQ_O001 cbsTota, String cbsTotaStr) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
    	rtnCode = prepareUpdateIMSData(getTota());
    	if(rtnCode != FEPReturnCode.Normal) {
    		getLogContext().setMessage("after prepare Update IMS Data, RC:" + rtnCode.toString());
    		sendEMS(getLogContext());
    	}else if (!cbsTota.getIMSRC_TCB().equals("000")) {
            rtnCode = FEPReturnCode.CBSCheckError;
        }
		
    	//處理存款帳號名稱中文轉換
    	if(StringUtils.isNotBlank(cbsTotaStr)) {
    		String EBCDICACTNAME = cbsTotaStr.substring(562, 602); //CBS_TOTA[563：40]   //取EBCDIC 值
        	String ACTNAME = EbcdicConverter.changeChinese(EBCDICACTNAME);
        	if(StringUtils.isNotBlank(ACTNAME)) {
        		feptxntcb.setFeptxntcbActname(StringUtils.trim(ACTNAME));
        	}
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
