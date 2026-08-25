package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;

/**
 * @author ChenYu
 */
public class AA2574 extends INBKAABase {
	private Inbkpend defInbkpend;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	// private InbkpendMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendMapper.class);
	private FeptxnExt desFEPTXN = new FeptxnExt();

	// "建構式"
	public AA2574(FISCData txnData) throws Exception {
		super(txnData);
	}

	// "AA進入點主程式"
	@Override
	public String processRequestData() {
		FEPReturnCode rtnCode2 = CommonReturnCode.Normal;
		try {
			defInbkpend = new Inbkpend();
			// 檢核財金電文
			rtnCode2 = processRequestHeader();
			if (rtnCode2 != CommonReturnCode.Normal) {
				return "";
			}
			// Prepare 交易記錄初始資料(INBKPEND)
			rtnCode2 = prepareINBKPEND();
			if (rtnCode2 == CommonReturnCode.Normal) {
				rtnCode2 = getFiscBusiness().prepareFEPTXN_IC();
			}
			// 檢核原交易記錄 FEPTXN
			if (rtnCode2 != CommonReturnCode.Normal) {
				return "";
			}
			_rtnCode = addTxData();

			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = checkFeptxn();
			}
			// '產生財金回應訊息電文()
			// 'Fly 2019/04/09 異常仍需更新欄位
			_rtnCode = prepareForFISC();
			// 需顯示交易成功訊息於EMS
			getLogContext().setRemark("defINBKPEND.INBKPEND_PCODE =" + defInbkpend.getInbkpendPcode() + ";defINBKPEND.INBKPEND_REP_RC=" + defInbkpend.getInbkpendRepRc() + ";");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());

			if (NormalRC.FISC_OK.equals(defInbkpend.getInbkpendRepRc())) {
				FEPReturnCode infoRC = null;
				getLogContext().setpCode(defInbkpend.getInbkpendPcode());
				getLogContext().setDesBkno(defInbkpend.getInbkpendDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("3");// INBK
				// Fly 2019/01/17 補上處理結果
				getLogContext().setMessageParm13(defInbkpend.getInbkpendPrcResult());
				infoRC = FISCReturnCode.FISCICReverseNotice;
				getLogContext().setProgramName(ProgramName);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(infoRC, getLogContext()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());

				getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
			} else {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
			}
			updateTxData();
			// '送回覆電文到財金(SendToFISC)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		} finally {
			getFiscRes().setResponseCode(MathUtil.toString(BigDecimal.valueOf(_rtnCode.getValue()), "0000"));
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	// "本支AA共用之sub routine"
	// 拆解並檢核由財金發動的Request電文
	private FEPReturnCode processRequestHeader() {
		_rtnCode = getFiscBusiness().checkHeader(getFiscReq(), false);
		if (_rtnCode == FISCReturnCode.MessageTypeError || _rtnCode == FISCReturnCode.TraceNumberDuplicate || _rtnCode == FISCReturnCode.OriginalMessageError
				|| _rtnCode == FEPReturnCode.STANError || _rtnCode == FISCReturnCode.CheckBitMapError || _rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
			return _rtnCode;
		}
		if (_rtnCode != CommonReturnCode.Normal) {
			return CommonReturnCode.Normal;
		}
		return _rtnCode;
	}

	// 跨行延遲交易檔初始資料(INBKPEND)
	private FEPReturnCode prepareINBKPEND() {
		try {
			defInbkpend.setInbkpendTxDate(CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getTxnInitiateDateAndTime().substring(0, 6), 7, "0")));
			defInbkpend.setInbkpendTxTime(getFiscReq().getTxnInitiateDateAndTime().substring(6, 12)); // 交易時間
			defInbkpend.setInbkpendEjfno(getTxData().getEj()); // 電子日誌序號
			defInbkpend.setInbkpendStan(getFiscReq().getSystemTraceAuditNo());
			defInbkpend.setInbkpendBkno(getFiscReq().getTxnSourceInstituteId().substring(0, 3)); // 交易啟動銀行
			defInbkpend.setInbkpendDesBkno(getFiscReq().getTxnDestinationInstituteId().substring(0, 3)); // 交易接收銀行
			defInbkpend.setInbkpendAtmno(getFiscReq().getATMNO()); // 櫃員機代號
			defInbkpend.setInbkpendSubsys(getTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
			defInbkpend.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // F1 (FISC REQUEST)
			defInbkpend.setInbkpendPcode(getFiscReq().getProcessingCode());
			defInbkpend.setInbkpendReqRc(getFiscReq().getResponseCode());
			defInbkpend.setInbkpendOriTxDate(getFiscReq().getTxDatetimeFisc().substring(0, 8));
			defInbkpend.setInbkpendTroutbkno(getFiscReq().getTxnDestinationInstituteId().substring(0, 3));

			if (StringUtils.isNotBlank(getFiscReq().getTxAmt())) {
				defInbkpend.setInbkpendTxAmt(new BigDecimal(getFiscReq().getTxAmt())); // 交易金額
			}
			if (StringUtils.isNotBlank(getFiscReq().getOriStan())) {
				defInbkpend.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
				defInbkpend.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));
			}
			defInbkpend.setInbkpendTroutActno(getFiscReq().getTroutActno());
			defInbkpend.setInbkpendPrcResult(getFiscReq().getRsCode()); // 處理結果

