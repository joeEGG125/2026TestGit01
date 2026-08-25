package com.syscom.fep.server.aa.inbk;

import com.ibm.db2.jcc.t4.ai;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Zone;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author ChenYu
 */
public class AA2140 extends INBKAABase {
	// "共用變數宣告"
	FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	Inbkpend aINBKPEND = new Inbkpend();
	InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	Feptxn desFeptxn = new FeptxnExt();
	Feptxn pendFEPTXN = new FeptxnExt();

	// "建構式"
	public AA2140(FISCData txnData) throws Exception {
		super(txnData);
	}

	// "AA進入點主程式"
	@Override
	public String processRequestData() {
		try {
			// 準備跨行延遲交易檔初始資料(INBKPEND)&FEPTXN
			_rtnCode = prepareInbkpend();
			// 組送財金Request 電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareForFisc();
			}
			// 新增跨行延遲交易檔(INBKPEND & FEPTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = insertInbkpend();
			}
			// 送REQ電文至財金並等待回應
			if (_rtnCode == CommonReturnCode.Normal) {
				//RefBase<Inbkpend> inbkpendRefBase = new RefBase<>(new Inbkpend());
				RefBase<Inbkpend> inbkpendRefBase = new RefBase<>(aINBKPEND);
				_rtnCode = getFiscBusiness().sendPendingRequestToFISC(inbkpendRefBase);
				
				//2024-11-04 Troy 調整 Inbkpend 的處理流程
				if (_rtnCode == CommonReturnCode.Normal) {
					aINBKPEND.setInbkpendMsgflow(inbkpendRefBase.get().getInbkpendMsgflow());
					aINBKPEND.setInbkpendPending(inbkpendRefBase.get().getInbkpendPending());
					aINBKPEND.setInbkpendFiscTimeout(inbkpendRefBase.get().getInbkpendFiscTimeout());
					aINBKPEND.setInbkpendAaRc(inbkpendRefBase.get().getInbkpendAaRc());
				}
			}
			// 檢核財金Response電文 Header
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processResponse();
			}
			//更新跨行延遲交易檔(INBKPEND & FEPTXN)
			updateInbkpend();
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramName(ProgramName);
			if (_rtnCode != CommonReturnCode.Normal) {
				getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			} else {
				getTxData().getTxObject().setDescription(getFiscRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(getFiscRes().getResponseCode(), FEPChannel.FISC, getLogContext()));
			}
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscRes().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(getTxData().getTxObject().getDescription());
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	// "本支AA共用之sub routine"
	// Prepare 跨行延遲交易檔初始資料(INBKPEND)
	private FEPReturnCode prepareInbkpend() {
		try {
			aINBKPEND.setInbkpendTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			aINBKPEND.setInbkpendEjfno(TxHelper.generateEj()); // 電子日志序號
			aINBKPEND.setInbkpendTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))); // 交易時間
			aINBKPEND.setInbkpendStan(getFiscBusiness().getStan());
			aINBKPEND.setInbkpendBkno(SysStatus.getPropertyValue().getSysstatHbkno());// 交易啟動銀行
			aINBKPEND.setInbkpendDesBkno(SysStatus.getPropertyValue().getSysstatFbkno());// 交易接收銀行
			aINBKPEND.setInbkpendReqRc(NormalRC.FISC_REQ_RC);
			aINBKPEND.setInbkpendPcode("2140");
			// 2015/06/12 Modify by Ruling for 跨行外幣提款
			aINBKPEND.setInbkpendTxAmt(new BigDecimal(getFiscReq().getTxAmt()));
			// .INBKPEND_TX_AMT = CType(CDbl(fiscReq.TX_AMT) * 100, Decimal)
			aINBKPEND.setInbkpendTroutbkno(getFiscReq().getTroutBkno());
			aINBKPEND.setInbkpendTroutActno(getFiscReq().getTroutActno());
			aINBKPEND.setInbkpendOriTxDate(CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, "0")));	
			aINBKPEND.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
			aINBKPEND.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));
			aINBKPEND.setInbkpendEcInstruction(getFiscReq().getREMARK());
			
			pendFEPTXN.setFeptxnTxDate(aINBKPEND.getInbkpendTxDate());
			pendFEPTXN.setFeptxnTxTime(aINBKPEND.getInbkpendTxTime());
			pendFEPTXN.setFeptxnBkno(aINBKPEND.getInbkpendBkno());
			pendFEPTXN.setFeptxnStan(aINBKPEND.getInbkpendStan());
			pendFEPTXN.setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
			pendFEPTXN.setFeptxnAtmno(aINBKPEND.getInbkpendAtmno());
			pendFEPTXN.setFeptxnTxAmt(aINBKPEND.getInbkpendTxAmt());
			pendFEPTXN.setFeptxnEjfno(aINBKPEND.getInbkpendEjfno());
			pendFEPTXN.setFeptxnPcode(aINBKPEND.getInbkpendPcode());
			pendFEPTXN.setFeptxnReqRc(aINBKPEND.getInbkpendReqRc());
			pendFEPTXN.setFeptxnSubsys((short)1);
			pendFEPTXN.setFeptxnFiscFlag((short)1);
			Zone defZone = getFiscBusiness().getZoneByZoneCode("TWN");
			if(defZone != null) {
				pendFEPTXN.setFeptxnTbsdy(defZone.getZoneTbsdy());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareInbkpend");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	// Insert 跨行延遲交易檔(INBKPEND)
	private FEPReturnCode insertInbkpend() {
		try {
			aINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
			aINBKPEND.setInbkpendPending((short) 1); // PENDING
			aINBKPEND.setInbkpendFiscTimeout(DbHelper.toShort(true));
			aINBKPEND.setInbkpendAaRc("0601"); // TIMEOUT
			if (dbINBKPEND.insertSelective(aINBKPEND) != 1) {
				return IOReturnCode.INBKPENDInsertError;
			}
			if(feptxnDao.insertSelective(pendFEPTXN) < 1) {
				return IOReturnCode.FEPTXNInsertError;
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".insertInbkpend");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 組傳送財金Request電文(OC035)
	private FEPReturnCode prepareForFisc() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// 電文Header
		rtnCode = getFiscBusiness().preparePendHeader("0200", aINBKPEND);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 電文Body
		rtnCode = prepareBody();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper;
		try {
			getFiscReq().setTxAmt(aINBKPEND.getInbkpendTxAmt().toString());
			getFiscReq().setTroutBkno(StringUtils.rightPad(aINBKPEND.getInbkpendTroutbkno(), 7, "0"));
			getFiscReq().setMODE(aINBKPEND.getInbkpendEcInstruction());
			getFiscReq().setDueDate(CalendarUtil.adStringToROCString(aINBKPEND.getInbkpendOriTxDate()).substring(1, 7)); // 2021-10-09 Richard modified

			getFiscReq().setTrinActno(aINBKPEND.getInbkpendTroutActno());
			getFiscReq().setOriStan(aINBKPEND.getInbkpendOriBkno() + aINBKPEND.getInbkpendOriStan());
			getFiscReq().setREMARK(null);
			getFiscReq().setTroutActno(null);

			// Prepare FEPTXN for DES, pls new FEPTXN just for DES
			rtnCode = prepareDesFeptxn();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			encHelper = new ENCHelper(desFeptxn, getTxData());

			// 產生MAC
			RefString mac = new RefString(getFiscReq().getMAC());
			rtnCode = encHelper.makeFISCMACPend("00", mac);
			getFiscReq().setMAC(mac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 產生Bitmap
			rtnCode = getFiscBusiness().makeBitmap(getFiscReq().getMessageType(), getFiscReq().getProcessingCode(), MessageFlow.Request);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareBody");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// 處理財金Response電文(OC036)
	private FEPReturnCode processResponse() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper;

		try {
			// 檢核Header
			rtnCode = getFiscBusiness().checkHeader(getFiscRes(), false);
			if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError
					|| rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscRes());
				return rtnCode;
			}
			if (rtnCode != CommonReturnCode.Normal) {
				aINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode), 4, "0"));
				return rtnCode;
			} else {
				aINBKPEND.setInbkpendRepRc(getFiscRes().getResponseCode());
			}

			// 檢核MAC
			desFeptxn.setFeptxnRepRc(aINBKPEND.getInbkpendRepRc());
			encHelper = new ENCHelper(desFeptxn, getTxData());
			rtnCode = encHelper.checkFISCMACPend(getFiscRes().getMessageType().substring(2, 4), getFiscRes().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				rtnCode = ENCReturnCode.ENCCheckMACError;
				aINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode), 4, "0"));
				return rtnCode;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processResponse");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	// Prepare FEPTXN for DES
	private FEPReturnCode prepareDesFeptxn() {
		try {
			desFeptxn.setFeptxnTxDate(aINBKPEND.getInbkpendTxDate());
			desFeptxn.setFeptxnBkno(aINBKPEND.getInbkpendBkno());
			desFeptxn.setFeptxnStan(aINBKPEND.getInbkpendStan());
			desFeptxn.setFeptxnDesBkno(aINBKPEND.getInbkpendOriBkno());
			desFeptxn.setFeptxnOriStan(aINBKPEND.getInbkpendOriStan());
			desFeptxn.setFeptxnTbsdyFisc(aINBKPEND.getInbkpendOriTxDate());
			desFeptxn.setFeptxnRemark(aINBKPEND.getInbkpendEcInstruction());
			desFeptxn.setFeptxnReqRc(aINBKPEND.getInbkpendReqRc());

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareDesFeptxn");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
	
	private FEPReturnCode updateInbkpend() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			pendFEPTXN.setFeptxnMsgflow(aINBKPEND.getInbkpendMsgflow());
			pendFEPTXN.setFeptxnPending(aINBKPEND.getInbkpendPending());
			pendFEPTXN.setFeptxnFiscTimeout(aINBKPEND.getInbkpendFiscTimeout());
			pendFEPTXN.setFeptxnRepRc(aINBKPEND.getInbkpendRepRc());
			pendFEPTXN.setFeptxnAaRc(Integer.valueOf(aINBKPEND.getInbkpendAaRc()));
			pendFEPTXN.setFeptxnAaComplete((short) 1); /* AA Complete */
			if(_rtnCode != CommonReturnCode.Normal) {
				pendFEPTXN.setFeptxnTxrust("R"); /* 處理結果=Reject */
			}else {
				pendFEPTXN.setFeptxnTxrust("A"); /* 處理結果=成功 */
			}
			
			if(dbINBKPEND.updateByPrimaryKeySelective(aINBKPEND) <= 0 
					|| feptxnDao.updateByPrimaryKeySelective(pendFEPTXN) <= 0) {
				transactionManager.rollback(txStatus);
				return IOReturnCode.UpdateFail;
			}
			
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateInbkpend");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}
}
