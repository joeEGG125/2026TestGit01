package com.syscom.fep.server.aa.eatm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author Jaime
 */
public class EATMSelfIssueC extends ATMPAABase {
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	boolean isGoupdate = false;
	String AATxTYPE = "";

	public EATMSelfIssueC(ATMData txnData) throws Exception {
		super(txnData,"eatm");
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		try {
			// 1. CheckBusinessRule: 商業邏輯檢核
			this.checkBusinessRule();

			// 2. UpdateTxData: 更新交易記錄(FEPTxn)
			if(!isGoupdate) {
				this.updateTxData();

				// 3. 交易通知 (if need)
				this.sendToMailHunter();
			}

			// 4. Response:組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = this.response();

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return rtnMessage;
	}

	/**
	 * 1. CheckBusinessRule: 商業邏輯檢核
	 *
	 * @return
	 * @throws Exception
	 */
	private void checkBusinessRule() throws Exception {
		// 1.1 取得原交易之 FEPTXN
		//將原交易的feptxn取代掉本來的feptxn
		getATMBusiness().setFeptxn(getATMBusiness().eatm_checkConData());
		feptxn = getATMBusiness().getFeptxn();
		if (getATMBusiness().getFeptxn() == null) {
			rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
			sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
			isGoupdate = true;
			return; // GO TO 3 /* 組 ATM 回應電文 */
		}

		// 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
		rtnCode = getATMBusiness().prepareConFEPTXN();
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 2 /* 更新交易記錄 */
		}

		// 1.3 交易確認電文檢核 MAC
		/* 檢核 ATM 電文 MAC */
		if (getTxData().getMsgCtl().getMsgctlReqmacType() != null) {
			/* 檢核 ATM 電文 MAC */
			String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
			if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
				rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
				return; // GO TO 5 /* 更新交易記錄 */
			}
			String newMac = EbcdicConverter.fromHex(CCSID.English,StringUtils.substring(this.getTxData().getTxRequestMessage(), 742, 758));

			this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);
			logMessage(this.logContext);

			rtnCode = new ENCHelper(getTxData()).checkAtmMacNew("NEATM001",
					StringUtils.substring(this.getTxData().getTxRequestMessage(), 36, 742),
					newMac);
			this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
		}

	}

	/**
	 * 2. SendToCBS(if need)
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		switch (feptxn.getFeptxnTxCode()) {
		case "F1":
		case "F2":
		case "F3":
		case "F4":
		case "F5":
		case "F6":
		case "F7":
		case "W1":
		case "W2":
		case "W3":
		case "W4":
		case "WF":
		case "US":
		case "JP":
			// 提款確認電文, 如ATM送Con(-), 須組I002電文送往CBS主機
			AATxTYPE = ""; // 預設
			// ATM第二道為失敗電文且FEP紀錄CBS已記帳
			ATMGeneralRequest ATMRequest = getATMRequest();
			if ("SE".equals(ATMRequest.getMSGTYP()) && feptxn.getFeptxnAccType() == 1) {
				// ATM.REQ.STATUS[3:10] <> 0
				if (BigDecimal.ZERO
						.compareTo(new BigDecimal(StringUtils.substring(ATMRequest.getSTATUS(), 2, 12))) != 0) {
					AATxTYPE = "2"; // 上CBS沖正
				} else if (!"I".equals(feptxn.getFeptxnExcpCode())) { // ATM異常
					AATxTYPE = "5"; // 上CBS註記
				}
			}
			if (StringUtils.isNotBlank(AATxTYPE)) {
				/* 交易記帳處理 */
				String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
				feptxn.setFeptxnCbsTxCode(AA);
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
				rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 3. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {

		getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
		if(rtnCode != FEPReturnCode.Normal){
			getATMBusiness().getFeptxn().setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
					getTxData().getTxChannel(), getTxData().getLogContext()));
		}
		getATMBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			getATMBusiness().getFeptxn().setFeptxnTxrust("A"); /* Con(-), 處理結果=成功 */

		}

		// 回寫 FEPTXN
		/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
		FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			feptxnDao.updateByPrimaryKeySelective(getATMBusiness().getFeptxn()); // 更新資料
		} catch (Exception ex) {
			rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			sendEMS(getLogContext());
		}

		if (rtnCode2 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			this.feptxn.setFeptxnReplyCode("L013");
			sendEMS(getLogContext());
		}
	}

	/**
	 * 4. Response:組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @return
	 * @throws Exception
	 */
	private String response() throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			// 組 Header
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
			RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmbody = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();

            RefString rfs = new RefString();
			String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
					FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
			SEND_EATM_FSN_HEAD2 rs = new SEND_EATM_FSN_HEAD2();
			SEND_EATM_FSN_HEAD2_Body rsbody = new SEND_EATM_FSN_HEAD2_Body();
			SEND_EATM_FSN_HEAD2_Body_NS1MsgRs msgrs = new SEND_EATM_FSN_HEAD2_Body_NS1MsgRs();
			SEND_EATM_FSN_HEAD2_Body_MsgRs_Header header = new SEND_EATM_FSN_HEAD2_Body_MsgRs_Header();
			SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs();
			msgrs.setSvcRq(msgbody);
			msgrs.setHeader(header);
			rsbody.setRs(msgrs);
			rs.setBody(rsbody);

			header.setCLIENTTRACEID(atmheader.getCLIENTTRACEID());
			header.setCHANNEL("EAT");
			header.setMSGID(atmheader.getMSGID());
			header.setCLIENTDT(atmheader.getCLIENTDT());
			header.setSYSTEMID("FEP");
			if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
				header.setSTATUSCODE("4001");
			} else {
				header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
			}
			if(feptxn == null){ //原交易之 FEPTXN  is Nothing
				header.setSTATUSCODE("EF0305");
				header.setSTATUSDESC("OriginalMessageNotFound");
			}
			msgbody.setWSID(atmbody.getWSID());
			msgbody.setRECFMT("1");
			msgbody.setMSGCAT("F");
			msgbody.setMSGTYP("PC");
			msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
			msgbody.setTRANTIME(systemTime.substring(8,14)); // 系統時間
			msgbody.setTRANSEQ(atmbody.getTRANSEQ());
			msgbody.setTDRSEG(atmbody.getTDRSEG()); // 回覆FSN或FSE
			// PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
			// (FC1、P1)主機才有可能依據狀況要求吃卡
			msgbody.setPRCRDACT("0");
			if (feptxn == null) {
				msgbody.setRECFMT("0");
			}

			/* CALL ENC 取得MAC 資料 */
			String res = msgbody.makeMessage();
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
			rfs.set("");
			rtnCode = atmEncHelper.makeAtmMac("NEATM001", res, rfs);
			if (rtnCode != FEPReturnCode.Normal) {
				msgbody.setMACCODE("");
			} else {
				msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			msgbody.setOUTDATA(msgbody.makeMessage());
			rtnMessage = XmlUtil.toXML(rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 4. 交易通知 (if need)
	 *
	 * @return
	 * @throws Exception
	 */
	private void sendToMailHunter() {
		try {
			String noticeType = feptxn.getFeptxnNoticeType();
			if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc())&& "4001".equals(feptxn.getFeptxnConRc())) {
				switch (noticeType) {
				case "P": /* 送推播 */
					getATMBusiness().preparePush(this.feptxn);
					break;
				case "M": /* 簡訊 */
					getATMBusiness().prepareSms(this.feptxn);
					break;
				case "E": /* Email */
					getATMBusiness().prepareMail(this.feptxn);
					break;
				}
			}
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
			sendEMS(this.logContext);
		}
	}

}
