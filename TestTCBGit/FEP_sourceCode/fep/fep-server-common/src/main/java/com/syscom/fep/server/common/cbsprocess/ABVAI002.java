package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.AB_VA_I002;
import com.syscom.fep.vo.text.ims.AB_VA_O002;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;

public class ABVAI002 extends ACBSAction {

	public ABVAI002(MessageBase txData) {
		super(txData, new AB_VA_O002());
	}

	RCV_VA_GeneralTrans_RQ atmRequest = this.getVARequest();

	FISC_INBK inbkReq = this.getInbkRequest();
//	private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

	/**
	 * 組CBS TITA電文
	 *
	 * @param txType
	 * @return
	 * @throws Exception
	 */
	@Override
	public FEPReturnCode getCbsTita(String txType) throws Exception {
		/* TITA 請參考合庫主機電文規格(AB_VA_I001) */
		// Header
		inbkReq = this.getInbkRequest();
		AB_VA_I002 cbstita = new AB_VA_I002();

		String VACATE = atmRequest.getBody().getRq().getSvcRq().getVACATE();//'業務類別代號’
		String AEIPYTP = atmRequest.getBody().getRq().getSvcRq().getAEIPYTP();//‘交易類別’

//		cbstita.setIMS_TRANS("MFEPEA00");
		cbstita.setIMS_TRANS(TxHelper.getIMSTRANSString(feptxn.getFeptxnChannel(), getLogContext().getMessageId()));
		cbstita.setSYSCODE("FEP");
		cbstita.setSYS_DATETIME(
				FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
		cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
		// TXN_FLOW
		if (DbHelper.toShort(false).equals(feptxn.getFeptxnFiscFlag())) {
			cbstita.setTXN_FLOW("C"); // 自行
			cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE());
			cbstita.setAEIPYBK(atmRequest.getBody().getRq().getSvcRq().getAEIPYBK());
		} else {
			cbstita.setTXN_FLOW("A"); // 代理
			//FeptxnRepRc財金回傳值 如果有去過財金 MSG_CAT 統一帶10
			if(StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())){
				cbstita.setMSG_CAT("10");
			} else {
				cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE());
			}

		}
//		cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE());
		cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
		cbstita.setPCODE(feptxn.getFeptxnPcode());
		cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
		// PROCESS_TYPE
		if(!DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) && VACATE.equals("10")){
			cbstita.setPROCESS_TYPE("CHK");
		}else{
			cbstita.setPROCESS_TYPE("SET");
		}
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
		cbstita.setCARDISSUE_BANK(atmRequest.getBody().getRq().getSvcRq().getAEIPCRBK());
		// cbstita.setCBSMAC();


		if(VACATE.equals("10")){
			if(!DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())){
				if(AEIPYTP.equals("00")){
					cbstita.setCARDTYPE("K");
				}else{
					cbstita.setCARDTYPE("X");
				}
			}else{
				cbstita.setCARDTYPE("N");
			}
		}else{
			cbstita.setCARDTYPE("N");
		}
		feptxntcb.setFeptxntcbCardfmt(cbstita.getCARDTYPE()); // 紀錄在FEPTXNTCB

		if(StringUtils.isNotBlank(feptxn.getFeptxnRepRc())){
			if ("4001".equals(feptxn.getFeptxnRepRc())) {
				cbstita.setRESPONSE_CODE(feptxntcb.getFeptxntcbImsrc4Fisc());
			}else {
				cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
			}
		}else{
			cbstita.setRESPONSE_CODE("0000"); // 正常才上送
		}

		// Detail
		// cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
		cbstita.setICCHIPSTAN(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnIcSeqno()),8,"0"));
		cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
		String AEICDATE = null;
		if(StringUtils.isNotBlank(atmRequest.getBody().getRq().getSvcRq().getAEICDAY()) && atmRequest.getBody().getRq().getSvcRq().getAEICDAY().length() == 7){
			AEICDATE =  String.valueOf(Integer.valueOf(atmRequest.getBody().getRq().getSvcRq().getAEICDAY()) + 19110000);
		} else {
			AEICDATE = atmRequest.getBody().getRq().getSvcRq().getAEICDAY();
		}
		String AEICTIME = atmRequest.getBody().getRq().getSvcRq().getAEICTIME();
		cbstita.setTERMTXN_DATETIME(AEICDATE + AEICTIME);
		if(StringUtils.isNotBlank(atmRequest.getBody().getRq().getSvcRq().getICMARK()))
			cbstita.setICMEMO(atmRequest.getBody().getRq().getSvcRq().getICMARK());
		else
			cbstita.setICMEMO("463232393035323735353939393737363631323331363130312020202037");

		//第一次上送CBS