			if (dbINBKPEND.insertSelective(defInbkpend) < 1) {
				return IOReturnCode.INBKPENDInsertError;
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareINBKPEND");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	// 檢核原交易記錄 FEPTXN
	private FEPReturnCode checkFeptxn() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = null;
		try {
			rtnCode = prepareDesFeptxn();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			encHelper = new ENCHelper(desFEPTXN, getTxData());
			rtnCode = encHelper.checkFISCICMAC("0200", getFiscReq().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode; // 訊息押碼錯誤
			}

			Inbkpend tempInbk = new Inbkpend();
			tempInbk.setInbkpendOriBkno(defInbkpend.getInbkpendOriBkno());
			tempInbk.setInbkpendOriStan(defInbkpend.getInbkpendOriStan());
			// List getOriDataByPcodes = new ArrayList<>();
			// String arry = getOriDataByPcodes.toArray(new String);
			// InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
			if (dbINBKPEND.getOriDataByPcodes(tempInbk).size() <= 0) {
				getLogContext().setRemark("2573/2549交易不存在");
				logMessage(Level.DEBUG, getLogContext());
				// 'Fly 2019/01/17 不再送Garbled
				// 'fiscBusiness.SendGarbledMessage(fiscReq.EJ, FEPReturnCode.OriginalMessageError, fiscReq)
				return FEPReturnCode.MessageFormatError;
			} else {
				// 增加檢核ICTLTCN
				Ictltxn tempIctl = new Ictltxn();
				IctltxnExtMapper dbIctl = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
				tempIctl.setIctltxnTxDate(defInbkpend.getInbkpendOriTxDate());
				tempIctl.setIctltxnBkno(defInbkpend.getInbkpendOriBkno());
				tempIctl.setIctltxnStan(defInbkpend.getInbkpendOriStan());
				// IctltxnExtMapper ictltxnExtMapper = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
				tempIctl = dbIctl.getIctltxn(tempIctl);
				if (tempIctl == null) {
					getLogContext().setRemark("查無原交易");
					logMessage(Level.DEBUG, getLogContext());
					return FEPReturnCode.MessageFormatError;
				}
				// 2021-10-09 Richard modified start
				boolean originalDataMatched = true;
				if (tempIctl.getIctltxnSetAmt().compareTo(getFeptxn().getFeptxnTxAmtAct())!=0) {
					getLogContext().setRemark(StringUtils.join("與原交易資料不符",
							", ICTLTXN.ICTLTXN_SET_AMT = [", tempIctl.getIctltxnSetAmt(), "]",
							", FEPTXN.FEPTXN_TX_AMT_ACT = [", getFeptxn().getFeptxnTxAmtAct(), "]"));
					originalDataMatched = false;
				} else if (!tempIctl.getIctltxnTxDatetimeFisc().equals(getFeptxn().getFeptxnTxDatetimeFisc())) {
					getLogContext().setRemark(StringUtils.join("與原交易資料不符",
							", ICTLTXN.ICTLTXN_TX_DATETIME_FISC = [", tempIctl.getIctltxnTxDatetimeFisc(), "]",
							", FEPTXN.FEPTXN_TX_DATETIME_FISC = [", getFeptxn().getFeptxnTxDatetimeFisc(), "]"));
					originalDataMatched = false;
				} else if (!tempIctl.getIctltxnAtmno().equals(getFeptxn().getFeptxnAtmno())) {
					getLogContext().setRemark(StringUtils.join("與原交易資料不符",
							", ICTLTXN.ICTLTXN_ATMNO = [", tempIctl.getIctltxnAtmno(), "]",
							", FEPTXN.FEPTXN_ATMNO = [", getFeptxn().getFeptxnAtmno(), "]"));
					originalDataMatched = false;
				} else if (!tempIctl.getIctltxnTroutActno().equals(getFeptxn().getFeptxnTroutActno())) {
					getLogContext().setRemark(StringUtils.join("與原交易資料不符",
							", ICTLTXN.ICTLTXN_TROUT_ACTNO = [", tempIctl.getIctltxnTroutActno(), "]",
							", FEPTXN.FEPTXN_TROUT_ACTNO = [", getFeptxn().getFeptxnTroutActno(), "]"));
					originalDataMatched = false;
				}
				if (!originalDataMatched) {
					logMessage(Level.DEBUG, getLogContext());
					return FEPReturnCode.MessageFormatError;
				}
				// 2021-10-09 Richard modified end
				defInbkpend.setInbkpendOriPcode(tempInbk.getInbkpendPcode());
				tempInbk.setInbkpendPrcResult(defInbkpend.getInbkpendPrcResult());
				dbINBKPEND.updateByPrimaryKey(tempInbk);
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".checkFeptxn");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 新增交易記錄
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// DBFepTxn.Database.BeginTransaction();
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				// DBFepTxn.Database.RollbackTransaction();
				transactionManager.rollback(txStatus);
				return rtnCode;
			}
			// DBFepTxn.Database.CommitTransaction();
			transactionManager.commit(txStatus);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 產生財金回應訊息電文
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = null;
		try {
			if (_rtnCode != CommonReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName);
				defInbkpend.setInbkpendRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			} else {
				defInbkpend.setInbkpendRepRc(NormalRC.FISC_OK);
			}
			rtnCode = getFiscBusiness().preparePendHeader("0210", defInbkpend);
			if (rtnCode != CommonReturnCode.Normal) {
				defInbkpend.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode), 4, "0"));
				// 更新交易記錄(INBKPEND)
				defInbkpend.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // "F2"(FISC RESPONSE)
				defInbkpend.setInbkpendPending((short) 2); // (解除PENDING)
				dbINBKPEND.updateByPrimaryKey(defInbkpend);
				return rtnCode;
			}
			getFiscRes().setTxDatetimeFisc(getFiscReq().getTxDatetimeFisc());
			getFiscRes().setOriStan(getFiscReq().getOriStan());

			// 產生訊息押碼(MAC)
			encHelper = new ENCHelper(desFEPTXN, getTxData());
			RefString mac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFISCICMAC("0210", mac);
			getFiscRes().setMAC(mac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscRes().setMAC("00000000");
				defInbkpend.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, "0"));
			}
			// Make Bit Map
			rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
			if (rtnCode != CommonReturnCode.Normal) {
				defInbkpend.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, "0"));
				getFiscRes().setBitMapConfiguration(StringUtils.leftPad("0", 16, "0"));
			}
			rtnCode = getFiscRes().makeFISCMsg();
			if (rtnCode != CommonReturnCode.Normal) {
				defInbkpend.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, "0"));
				// 更新交易記錄(INBKPEND)
				defInbkpend.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // "F2"(FISC RESPONSE)
				defInbkpend.setInbkpendPending((short) 2); // (解除PENDING)
				dbINBKPEND.updateByPrimaryKey(defInbkpend);
				return rtnCode;
			}
			// 更新交易記錄(INBKPEND)
			defInbkpend.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // "F2"(FISC RESPONSE)
			defInbkpend.setInbkpendPending((short) 2); // (解除PENDING)
			dbINBKPEND.updateByPrimaryKeySelective(defInbkpend);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareForFISC");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode prepareDesFeptxn() {
		try {
			desFEPTXN.setFeptxnTxDate(defInbkpend.getInbkpendTxDate());
			desFEPTXN.setFeptxnBkno(defInbkpend.getInbkpendBkno());
			desFEPTXN.setFeptxnStan(defInbkpend.getInbkpendStan());
			desFEPTXN.setFeptxnDesBkno(defInbkpend.getInbkpendOriBkno());
			desFEPTXN.setFeptxnOriStan(defInbkpend.getInbkpendOriStan());
			desFEPTXN.setFeptxnReqDatetime(defInbkpend.getInbkpendTxDate() + defInbkpend.getInbkpendTxTime());
			desFEPTXN.setFeptxnTxAmtAct(defInbkpend.getInbkpendTxAmt());
			desFEPTXN.setFeptxnReqRc(defInbkpend.getInbkpendReqRc());

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareDesFeptxn");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// UpdateTxData部份
	private FEPReturnCode updateTxData() {
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
		getFiscBusiness().getFeptxn().setFeptxnRepRc(defInbkpend.getInbkpendRepRc());
		getFiscBusiness().getFeptxn().setFeptxnPending(defInbkpend.getInbkpendPending());
		getFiscBusiness().getFeptxn().setFeptxnNoticeId(defInbkpend.getInbkpendOriPcode());
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
		getFiscBusiness().updateTxData();
		return CommonReturnCode.Normal;
	}

}
