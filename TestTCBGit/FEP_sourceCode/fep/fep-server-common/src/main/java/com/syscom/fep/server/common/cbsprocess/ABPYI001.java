package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_PY_I001;
import com.syscom.fep.vo.text.ims.AB_PY_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * 代理全繳交易上送主機
 *
 * @author vincent
 *
 */

public class ABPYI001 extends ACBSAction {

	public ABPYI001(MessageBase txData) {
		super(txData, new AB_PY_O001());
	}

	private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_PY_I001) */
		// Header
		AB_PY_I001 cbstita = new AB_PY_I001();
		String IPYDATA = "";
		if("EAT".equals(feptxn.getFeptxnChannel())){
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
			IPYDATA = atmReq.getIPYDATA();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID());
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			// 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
					&& "K".equals(atmReq.getPICCDID()) && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
				cbstita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
			}else{
				cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
			}
			
			//銷帳編號後24位
			if(StringUtils.isNotBlank(atmReq.getSPECIALDATA())) {
				cbstita.setPY_PAYTXNOL1(atmReq.getSPECIALDATA().substring(75,  99));
			}
			/* 預約交易 */
			/*  (FEPTXN_MSGKIND = “T” OR “X” ) */
			if ("RV".equals(atmReq.getLANGID())
					&& SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
				//預約交易扣款註記
				cbstita.setEATM_RESERVE_FLAG("Y");
				//原預約時交易日期時間(驗TAC使用)
				if(StringUtils.isNotBlank(atmReq.getSPECIALDATA())) {
					cbstita.setORIGINAL_TX_DAYTIME(atmReq.getSPECIALDATA().substring(0,  14));
				}
			}
			cbstita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
			// ATM交易序號
			cbstita.setATMTRANSEQ(atmReq.getTRANSEQ());
		}else{
			ATMGeneralRequest atmReq = this.getAtmRequest();
			cbstita.setMSG_CAT(atmReq.getMSGTYP());
			cbstita.setCARDTYPE(atmReq.getPICCDID());
			cbstita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
			// 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
					&& "K".equals(atmReq.getPICCDID()) && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
				cbstita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
			}else{
				cbstita.setTXNICCTAC(StringUtils.repeat("40",10));
			}
			cbstita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
			// ATM交易序號
			cbstita.setATMTRANSEQ(atmReq.getTRANSEQ());
		}
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		// TXN_FLOW
		if (feptxn.getFeptxnFiscFlag() == 0) {
			cbstita.setIMS_TRANS("MFEPAT00");
			cbstita.setTXN_FLOW("C"); // 自行
		} else {
			cbstita.setIMS_TRANS("MFEPAF00");
			cbstita.setTXN_FLOW("A"); // 代理
			if(StringUtils.equals(txType, "1")) {
				cbstita.setMSG_CAT("10");
			}
		}
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		// PROCESS TYPE
		if ("0".equals(txType)) { // 查詢、檢核
			cbstita.setPROCESS_TYPE("CHK");
		} else if ("1".equals(txType)) { // 入扣帳
			cbstita.setPROCESS_TYPE("ACCT");
		} else if ("2".equals(txType)) { // 沖正
			cbstita.setPROCESS_TYPE("RVS");
		}
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
		if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
		} else {
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		}

		// ATM交易序號
		cbstita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno()); 
		
		// Detail
		cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
		cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
		if(StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno())) {
			cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
		}
		if(StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno())) {
			cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
		}
		if ("256".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 3)) // 繳費移轉計畫
				&& "18888888".equals(feptxn.getFeptxnBusinessUnit()) && "59999".equals(feptxn.getFeptxnPaytype())
				&& "9999".equals(feptxn.getFeptxnPayno()) && "99991231".equals(feptxn.getFeptxnDueDate())) {
			cbstita.setPY_SPECIAL_FLAG("ET");
			// EATM轉帳手續費(優惠後手續費)
			if("EAT".equals(feptxn.getFeptxnChannel()) && StringUtils.isNotBlank(IPYDATA)){
				cbstita.setCHANNEL_CHARGE(new BigDecimal(IPYDATA.substring(8, 10)));
			}
		}

		// 取得財金全繳委託單位檔之「帳務代理銀行」，合庫自行(C)或代理(A)交易：委託單位代號="10000081"且繳費類別="00001" =>「帳務代理銀行代號」需改為”006”
		// 讀取 委託單位檔 NPSUNIT.NPSUNIT_BKNO
		if(StringUtils.equals("10000081", feptxn.getFeptxnBusinessUnit()) && StringUtils.equals("00001", feptxn.getFeptxnPaytype())) {
			cbstita.setPY_HOST_BRANCH("0060000");
		}else {
			Npsunit npsunit = npsunitExtMapper.selectByPrimaryKey(feptxn.getFeptxnBusinessUnit(), feptxn.getFeptxnPaytype(),
					feptxn.getFeptxnPayno());
			if (npsunit == null) {
				cbstita.setPY_HOST_BRANCH("");
			} else {
				cbstita.setPY_HOST_BRANCH(npsunit.getNpsunitBkno());
			}
		}
		
		cbstita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
		cbstita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
		cbstita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
		cbstita.setPY_PAYTXNOL(feptxn.getFeptxnReconSeqno()); // 銷帳編號
		cbstita.setPY_PAYDDATE(feptxn.getFeptxnDueDate()); // 繳款期限


		if ("A".equals(cbstita.getTXN_FLOW())
				&& "ACCT".equals(cbstita.getPROCESS_TYPE())) {// 代理記帳須提供
			//繳費作業手續費
			DecimalFormat df = new DecimalFormat("0000");
			cbstita.setPY_CHARGCUS(new BigDecimal(df.format(feptxn.getFeptxnNpsFeeCustpay())));
			//組合成財金 response Data bit#50
			cbstita.setPY_CHARGUNT(new BigDecimal(
					df.format(feptxn.getFeptxnNpsFeeRcvr().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeAgent().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeTrout().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeTrin().multiply(new BigDecimal("10")))+
							df.format(feptxn.getFeptxnNpsFeeFisc().multiply(new BigDecimal("10")))));
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
		/* 電文內容格式請參照TOTA電文格式(AB_PY_O001) */
		/* 拆解主機回應電文 */
		AB_PY_O001 tota = new AB_PY_O001();
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
	private FEPReturnCode updateFEPTxn(AB_PY_O001 cbsTota) throws Exception {
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
    	}else if (!cbsTota.getIMSRC_TCB().equals("000")
    			|| (!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("0000"))) {
			rtnCode = FEPReturnCode.CBSCheckError;
		}

		//20260514 修改轉出方為本行時取主機下送手續費
		if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno()) ) {
			feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
		} else if (feptxn.getFeptxnPaytype().compareTo("00001") >= 0 && feptxn.getFeptxnPaytype().compareTo("49999") <= 0) {
			// 轉出行為他行，判斷繳費類別範圍內手續費為0，範圍外取財金手續費(REP時已記錄)
			feptxn.setFeptxnFeeCustpay(BigDecimal.valueOf(0));
		}

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        	int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);
        	
			if (insertCount <= 0 || insertCount2 <= 0) {
				throw new Exception();
			}
			transactionManager.commit(txStatus);
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