//		boolean isSysstatHbknoEqFeptxnTroutBkno = SysStatus.getPropertyValue().getSysstatHbkno()
//				.equals(feptxn.getFeptxnTroutBkno());
//		if(isSysstatHbknoEqFeptxnTroutBkno && "RQ".equals(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE())) {
//			Calendar cal = Calendar.getInstance();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.TAIWAN);
//			int indate = Integer.valueOf(atmRequest.getBody().getRq().getSvcRq().getAEICDAY()) + 19110000;
//			int intime = Integer.valueOf(atmRequest.getBody().getRq().getSvcRq().getAEICTIME());
//			cal.setTime(sdf.parse(String.valueOf(indate) + StringUtils.leftPad(String.valueOf(intime), 6, "0")));
//			cbstita.setTERMTXN_DATETIME(FormatUtil.dateTimeFormat(cal, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
//			String icTac = atmRequest.getBody().getRq().getSvcRq().getIC_TAC_LEN() + atmRequest.getBody().getRq().getSvcRq().getIC_TAC();
//			String TXNICCTAC = StringUtils.leftPad(StringUtils.defaultIfEmpty(icTac, StringUtils.EMPTY), 20, "40"); ;
//			cbstita.setTXNICCTAC(TXNICCTAC);
//			cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE());
//		}
//		else {
//			cbstita.setTXNICCTAC("40404040404040404040");
//		}
		String icTacFromBody = atmRequest.getBody().getRq().getSvcRq().getIC_TAC() != null ? atmRequest.getBody().getRq().getSvcRq().getIC_TAC() : "00000000";
		String icTac = atmRequest.getBody().getRq().getSvcRq().getIC_TAC_LEN() + icTacFromBody;
		String TXNICCTAC = StringUtils.leftPad(StringUtils.defaultIfEmpty(icTac, StringUtils.EMPTY), 20, "40"); ;
		cbstita.setTXNICCTAC(TXNICCTAC);
		cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
		cbstita.setAEIPYAC(feptxn.getFeptxnTroutActno());
		if("02".equals(VACATE)) {
			if(!"03".equals(AEIPYTP)) {
				cbstita.setFROMACT(feptxn.getFeptxnMajorActno());
			}
		} else {
			// if("00".equals(notice34)){
			// 	cbstita.setFROMACT(feptxn.getFeptxnMajorActno());
			// }else{
				cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
			// }
		}
		if("10".equals(VACATE) && DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())) {
			cbstita.setCHARGCUS(feptxn.getFeptxnFeeCustpay());
		}else {
			cbstita.setCHARGCUS(BigDecimal.ZERO);
		}
		cbstita.setVACATE(VACATE);
		cbstita.setAEIPYTP(AEIPYTP);
		if("10".equals(VACATE)) {
			cbstita.setAEIPYUES(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES());
			cbstita.setAELFTP(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());
			/* 以下三個欄位，若是跨行，財金回0210，才有值 */
			if(DbHelper.toBoolean(feptxn.getFeptxnFiscFlag())){
				if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())){
					cbstita.setAEILFRC1(feptxn.getFeptxnRemark().substring(0,2));
					cbstita.setAEILFRC2(feptxn.getFeptxnRemark().substring(2,4));
					cbstita.setOPENACT(feptxn.getFeptxnRemark().substring(4,6));
				} else {
					cbstita.setAEILFRC1("  ");
					cbstita.setAEILFRC2("  ");
					cbstita.setOPENACT("  ");
				}

			}
			// else{
			// 	cbstita.setAEILFRC1("00");
			// 	cbstita.setAEILFRC2("00");
			// 	cbstita.setOPENACT("00");
			// }
		}
		// else{
		// 	cbstita.setAEIPYUES("00");
		// 	cbstita.setAELFTP("00");
		// 	cbstita.setAEILFRC1("00");
		// 	cbstita.setAEILFRC2("00");
		// 	cbstita.setOPENACT("00");
		// }
		cbstita.setIDNO(feptxn.getFeptxnIdno());

		if("02".equals(VACATE)) {
			if(!"03".equals(AEIPYTP)) {
				cbstita.setCLCPYCI(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
			}
			String payuntno = atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO();
			String taxtype = atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE();
			Npsunit npsunit = dbNPSUNIT.selectByNoAndPayType(payuntno, taxtype);
			cbstita.setPAYUNTNO(npsunit.getNpsunitNo());
			cbstita.setTAXTYPE(npsunit.getNpsunitPaytype());
			cbstita.setPAYFEENO(npsunit.getNpsunitFeeno());
			cbstita.setAEIPYAC2(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2());
		}
		cbstita.setMOBILENO(atmRequest.getBody().getRq().getSvcRq().getMOBILENO());
		cbstita.setINSURE_IDNO(" ");
		if("10".equals(VACATE)) {
			cbstita.setBIRTHDAY(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY().toString());
			cbstita.setTELHOME(atmRequest.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
		}

		cbstita.setAEIPYBH(atmRequest.getBody().getRq().getSvcRq().getAEIPYBH());
		cbstita.setSSLTYPE(atmRequest.getBody().getRq().getSvcRq().getSSLTYPE());

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
		/* 電文內容格式請參照TOTA電文格式(AB_VA_O001) */
		/* 拆解主機回應電文 */
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		AB_VA_O002 tota = new AB_VA_O002();
		tota.parseCbsTele(cbsTota);
		this.setTota(tota);//塞入拆解後的tota讓AA取得

		/* 更新交易 */
		this.updateFEPTxn(tota);

		/* 新增交易通知 */
		if (StringUtils.isNotBlank(tota.getNOTICE_TYPE())) {
			insertSMSMSG(tota);
		}
		/* 回覆FEP */
		// 處理 CBS 回應
		if (!StringUtils.equals(feptxn.getFeptxnCbsRc(), NormalRC.CBS_OK)) {
			/* 回應錯誤 */
			rtnCode = FEPReturnCode.CBSCheckError;
		}
		return rtnCode;
	}

	/**
	 * 更新交易
	 *
	 * @param cbsTota
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTxn(AB_VA_O002 cbsTota) throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
		feptxn.setFeptxnCbsTimeout((short) 0);
		/* 變更FEPTXN交易記錄 */
		// IMSRC_TCB = "000" or empty表交易成功
		// IMSRC_FISC = "0000" or "4001" 表交易成功
		/* 變更FEPTXN交易記錄 */
		if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
			cbsTota.setIMSRC_TCB("000");
		}
		// IMSRC_TCB = "000" or empty表交易成功
		// IMSRC_FISC = "4001" 表交易成功
		if (!"000".equals(cbsTota.getIMSRC_TCB())) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			if("Y".equals(cbsTota.getIMSACCT_FLAG())){
				feptxn.setFeptxnAccType((short) 1); // 已記帳
			}else {
				feptxn.setFeptxnAccType((short) 0); // 未記帳
			}
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
			rtnCode = FEPReturnCode.CBSCheckError;
		} else if (!StringUtils.equalsAny(cbsTota.getIMSRC4_FISC(), "4001")) {
			feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
			if("Y".equals(cbsTota.getIMSACCT_FLAG())){
				feptxn.setFeptxnAccType((short) 1); // 已記帳
			}else {
				feptxn.setFeptxnAccType((short) 0); // 未記帳
			}
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
			rtnCode = FEPReturnCode.CBSCheckError;
		} else {
			feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
			// CBS 帳務日(本行營業日)
			//轉西元年
			String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
			if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
					|| (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
				imsbusinessDate = "00000000";
			} else {
				imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
			}
			feptxn.setFeptxnTbsdy(imsbusinessDate);
			// 記帳類別
			if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
				feptxn.setFeptxnAccType((short) 1); // 已記帳
			}else{
				feptxn.setFeptxnAccType((short)0);
			}
			//帳務分行
			feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
			feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
			// 主機交易時間
			feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
			/* 主機回傳的手續費 */
			// 手續費(轉出客戶)
			feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
			// 交易通知方式
			if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
				feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
			}
			String VACATE = atmRequest.getBody().getRq().getSvcRq().getVACATE();//'業務類別代號’
			String AEIPYTP = atmRequest.getBody().getRq().getSvcRq().getAEIPYTP();//‘交易類別’
			Vatxn vatxn = this.getNBData().getVatxn();
			if("02".equals(VACATE) && "03".equals(AEIPYTP)) {
				feptxn.setFeptxnTroutActno(cbsTota.getFROMACT());
				vatxn.setVatxnTroutActno(cbsTota.getFROMACT());
				vatxn.setVatxnBusinessUnit(cbsTota.getPAYUNTNO());
				vatxn.setVatxnPaytype(cbsTota.getTAXTYPE());
				vatxn.setVatxnFeeno(cbsTota.getPAYFEENO());
			} else if ("10".equals(VACATE)) {
				if (StringUtils.equalsIgnoreCase(feptxn.getFeptxnChannel(), "FID")
						&& StringUtils.equals(feptxn.getFeptxnTroutBkno(), "006")
						&& StringUtils.equals(feptxn.getFeptxnTrinBkno(), "006")) {
					feptxn.setFeptxnTroutActno(cbsTota.getFROMACT());
					vatxn.setVatxnTroutActno(cbsTota.getFROMACT());
				}
				vatxn.setVatxnResult(cbsTota.getAEILFRC1());
				vatxn.setVatxnAcresult(cbsTota.getAEILFRC2());
				vatxn.setVatxnAcstat(cbsTota.getOPENACT());
				vatxn.setVatxnTelresult(cbsTota.getMNO_CHANGE());
			}

		}

		String IMS_ERRPGM = this.getImsPropertiesValue(cbsTota, ImsMethodName.IMS_ERRPGM.getValue());
		if (StringUtils.isNotBlank(IMS_ERRPGM)) {
			feptxn.setFeptxnErrMsg(IMS_ERRPGM);
		}

		//更新TCB資料
		other_prepareUpdateIMSData(cbsTota);

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

//        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
		try {
			int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
			int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

			if (insertCount <= 0 || insertCount2 <= 0) {
				rtnCode = FEPReturnCode.FEPTXNUpdateError;
				transactionManager.rollback(txStatus);
			}else {
				transactionManager.commit(txStatus);
			}
		} catch (Exception ex){
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
	private void insertSMSMSG(AB_VA_O002 cbsTota) throws ParseException {
//		String txDate = feptxn.getFeptxnTxDate();
//		Integer ejfno = feptxn.getFeptxnEjfno();
//		Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//		// 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//		if (smsmsg == null) {
//			smsmsg = new Smsmsg();
//			smsmsg.setSmsmsgTxDate(txDate);
//			smsmsg.setSmsmsgEjfno(ejfno);
//			smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//			smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//			smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//			smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//			smsmsg.setSmsmsgBrno(feptxn.getFeptxnBrno());
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
