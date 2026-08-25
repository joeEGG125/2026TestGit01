package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @author Jaime
 */
public class LEIssueRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode1 = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode3 = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode4 = FEPReturnCode.Normal;
	private String rtnMessage = "";
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
	private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

	public LEIssueRequestA(NBData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		Vatxn vatxn = new Vatxn();
		getFiscBusiness().setmNBtxData(getnBData());
		RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
		RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
		RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
		RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();
		try {
			getFeptxn().setFeptxnStan(getFiscBusiness().getStan());
			getLogContext().setStan(getFeptxn().getFeptxnStan());

			// 1. 準備FEP交易記錄檔
			_rtnCode = getFiscBusiness().VAPrepareFEPTXN();
			if (_rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
				getLogContext().setRemark("PREPARE FEPTXN ERROR");
				sendEMS(getLogContext());
				return rtnMessage;
			}

			_rtnCode = getFiscBusiness().other_prepareFEPTXNTCB();
			if (_rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
				getLogContext().setRemark("PREPARE FEPTXNTCB ERROR");
				sendEMS(getLogContext());
				return rtnMessage;
			}

            // 2. AddTxData: 新增交易記錄(FEPTXN)
            addTxData();
            if (_rtnCode != FEPReturnCode.Normal) {
                return rtnMessage;
            }

            /* 撤銷通知(03)已在HANDLER 依NPSUNIT的帳務代理行決定走自行AA或跨行AA，
            如找不到 8+5，會先走自行AA，透過以下檢核剔退交易。 */
            String payuntno = nbbody.getSENDDATA().getPAYUNTNO();
            String taxtype = nbbody.getSENDDATA().getTAXTYPE();
            Npsunit npsunit = dbNPSUNIT.selectByNoAndPayType(payuntno, taxtype);
            if ( npsunit == null) {
                feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                feptxn.setFeptxnReplyCode("4808");
                _rtnCode = FEPReturnCode.NPSNotFound;
                // GO TO 8 更新交易記錄
            }else{
                feptxn.setFeptxnBusinessUnit(npsunit.getNpsunitNo());
                feptxn.setFeptxnPaytype(npsunit.getNpsunitPaytype());
                feptxn.setFeptxnPayno(npsunit.getNpsunitFeeno());
                if(StringUtils.equals(nbbody.getAEIPYTP(), "03")) { //取得撤銷通知的帳務代理行
                    feptxn.setFeptxnTrinBkno(npsunit.getNpsunitBkno().substring(0, 3)); // 取得撤銷通知的帳務代理行
                }
            }

            // 3. 商業邏輯檢核(ATM電文)
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), nbbody.getINTIME());
                if (_rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                    // GO TO 8 更新交易記錄
                }
            }

			// 4. 新增約定及核驗交易(VATXN)記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
				prepareVATXNforVALE(vatxnRefBase, tita);
				vatxn = vatxnRefBase.get();
				vatxnMapper.insertSelective(vatxn);
			}

			// 5. SendToCBS/ASC(if need): 進帳務主機手續費分潤
            if(_rtnCode == FEPReturnCode.Normal) {
                if(StringUtils.equals(nbbody.getAEIPYTP(), "03")) {
                    String AATxTYPE = "3"; // 上CBS入扣帳
                    String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
                    feptxn.setFeptxnCbsTxCode(AA);
                    this.getnBData().setVatxn(vatxn);
                    ACBSAction aaObject = (ACBSAction) this.getInstanceObject(AA, getnBData());
                    _rtnCode2 = new CBS(aaObject, getnBData()).sendToCBS(AATxTYPE);
//					vatxn = aaObject.getNBData().getVatxn();
                    tota = aaObject.getTota();
                }
            }

			// 6. 組送往 FISC 之 Request 電文並等待財金之 Response( if need)
			if (_rtnCode == FEPReturnCode.Normal && _rtnCode2 == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
				if (_rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                    // GO TO 8 更新交易記錄
                }
			}

			// 7. CheckResponseFromFISC:檢核回應電文是否正確
			if (_rtnCode == FEPReturnCode.Normal && _rtnCode2 == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();
//				if (_rtnCode != FEPReturnCode.Normal && "4001".equals(feptxn.getFeptxnRepRc())) {
//                    // GO TO 8 更新交易記錄
//				}
			}

			// 8. UpdateTxData:更新交易記錄(FEPTXN )
            updateTxData();

            // 9. 更新約定及核驗交易(VATXN)記錄(if need)
            if(vatxn != null) {
                vatxn.setVatxnConRc(getFeptxn().getFeptxnConRc());
                vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
                vatxn.setVatxnReqRc(feptxn.getFeptxnReqRc());
                vatxn.setVatxnRepRc(feptxn.getFeptxnRepRc());
                vatxn.setVatxnBusinessUnit(feptxn.getFeptxnBusinessUnit());
                vatxn.setVatxnPaytype(feptxn.getFeptxnPaytype());
                vatxn.setVatxnFeeno(feptxn.getFeptxnPayno());

                if(StringUtils.isNotBlank(getFiscRes().getMEMO()) && "4001".equals(feptxn.getFeptxnRepRc())) {
                    feptxn.setFeptxnRemark(getFiscRes().getMEMO());
                }

//				Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
                vatxnMapper.updateByPrimaryKeySelective(vatxn);
            }


			// 10. 組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = this.response(vatxn);

			// 11. 交易通知 (if need)
			getFiscBusiness().sendToNotify();
		} catch (Exception ex) {
			rtnMessage = getResVAStr(header);
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getnBData().getLogContext().setMessage(rtnMessage);
			getnBData().getLogContext().setProgramName(this.aaName);
			getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setProgramName(this.aaName);
            String codeToMsg = String.valueOf(_rtnCode.getValue());
            FEPChannel channel = FEPChannel.FEP;
            // 2026.3.16 LeYun 修正交易異常通知邏輯
            if (getFeptxn() != null) {
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !"4001".equals(getFeptxn().getFeptxnRepRc())) {
                    codeToMsg = getFeptxn().getFeptxnRepRc();
                    channel = FEPChannel.FISC; // 使用財金 Channel 查錯誤碼
                }
                else if (_rtnCode != FEPReturnCode.ProgramException && getFeptxn().getFeptxnAaRc() != null) {
                    codeToMsg = String.valueOf(getFeptxn().getFeptxnAaRc());
                }
            }

            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(codeToMsg, channel, getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
		}
		// 先組回應ATM 故最後return空字串
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
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
//			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
//			int insertCount2 = feptxnDao.insertSelective(feptxntcb);
//			if (insertCount <= 0 || insertCount2 <= 0) { // 更新失敗
//				throw new Exception(); //2025.07.15 Transaction call review 調整
//			}
            _rtnCode = getFiscBusiness().insertFEPTxn();
            _rtnCode1 = getFiscBusiness().insertFEPTXNTCB();
            if (_rtnCode != FEPReturnCode.Normal || _rtnCode1 != FEPReturnCode.Normal) { // 更新失敗
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
			_rtnCode = FEPReturnCode.FEPTXNInsertError;
		}
	}

	/**
	 * 4. Prepare約定及核驗交易(VATXN)記錄
	 * @param vatxn
	 * @param tita
	 * @return
	 */
	public FEPReturnCode prepareVATXNforVALE(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnStan(getFeptxn().getFeptxnStan());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            String VACATE = tita.getBody().getRq().getSvcRq().getVACATE();//'業務類別代號’
            String AEIPYTP = tita.getBody().getRq().getSvcRq().getAEIPYTP();//‘交易類別’
            vatxn.get().setVatxnCate(VACATE);
            vatxn.get().setVatxnType(AEIPYTP);
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnTroutKind(null == getFeptxn().getFeptxnTroutKind() ? StringUtils.SPACE : getFeptxn().getFeptxnTroutKind());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());

            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
            vatxn.get().setVatxnBusino(tita.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
            vatxn.get().setVatxnBusinessUnit(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO());
            vatxn.get().setVatxnPaytype(tita.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
            vatxn.get().setVatxnFeeno(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
            vatxn.get().setVatxnPactno(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2());
            vatxn.get().setVatxnActno(getFeptxn().getFeptxnTroutActno());

            vatxn.get().setUpdateUserid(0);
            vatxn.get().setUpdateTime(new Date());


            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

	/**
	 * 8. 	更新交易記錄(FEPTXN)
	 *
	 * @return
	 */
	private FEPReturnCode updateTxData() {
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
            getFeptxn().setFeptxnPending((short) 2);  /*解除 Pending*/
		}

        String AEIPYTP = this.getmVAReq().getBody().getRq().getSvcRq().getAEIPYTP();
        if ((!StringUtils.equals(AEIPYTP, "03") && _rtnCode == CommonReturnCode.Normal && "4001".equals(getFeptxn().getFeptxnRepRc()))
                || (StringUtils.equals(AEIPYTP, "03") && StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && _rtnCode2 == CommonReturnCode.Normal)) { /*+REP*/
            //約定(00)、撤銷(01)以財金REP結果回前端
            //撤銷通知(03)以主機結果回前端(財金REP失敗不影響)
            getFeptxn().setFeptxnReplyCode("4001"); /*回覆 ATM正常*/
			getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A成功
			if (getTxData().getMsgCtl().getMsgctlFisc2way() == 0 && "4001".equals(feptxn.getFeptxnRepRc())) {
				getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK); /*+CON*/
				_rtnCode1 = getFiscBusiness().sendConfirmToFISC();
				if (_rtnCode1 != CommonReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
					getLogContext().setReturnCode(_rtnCode1);
					getLogContext().setRemark("SendConfirmToFISC Error");
					getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
					sendEMS(getLogContext());
				}
			}

			//跨行清算統計
			if (getnBData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
				_rtnCode3 = getFiscBusiness().processAptot(false);
				if (_rtnCode3 != CommonReturnCode.Normal) {
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode3.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
					getLogContext().setReturnCode(_rtnCode3);
					getLogContext().setRemark("ProcessAptot Error");
					getLogContext().setProgramName(StringUtils.join(ProgramName, ".processAptot"));
					sendEMS(getLogContext());
				}
			}
		} else if (!StringUtils.equals(AEIPYTP, "03") && StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
            //撤銷通知(03)財金-rep，仍以主機結果回前端
			feptxn.setFeptxnTxrust("R"); /*Reject-normal*/
			feptxn.setFeptxnReplyCode(getFeptxn().getFeptxnRepRc());
			feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC));
		} else if (_rtnCode2 != CommonReturnCode.Normal) { //主機錯誤
			feptxn.setFeptxnTxrust("R"); /* Reject-normal */
			if (feptxn.getFeptxnCbsTimeout() == 1) { //主機逾時，回-con 給財金
				feptxn.setFeptxnReplyCode("0601"); /*+CON*/
			} else {
				feptxn.setFeptxnReplyCode(feptxntcb.getFeptxntcbImsrc4Fisc());
			}
			feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnReplyCode(), FEPChannel.FISC));
		} else if(feptxn.getFeptxnFiscTimeout() != null && feptxn.getFeptxnFiscTimeout() == 1) {
            //財金timeout回覆財金-Con
            feptxn.setFeptxnTxrust("R");
            feptxn.setFeptxnConRc("0601");//交易逾時
            feptxn.setFeptxnReplyCode(getFeptxn().getFeptxnConRc());
            feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(getFeptxn().getFeptxnConRc(), FEPChannel.FISC));

            _rtnCode1 = getFiscBusiness().sendConfirmToFISC();
            if (_rtnCode1 != CommonReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode1.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                getLogContext().setReturnCode(_rtnCode1);
                getLogContext().setRemark("SendConfirmToFISC Error");
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
                sendEMS(getLogContext());
            }
        } else { //FEP錯誤
            if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getnBData().getLogContext()));
                feptxn.setFeptxnErrMsg(TxHelper.getMessageFromFEPReturnCode(Objects.toString(_rtnCode.getValue()), FEPChannel.FEP));
            }
        }

		feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
		if (_rtnCode3 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode3.getValue());
		} else if (_rtnCode2 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		} else if (_rtnCode1 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode1.getValue());
		} else if (_rtnCode != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else {
			getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/

		//回寫檔案 (FEPTxn)
		_rtnCode4 = getFiscBusiness().updateTxData();
		if (_rtnCode4 != FEPReturnCode.Normal) { // Update錯誤
			if (feptxn.getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
				feptxn.setFeptxnReplyCode("T452"); //FEPTXNUpdateError
				_rtnCode = _rtnCode4;
			}
			getLogContext().setReturnCode(_rtnCode);
			getLogContext().setRemark("UpdateTxData Error");
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(getLogContext());
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 10. 	組ATM回應電文 & 回 ATMMsgHandler
	 * @param vatxn
	 * @return
	 * @throws Exception
	 */
	private String response(Vatxn vatxn) throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

			header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
			header.setCHANNEL(feptxn.getFeptxnChannel());
			header.setMSGID(tita.getBody().getRq().getHeader().getMSGID());
			header.setCLIENTDT(tita.getBody().getRq().getHeader().getCLIENTDT());
			header.setSYSTEMID("ATM");

			if (feptxn.getFeptxnCbsTimeout() != null && feptxn.getFeptxnCbsTimeout() == 1) {
				header.setSTATUSCODE("T203");  //主機回應逾時
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC("主機回應逾時");
			}else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxntcb.getFeptxntcbImsrc4Fisc())) { //主機錯誤
				header.setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc()); //自行回應結果，主機失敗，提供3碼RC，否則給空白或000
				body.setRSPRESULT(feptxntcb.getFeptxntcbImsrcTcb());
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			} else if ( !("03".equals(tita.getBody().getRq().getSvcRq().getAEIPYTP())) && StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc()) ) { //財金錯誤
				header.setSTATUSCODE(feptxn.getFeptxnRepRc());
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			} else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
				header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			} else {
				header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
				header.setSEVERITY("INFO");
				header.setSTATUSDESC("");
				getLogContext().setRemark("STATUSCODE OK111" +NormalRC.FISC_ATM_OK);
				logMessage(getLogContext());
			}

			body.setOUTDATE(feptxn.getFeptxnTxDate());
			body.setOUTTIME(feptxn.getFeptxnTxTime());
			body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
			body.setTXNSTAN(feptxn.getFeptxnStan());
			body.setCUSTOMERID(feptxn.getFeptxnIdno());
			body.setTXNTYPE(tita.getBody().getRq().getSvcRq().getTXNTYPE());
			body.setFSCODE(feptxn.getFeptxnTxCode());
			body.setTRANSAMT(tita.getBody().getRq().getSvcRq().getTRANSAMT() == null ? "0" : tita.getBody().getRq().getSvcRq().getTRANSAMT().multiply(new BigDecimal(100)).toString());
			body.setAEIPYTP(tita.getBody().getRq().getSvcRq().getAEIPYTP());
			body.setTAXIDNO(vatxn.getVatxnIdno());
			body.setAEIPCRBK(tita.getBody().getRq().getSvcRq().getAEIPCRBK());
			body.setCLACTNO(feptxn.getFeptxnTroutActno());
			replydata.setPAYUNTNO(vatxn.getVatxnBusinessUnit());
			replydata.setTAXTYPE(vatxn.getVatxnPaytype());
			replydata.setPAYFEENO(vatxn.getVatxnFeeno());

			body.setREPLYDATA(replydata);
			SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
			va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
			va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
			va_rs.getBody().getRs().setHeader(header);
			va_rs.getBody().getRs().setSvcRs(body);

			getLogContext().setProgramName("prepareResponseData");
			logMessage(Level.INFO,getLogContext());

			rtnMessage =XmlUtil.toXML(va_rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	private String getResVAStr(RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header vaheader) {
		String ResStr = "";
		SEND_VA_GeneralTrans_RS nbRs = new SEND_VA_GeneralTrans_RS();
		SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
		SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
		SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
		SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
		msgrs.setHeader(header);
		body.setOUTDATE(" ");
		body.setOUTTIME(" ");
		body.setFEP_EJNO(" ");
		body.setTXNSTAN(" ");
		body.setCUSTOMERID(" ");
		body.setTXNTYPE("RQ");
		body.setFSCODE(" ");
		body.setTRANSAMT("0");
		body.setAEIPYTP(" ");
		body.setTAXIDNO(" ");
		body.setAEIPCRBK(" ");
		body.setCLACTNO(" ");
		body.setRSPRESULT(" ");
		body.setTXN_DATETIME(" ");
		body.setTERMINAL_TYPE(" ");
		body.setTERMINALID(" ");
		RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmVAReq().getBody().getRq().getSvcRq();
		body.setREPLYDATA(new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA());
		if (tita.getVACATE() != null && "10".equals(tita.getVACATE())){
			//2566-10
			body.getREPLYDATA().setAELFTP(" ");
			body.getREPLYDATA().setRESULT(" ");
			body.getREPLYDATA().setACRESULT(" ");
			body.getREPLYDATA().setACSTAT(" ");
			body.getREPLYDATA().setAEIPYUES(" ");
			body.getREPLYDATA().setCHKCELL(" ");
			body.getREPLYDATA().setFILLER(" ");
		}else {
			//2566-02
			body.getREPLYDATA().setPAYUNTNO(" ");
			body.getREPLYDATA().setTAXTYPE(" ");
			body.getREPLYDATA().setPAYFEENO(" ");
			body.getREPLYDATA().setFILLER1(" ");
		}
		msgrs.setSvcRs(body);
		rsbody.setRs(msgrs);
		nbRs.setBody(rsbody);

		header.setCLIENTTRACEID(vaheader.getCLIENTTRACEID());
		header.setCHANNEL(vaheader.getCHANNEL());
		header.setMSGID(vaheader.getMSGID());
		header.setCLIENTDT(vaheader.getCLIENTDT());
		header.setSYSTEMID("ATM");
		header.setSTATUSCODE("T099");
		header.setSEVERITY("ERROR");
		header.setSTATUSDESC("發生exception");

		ResStr = XmlUtil.toXML(nbRs);
		return ResStr;
	}
}
