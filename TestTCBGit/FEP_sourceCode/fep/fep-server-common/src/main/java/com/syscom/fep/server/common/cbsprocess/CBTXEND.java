package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.CB_TXEND;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS主機舊卡啟用新卡Request交易電文
 * 
 * @author Joseph
 *
 */

public class CBTXEND extends ACBSAction {

	public CBTXEND(MessageBase txData) {
		super(txData, new CB_TXEND());
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
		/* TITA 請參考合庫主機電文規格(CB_TXEND) */
		// Header
		CB_TXEND cbstita = new CB_TXEND();
		cbstita.setIMS_TRANS("MFEPMS00");
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		if(feptxn.getFeptxnTraceEjfno() != null) {
			cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()), 8, "0"));
		}else {
			cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
		}
		cbstita.setMSG_CAT("AF");
		if(feptxn.getFeptxnChannel().length() > 3){
			cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel().substring(0,3));
		}else{
			cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		}
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		if(StringUtils.isNotBlank(feptxn.getFeptxnTxCode())){
			cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		}
		cbstita.setPROCESS_TYPE("END"); //交易結束
		cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
		cbstita.setTXNSTAN(feptxn.getFeptxnStan());
		// 回應代碼(RC)
		if (StringUtils.isNotBlank(feptxn.getFeptxnConRc())) {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnConRc());
		}
		else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		}
		else if (StringUtils.isNotBlank(feptxn.getFeptxnReqRc())) {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnReqRc());
		}
		else if ("FISC".equals(feptxn.getFeptxnChannel())) {  //原存交易
			cbstita.setRESPONSE_CODE(String.valueOf(feptxn.getFeptxnAaRc()));
		}
		else {  //代理交易給主機的4碼RC
			cbstita.setRESPONSE_CODE(feptxntcb.getFeptxntcbImsrc4Fisc());
		}
		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 

		this.setoTita(cbstita);
		this.setTitaToString(cbstita.makeMessage());
		this.setASCIItitaToString(cbstita.makeMessageAscii());
		return FEPReturnCode.Normal;
	}

	@Override
	public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
		return null;
	}
}
