package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syscom.fep.mybatis.model.Inbkpend;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

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
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author ChenYu
 */
public class AA2573 extends INBKAABase {
	// "共用變數宣告"
	FEPReturnCode _rtnCode = CommonReturnCode.Normal;

	Inbkpend aINBKPEND = new Inbkpend();
	InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	FeptxnExt desFeptxn = new FeptxnExt();

	// "建構式"
	// ''' <summary>
	// ''' AA的建構式,在這邊初始化及設定其他相關變數
	// ''' </summary>
	// ''' <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
	// ''' <remarks>
	// ''' </remarks>
	public AA2573(FISCData txnData) throws Exception {
		super(txnData);
	}

	// "AA進入點主程式"
	// ''' <summary>
	// ''' 程式進入點
	// ''' </summary>
	// ''' <returns>Response電文</returns>
	// ''' <remarks></remarks>
	@Override
	public String processRequestData() {
		try {
			// 準備跨行延遲交易檔初始資料(INBKPEND)
			_rtnCode = prepareInbkpend();
			// 組送財金Request 電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareForFisc();
			}
			// 新增跨行延遲交易檔(INBKPEND)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = insertInbkpend();
			}
			// 送REQ電文至財金並等待回應
			if (_rtnCode == CommonReturnCode.Normal) {
				RefBase<Inbkpend> inbkpendRefBase = new RefBase<>(aINBKPEND);
				_rtnCode = getFiscBusiness().sendPendingRequestToFISC(inbkpendRefBase);
				aINBKPEND = inbkpendRefBase.get();
			}
			// 檢核財金Response電文 Header
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processResponse();
			}
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
	// ''' <summary>
	// ''' Prepare 跨行延遲交易檔初始資料(INBKPEND)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <modify>
	// ''' <modifier>HusanYin</modifier>
	// ''' <reason>修正Const RC</reason>
	// ''' <date>2010/11/25</date>
	// ''' </modify>
	private FEPReturnCode prepareInbkpend() {
		try {
			aINBKPEND.setInbkpendTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			aINBKPEND.setInbkpendEjfno(TxHelper.generateEj()); // 電子日志序號
			aINBKPEND.setInbkpendTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))); // 交易時間
			aINBKPEND.setInbkpendStan(getFiscBusiness().getStan());
			aINBKPEND.setInbkpendBkno(SysStatus.getPropertyValue().getSysstatHbkno());// 交易啟動銀行
			aINBKPEND.setInbkpendDesBkno(SysStatus.getPropertyValue().getSysstatFbkno());// 交易接收銀行
			aINBKPEND.setInbkpendReqRc(NormalRC.FISC_REQ_RC);
			aINBKPEND.setInbkpendPcode(getFiscReq().getProcessingCode());
			aINBKPEND.setInbkpendTxAmt(BigDecimal.valueOf(Double.parseDouble(getFiscReq().getTxAmt())));

			aINBKPEND.setInbkpendTroutbkno(getFiscReq().getTroutBkno());
			aINBKPEND.setInbkpendTroutActno(getFiscReq().getTroutActno());
			aINBKPEND.setInbkpendOriTxDate(CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, "0")));
			aINBKPEND.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
			aINBKPEND.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));

			aINBKPEND.setInbkpendAtmno(getFiscReq().getATMNO());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareInbkpend");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode insertInbkpend() {
		try {
			aINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
			aINBKPEND.setInbkpendPending((short) 1); // PENDING
			aINBKPEND.setInbkpendFiscTimeout(DbHelper.toShort(true));
			aINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(FISCReturnCode.FISCTimeout.getValue()), 4, '0'));
			if (dbINBKPEND.insertSelective(aINBKPEND) != 1) {
				return IOReturnCode.INBKPENDInsertError;
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

	// 準備財的APData
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper;
		try {
			getFiscReq().setTxAmt(String.valueOf(aINBKPEND.getInbkpendTxAmt()));
			getFiscReq().setATMNO(aINBKPEND.getInbkpendAtmno());
			if ("2549".equals(getFiscReq().getProcessingCode())) {
				getFiscReq().setMerchantId(aINBKPEND.getInbkpendTroutActno());
			}
			getFiscReq().setTroutActno(aINBKPEND.getInbkpendTroutActno());
			getFiscReq().setOriStan(StringUtils.join(aINBKPEND.getInbkpendOriBkno(), aINBKPEND.getInbkpendOriStan()));
			getFiscReq().setREMARK(null);
			getFiscReq().setTroutBkno(null);
			getFiscReq().setDueDate(null);

			rtnCode = prepareDesFeptxn();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			encHelper = new ENCHelper(desFeptxn, getTxData());

			// 產生MAC
			RefString mac = new RefString(getFiscReq().getMAC());
			rtnCode = encHelper.makeFISCICMAC("0200", mac);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			getFiscReq().setMAC(mac.get());

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
			rtnCode = encHelper.checkFISCICMAC("0210", getFiscRes().getMAC());
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
		} finally {
			dbINBKPEND.updateByPrimaryKeySelective(aINBKPEND);
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode prepareDesFeptxn() {
		try {
			desFeptxn.setFeptxnTxDate(aINBKPEND.getInbkpendTxDate());
			desFeptxn.setFeptxnBkno(aINBKPEND.getInbkpendBkno());
			desFeptxn.setFeptxnStan(aINBKPEND.getInbkpendStan());
			desFeptxn.setFeptxnDesBkno(aINBKPEND.getInbkpendOriBkno());
			desFeptxn.setFeptxnOriStan(aINBKPEND.getInbkpendOriStan());
			desFeptxn.setFeptxnReqDatetime(StringUtils.join(aINBKPEND.getInbkpendTxDate(), aINBKPEND.getInbkpendTxTime()));
			desFeptxn.setFeptxnTbsdyFisc(aINBKPEND.getInbkpendOriTxDate());
			desFeptxn.setFeptxnReqRc(aINBKPEND.getInbkpendReqRc());
			desFeptxn.setFeptxnTxAmt(aINBKPEND.getInbkpendTxAmt());
			desFeptxn.setFeptxnTxAmtAct(aINBKPEND.getInbkpendTxAmt());

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareDesFeptxn");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
