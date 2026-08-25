package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * 處理財金發動資金調撥Confirm電文
 * 
 * @author Sarah
 *
 */
public class FXConfirmI extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private boolean isExitProgram = false;

	public FXConfirmI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金電文
			_rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
			String sFiscRc = TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext());
	        if("10".equals(sFiscRc.substring(0, 2))) {
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), _rtnCode, getFiscCon());
				return StringUtils.EMPTY;
			}
			getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
			getTxData().setFeptxn(getFiscBusiness().getFeptxn());

			// 2.商業邏輯檢核＆電文Body檢核
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode= checkBusinessRule();
				if(isExitProgram){
					return StringUtils.EMPTY;
				}
			}

			// 3.更新交易記錄 (FEPTXN)
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = updateTxData();
				if(_rtnCode != FEPReturnCode.Normal){
					return StringUtils.EMPTY;
				}
			}

			// 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = processAPTOTSendToCBSASC();
			}

			// 5.label_END_OF_FUNC:

			// 6.更新交易記錄
			if (getFiscBusiness().getFeptxn() != null) {
				_rtnCode = updateFEPTXN();
			}

			// 7.發送推播或簡訊或Email
			if (getFiscBusiness().getFeptxn() != null
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                    && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
            	getFiscBusiness().sendToNotify();
            }

			// 8.判斷是否需傳送2160電文給財金
			/* 2025/7/1 修改 for 傳送 2160電文給財金 */
			/* “A”: 成功或失敗均需傳送 	*/
			/* “Y”: 僅成功交易傳送  	*/
			if( "4001".equals(getFiscBusiness().getFeptxn().getFeptxnConRc()) ){
				if( "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) || "Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) ){
					_rtnCode = insertINBK2160();
				}
			}else{
				if( "A".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160()) ){
					_rtnCode = insertINBK2160();
				}
			}

		} catch (Exception e) {
			_rtnCode = FEPReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		}finally {
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return StringUtils.EMPTY;
	}

	private FEPReturnCode SendToCBSEnd() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		/*沖轉主機帳務*/
		String AATxTYPE = "";
		String AATxRs = "N";
		try {
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
			rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
			if(rtnCode != FEPReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setRemark("通知主機交易結束時發生錯誤!!");
				sendEMS(getLogContext());
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendToCBSEnd");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 2. checkBusinessRule: 商業邏輯檢核＆電文Body檢核
	 * @return FEPReturnCode
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			/* 9/9 修改, 改抓 FEPTXN_REQ_DATETIME欄位 */
			String aa = (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6, 12);
			if (!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
					(!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0, 3))) ||
					(StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
					|| (StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmt()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
				return FEPReturnCode.OriginalMessageDataError;
			}

			//檢核交易是否未完成
			if (!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
				/* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}

			/* 9/22 修改 for CON 送2次 */
			if (!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
				this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
				isExitProgram = true;
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}

            /* 2024/7/1 以FEPTXN的PK讀取FEPTXNTCB Data */
            Feptxntcb tempFeptxntcb = getFiscBusiness().checkFeptxntcbData(getFiscBusiness().getFeptxn());
            getFiscBusiness().setFeptxntcb(tempFeptxntcb);
            getTxData().setFeptxntcb(tempFeptxntcb);
            if(getFiscBusiness().getFeptxntcb() == null) {
            	return FEPReturnCode.MessageFormatError;
            }

			//檢核 MAC
			getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
			/* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			//註解合庫開發環境暫時不測試
			rtnCode = encHelper.checkFiscMac(this.getFiscCon().getMessageType(), getFiscCon().getMAC());
			this.logContext.setRemark("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(this.logContext);

			if (rtnCode != FEPReturnCode.Normal) {
				this.getFeptxn().setFeptxnConRc(null);
				return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 3.updateTxData: 更新交易記錄(FEPTXN)
	 * @return FEPReturnCode
	 */
	private FEPReturnCode updateTxData() {
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); // 'Accept-Reverse
				}
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());

			/* 2025/5/9 修改 for  CON電文送2次*/
//			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
			int i = this.feptxnDao.updateConfirmByPrimaryKey(getFiscBusiness().getFeptxn());
			if (i <= 0) {
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setRemark("收到CON電文無法更新FEPTXN，EJFNO:"+getFiscBusiness().getFeptxn().getFeptxnEjfno());
				sendEMS(getLogContext());
				return IOReturnCode.FEPTXNUpdateError;
			}
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(this.logContext);
			return FEPReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
	}


	/**
	 * 4.processAPTOTSendToCBSASC: 判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC
	 * @return FEPReturnCode
	 */
	private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String AATxTYPE = "";
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /* +REP */
			if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) { /* -CON */
				/*沖轉跨行代收付*/
				rtnCode = getFiscBusiness().processAptot(true);
				getLogContext().setProgramName(ProgramName);
				/* FEP通知主機交易結束 */
				rtnCode = this.SendToCBSEnd();
				//由GetMessageFromFEPReturnCode執行 SendEMS
				TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
			} else {  /* +CON */
				/* 轉入行為本行, 送主機入帳 */
				AATxTYPE = "1";
				String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
				rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
			}
		}
		return rtnCode;
	}


	/**
	 * 6.updateFEPTXN: 更新交易記錄
	 * @return FEPReturnCode
	 */
	private FEPReturnCode updateFEPTXN() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		if (getFiscBusiness().getFeptxn().getFeptxnAaRc().equals(FEPReturnCode.Normal.getValue())) {
			if (this._rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());
			}
		}
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
		/* 2025/10/9 修改 for 原存CON交易主機處理異常 */
		if ( "PEND".equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc()) ) {
			/* 2025/10/7 修改 for 合庫要求, FEP重發pending */
			getFiscBusiness().getFeptxn().setFeptxnChannelEjfno(String.valueOf(getFiscBusiness().getFeptxn().getFeptxnTraceEjfno()));
			getFiscBusiness().getFeptxn().setFeptxnPending( (short) 1 );
			getFiscBusiness().getFeptxn().setFeptxnMsgflow( FEPTxnMessageFlow.FISC_Response);
			getFiscBusiness().getFeptxn().setFeptxnTxrust("B");
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(0);
		}

		rtnCode = getFiscBusiness().updateTxData();
		return rtnCode;
	}

	/**
	 * 8. 	判斷是否需傳送2160電文給財金
	 * @return FEPReturnCode
	 */
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().prepareInbk2160();
			if(rtnCode != FEPReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setProgramName(ProgramName + ".insertINBK2160");
				getLogContext().setRemark("寫入INBK2160發生錯誤!!");
				logMessage(getLogContext());
				return FEPReturnCode.INBK2160InsertError;
			}else{
				return FEPReturnCode.Normal;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".insertINBK2160");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}
}
