package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_WW_I002;
import com.syscom.fep.vo.text.ims.AB_WW_O002;

public class ABWWI002 extends ACBSAction {

	public ABWWI002(MessageBase txData) {
		super(txData, new AB_WW_O002());
	}

	private ATMGeneralRequest atmReq = this.getAtmRequest();

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_WW_I002) */
		// Header
		AB_WW_I002 cbstita = new AB_WW_I002();
		cbstita.setIMS_TRANS("MFEPAF00"); // ATM跨國交易
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()),8,"0"));
		cbstita.setTXN_FLOW("A"); // 合庫代理
		cbstita.setMSG_CAT(atmReq.getMSGTYP());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		cbstita.setPROCESS_TYPE("WDE");
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
		//發行卡
		if("260".equals(feptxn.getFeptxnPcode().substring(0,3)) || "264".equals(feptxn.getFeptxnPcode().substring(0,3))){ //260X 銀聯,264X JCB
			cbstita.setCARDISSUE_BANK("944");
		}
		else if("262".equals(feptxn.getFeptxnPcode().substring(0,3))){ //262X PLUS
			cbstita.setCARDISSUE_BANK("947");
		}
		else if("263".equals(feptxn.getFeptxnPcode().substring(0,3))){ //263X Cirrus
			cbstita.setCARDISSUE_BANK("946");
		}
		cbstita.setCARDTYPE(atmReq.getCARDFMT()); // 交易卡片型態
		cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		// Detail
		cbstita.setEMVCARD(feptxntcb.getFeptxntcbEmvcard());  //國際卡卡號
		cbstita.setATMSTATUS_CODE(feptxn.getFeptxnConExcpCode());
		cbstita.setRVSTXAMT("+" + StringUtils.leftPad(StringUtils.substring(atmReq.getSTATUS(), 2, 12), 13, "0")); // 沖正金額
		cbstita.setEMVARPCRC(atmReq.getPIARPCRC()); //晶片處理結果

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
		/* 電文內容格式請參照TOTA電文格式(AB_WW_O002) */
		/* 拆解主機回應電文 */
		AB_WW_O002 tota = new AB_WW_O002();
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
	private FEPReturnCode updateFEPTxn(AB_WW_O002 cbsTota) throws Exception {
		FEPReturnCode rtnCode;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "4");
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
			/* 2025/12/15 修正 */
//			feptxn.setFeptxnAccType((short) 3); //更正失敗
			rtnCode = FEPReturnCode.CBSCheckError;
		}
		
		// 晶片處理結果，ATM Confirm 上送，Request沒存到故補存
		feptxntcb.setFeptxntcbPiarpcrc(atmReq.getPIARPCRC());
		
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
