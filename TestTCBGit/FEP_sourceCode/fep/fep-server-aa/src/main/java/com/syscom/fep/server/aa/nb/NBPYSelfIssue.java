package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Objects;

import static com.syscom.fep.base.enums.FEPReturnCode.Normal;

/**
 * @author Jaime
 */
public class NBPYSelfIssue extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal; //aa流程用RC
	private FEPReturnCode rtnCode1 = FEPReturnCode.Normal; //第一次送主機用RC
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal; //整批轉即時送主機用RC

	private FEPReturnCode rtnCode_correction = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode_error = FEPReturnCode.Normal;
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

	private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);

	private String AATxTYPE = "";
	public NBPYSelfIssue(NBData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
		try {
			getFeptxn().setFeptxnStan(getFiscBusiness().getStan());
			getLogContext().setStan(getFeptxn().getFeptxnStan());

            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().nb_PrepareFEPTxn();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                getLogContext().setRemark("PREPARE FEPTXN ERROR");
                sendEMS(getLogContext());
                rtnCode_error = rtnCode;
                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage;
            }

            rtnCode = getFiscBusiness().other_prepareFEPTXNTCB();
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
                getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
                sendEMS(getLogContext());
                rtnCode_error = rtnCode;
                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage;
            }

            // 2. AddTxData: 新增交易記錄(FEPTXN)
            this.addTxData();
            if (rtnCode != FEPReturnCode.Normal) {
                rtnCode_error = rtnCode;

                rtnMessage = getResNBStr(atmReqheader);
                return rtnMessage; // EXIT PROGRAM
            }

            // 3. CheckBusinessRule: 商業邏輯檢核
            //檢核外圍Channel
			if (rtnCode == FEPReturnCode.Normal) {
				RCV_NB_GeneralTrans_RQ tita = this.getmNBReq();
				rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getBody().getRq().getSvcRq().getINTIME());
				if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
					rtnCode_error = rtnCode;
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM));
					feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP));
				}
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 4. SendToCBS/ASC(if need):送往CBS主機處理/進帳務主機
				this.sendToCBS();
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
			}

			// 5. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();

            // 6. Response:組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = getFiscBusiness().prepareNBPYSelfResponseData(tota, getLogContext().getChannel(), AATxTYPE);

			// 7. 交易通知 (if need)
			getFiscBusiness().sendToNotify();

			// 8. 交易結束通知主機(By PCODE)
            this.transactionCloseConnect();

		} catch (Exception ex) {
			rtnMessage = getResNBStr(atmReqheader);
			rtnCode = FEPReturnCode.ProgramException;
			getFiscBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getnBData().getLogContext().setMessage(rtnMessage);
			getnBData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
            String codeToMsg = String.valueOf((rtnCode_error != CommonReturnCode.Normal ? rtnCode_error : rtnCode).getValue());
            FEPChannel channel = FEPChannel.FEP;
            if (rtnCode_error == CommonReturnCode.Normal && rtnCode == CommonReturnCode.Normal) {
                if (getFeptxn() != null
                        && StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())
                        && !StringUtils.equalsAny(getFeptxn().getFeptxnCbsRc(), "000", "4001", "4002", "4007")) {
                    codeToMsg = getFeptxn().getFeptxnCbsRc();
                    channel = FEPChannel.CBS; // 使用 CBS Channel 查錯誤訊息
                }
            }

            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(codeToMsg, channel, getLogContext()));
			logMessage(Level.DEBUG, this.logContext);
		}
		return rtnMessage;
	}

	/**
	 * 2. AddTxData: 新增交易記錄( FEPTxn)
	 *
	 * @return
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            int insertCount2 = feptxnDao.insertSelective(feptxntcb);
            if (insertCount <= 0 || insertCount2 <= 0) { // 更新失敗
                throw new Exception(); //2025.07.15 Transaction call review 調整
            }
			transactionManager.commit(txStatus);
			this.logContext.setRemark("transaction sucess.");
			logMessage(Level.INFO, logContext);
		} catch (Exception ex) { // 新增失敗
			transactionManager.rollback(txStatus);
			this.logContext.setRemark("transaction fail.");
			logMessage(Level.INFO, logContext);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			rtnCode = FEPReturnCode.FEPTXNInsertError;
		}
	}

	/**
	 * 4. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
        /* 0:查詢,1:入扣帳,2:沖正,3:授權,4:沖正手續費優惠次數,5:註記,6:確認 */
		RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
		AATxTYPE = "";
		if(StringUtils.equals(atmReqheader.getMSGKIND().trim(), "R")){ //NPAY 逾時上送沖正請求
			AATxTYPE = "2";  //上主機沖正
		} else {
            AATxTYPE = "1"; //上主機查詢、檢核
        }
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode1 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        rtnCode_correction = rtnCode1;
        tota = hostAA.getTota();

        String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
        String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
        if (feptxn.getFeptxnCbsTimeout() != null && feptxn.getFeptxnCbsTimeout() == 1) { // HostResponseTimeout
            feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM));
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(Objects.toString(rtnCode1.getValue()), FEPChannel.FEP));
			if ( "1".equals(AATxTYPE) ) { //上主機入扣帳逾時
				if ( "NAM".equals(feptxn.getFeptxnChannel()) ) { //整批轉即時
					AATxTYPE = "2"; //主機Timeout，再送一次電文讓主機沖正
					AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
					feptxn.setFeptxnCbsTxCode(AA);
					feptxn.setFeptxnCbsRc("2999");
					feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
					feptxn.setFeptxnFeeCustpay(BigDecimal.valueOf(0));
					feptxn.setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
					rtnCode2 = new CBS(hostAA, getnBData()).sendToCBSForNoCheckCBS(AATxTYPE);
					tota = hostAA.getTota();
					if ( rtnCode2 != FEPReturnCode.Normal ) {
						getLogContext().setProgramName(ProgramName + ".sendToCBS");
						getLogContext().setRemark("NAM整批轉即時交易逾時，送主機沖正失敗");
						sendEMS(getLogContext());
					}
				}
			}
        } else if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
            feptxn.setFeptxnReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
            feptxn.setFeptxnErrMsg("主機檢核上送電文有誤");
        } else {
            feptxn.setFeptxnReplyCode(feptxn.getFeptxnCbsRc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS));
        }
	}

	/**
	 * 5. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
        feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */

        if (rtnCode == FEPReturnCode.Normal && rtnCode1 == FEPReturnCode.Normal) {
            feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
        } else {
            feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
        }

		if (rtnCode1 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode1.getValue());
		}else if (rtnCode2 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode2.getValue());
		}else if (rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		}else{
			feptxn.setFeptxnAaRc(Normal.getValue());
		}

        // 回寫 FEPTXN
        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
        try {
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
        }

        if (rtnCode2 != FEPReturnCode.Normal) {
            // 回寫檔案 (FEPTxn) 發生錯誤
            if(rtnCode == FEPReturnCode.Normal) {
                this.feptxn.setFeptxnReplyCode("T452");//FEPTXNUpdateError
                rtnCode = rtnCode2;
            }
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            getLogContext().setRemark("FEPTXN UPDATE ERROR");
            sendEMS(getLogContext());
        }

		// 20260420 整批轉即時交易更新NPSDTL
		if ("NAM".equals(feptxn.getFeptxnChannel())) {
			rtnCode2 = updateNpsdtl();
			if (rtnCode2 != FEPReturnCode.Normal) { // Update錯誤
				feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
				rtnCode = rtnCode2;
				getLogContext().setReturnCode(rtnCode);
				getLogContext().setRemark("updateNpsdtl Error");
				getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateNpsdtl"));
				sendEMS(getLogContext());
			}
		}
	}

	/**
	 * 8. 交易結束通知主機(By PCODE)
	 */
	public void transactionCloseConnect() {
		try {
			//NPAY交易主機timeout會送沖正電文，成功交易結束需傳送通知
            if ("NAM".equals(feptxn.getFeptxnChannel()) && "A".equals(feptxn.getFeptxnTxrust())) {
				AATxTYPE = ""; //不需提供此值
                String AATxRs = "N";//不需等待主機回應
                String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE, AATxRs);
                if (rtnCode != FEPReturnCode.Normal && rtnCode_error == FEPReturnCode.Normal) {
                    rtnCode_error = rtnCode;
                }
            }
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".transactionCloseConnect"));
			sendEMS(this.logContext);
		}
	}

	public void writeLog(String msg, ProgramFlow flow) {
		LogData logData = new LogData();
		logData.setProgramFlowType(flow);
		logData.setMessageFlowType(MessageFlow.Request);
		logData.setProgramName(ProgramName);
		logData.setMessage(msg);
		// logData.setRemark("MBService Receive Request");
		logData.setServiceUrl("/mb/recv");
		logData.setEj(TxHelper.generateEj());
		this.logMessage(logData);
	}

	private String getResNBStr(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {
		String ResStr = "";
		SEND_NB_GeneralTrans_RS nbRs = new SEND_NB_GeneralTrans_RS();
		SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
		SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
		SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
		SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
		msgrs.setHeader(header);
		body.setOUTDATE(" ");
		body.setOUTTIME(" ");
		body.setFEP_EJNO(" ");
		body.setTXNSTAN(" ");
		body.setCUSTOMERID(" ");
		body.setTXNTYPE("RQ");
		body.setHOSTACC_FLAG(" ");
		body.setTRANSAMT(new BigDecimal("0"));
		body.setTRANSFROUTBAL(new BigDecimal("0"));
		body.setTRANSOUTAVBL(new BigDecimal("0"));
		body.setTRANSAMTOUT(new BigDecimal("0"));
		body.setTRNSFROUTIDNO(" ");
		body.setTRNSFROUTNAME(" ");
		body.setTRNSFROUTBANK(" ");
		body.setTRNSFROUTACCNT(" ");
		body.setTRNSFRINBANK(" ");
		body.setTRNSFRINACCNT(" ");
		body.setCLEANBRANCHOUT(" ");
		body.setCLEANBRANCHIN(" ");
		body.setCUSTPAYFEE(new BigDecimal("0"));
		body.setFISCFEE(new BigDecimal("0"));
		body.setOTHERBANKFEE(new BigDecimal("0"));
		body.setCHAFEE_BRANCH(" ");
		body.setCHAFEEAMT(new BigDecimal("0"));
		body.setTRNSFRINNOTE(" ");
		body.setTRNSFROUTNOTE(" ");
		body.setPAYEREMAIL(" ");
		body.setCUSTOMERNATURE(" ");
		body.setTCBRTNCODE(" ");
		msgrs.setSvcRs(body);
		rsbody.setRs(msgrs);
		nbRs.setBody(rsbody);

		header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
		header.setCHANNEL(nbheader.getCHANNEL());
		header.setMSGID(nbheader.getMSGID());
		header.setCLIENTDT(nbheader.getCLIENTDT());
		SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
		header.setSYSTEMID("ATM");
		header.setSTATUSCODE("T099");
		header.setSEVERITY("ERROR");
		header.setSTATUSDESC("發生exception");
		if(StringUtils.equals(getLogContext().getChannel().getNameS() , FEPChannel.MCH.getNameS()))
			header.setTXNID(nbheader.getTXNID());

		ResStr = XmlUtil.toXML(nbRs);
		return ResStr;
	}

	/**
	 * 整批轉即時交易更新NPSDTL
	 */
	public FEPReturnCode updateNpsdtl() {
		String BatNo = feptxn.getFeptxnChannelEjfno().substring(5, 34);
		int seqNo = Integer.parseInt(feptxn.getFeptxnChannelEjfno().substring(34, 44));
		Npsdtl npsdtl = npsdtlExtMapper.selectByPrimaryKey(BatNo,seqNo);
		if (npsdtl == null){
			getLogContext().setRemark("updateNPSDTL時，找不到原交易的NPSDTL資料");
			getLogContext().setProgramName(ProgramName + ".updateNPSDTL");
			sendEMS(getLogContext());
			return FEPReturnCode.UpdateFail;
		}
		try {
			//NPAY交易更新 NPSDTL
			if ("A".equals(feptxn.getFeptxnTxrust())) {
				npsdtl.setNpsdtlResult("00");
			} else {
				npsdtl.setNpsdtlResult("01");
			}
			npsdtl.setNpsdtlTbsdy(feptxn.getFeptxnTbsdy());
			//交易結果
			if ( !"A".equals(feptxn.getFeptxnTxrust())) {
				if ( feptxn.getFeptxnConRc()!=null && !"4001".equals(feptxn.getFeptxnConRc()) ) {
					npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnConRc());
				}else if( feptxn.getFeptxnRepRc()!=null && !"4001".equals(feptxn.getFeptxnRepRc()) ){
					npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnRepRc());
				}else if( feptxntcb.getFeptxntcbImsrc4Fisc()!=null ){
					npsdtl.setNpsdtlReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
				}else {
					npsdtl.setNpsdtlReplyCode(feptxn.getFeptxnReplyCode());
				}
			}else{
				npsdtl.setNpsdtlReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
			}
			npsdtl.setNpsdtlErrMsg(feptxn.getFeptxnErrMsg());
			npsdtl.setNpsdtlEjfno(String.valueOf(feptxn.getFeptxnEjfno()));
			npsdtl.setNpsdtlTbsdyFisc(feptxn.getFeptxnTbsdyFisc());
			npsdtl.setNpsdtlStan(feptxn.getFeptxnStan());
			npsdtl.setNpsdtlFee(feptxn.getFeptxnFeeCustpayAct());
			if (feptxn.getFeptxnFeeCustpayAct() != null) {
				npsdtl.setNpsdtlHostCharge(feptxn.getFeptxnFeeCustpayAct().intValue());
			}
			npsdtl.setNpsdtlTxTime(feptxn.getFeptxnTxTime());
			npsdtl.setNpsdtlHostBrch(feptxntcb.getFeptxntcbPyHostBrch());
			npsdtl.setNpsdtlCbsRc(feptxntcb.getFeptxntcbImsrcTcb());
			npsdtl.setNpsdtlHostChargeFlag(feptxntcb.getFeptxntcbPyChargeFlag());

			if ( "2".equals(AATxTYPE) ) {
				//主機扣帳逾時，FEP已送沖正給主機
				npsdtl.setNpsdtlCbsRc("P02");
				npsdtl.setNpsdtlReplyCode("2999");
				npsdtl.setNpsdtlHostCharge(0); //手續費
			}

			//更NPSDTL
			npsdtl.setNpsdtlBatNo(BatNo);
			npsdtl.setNpsdtlSeqNo(seqNo);

			if (npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl) > 0) {
				return FEPReturnCode.Normal;
			} else {
				return FEPReturnCode.UpdateFail;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateNPSDTL");
			sendEMS(getLogContext());
			return FEPReturnCode.UpdateFail;
		}
	}
}
