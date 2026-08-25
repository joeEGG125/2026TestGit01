package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.AllbankMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.NBAdapter;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralResponse;
import com.syscom.fep.vo.text.nb.request.CLARequest;
import com.syscom.fep.vo.text.nb.request.CLARequest.CLARq;
import com.syscom.fep.vo.text.nb.request.CLARequest.CLASvcRq;
import com.syscom.fep.vo.text.nb.request.PSNRequest;
import com.syscom.fep.vo.text.nb.request.PSNRequest.PSNRq;
import com.syscom.fep.vo.text.nb.request.PSNRequest.PSNSvcRq;
import com.syscom.fep.vo.text.nb.response.CLAResponse;
import com.syscom.fep.vo.text.nb.response.CLAResponse.CLARs;
import com.syscom.fep.vo.text.nb.response.CLAResponse.CLASvcRs;
import com.syscom.fep.vo.text.nb.response.PSNResponse;
import com.syscom.fep.vo.text.nb.response.PSNResponse.PSNRs;
import com.syscom.fep.vo.text.nb.response.PSNResponse.PSNSvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

public class Ncnb extends HostBase {
	private MessageBase _txData;

	public Ncnb(MessageBase txData) {
		super(txData);
		_txData = txData;
	}

	public final FEPReturnCode sendToNCNB(String action, String txndate) {
		FEPReturnCode rtn = FEPReturnCode.Abnormal;
		@SuppressWarnings("unused")
		String rtnData = "";
		NBAdapter nbAdapter = new NBAdapter(_txData);
		NBGeneral nbGeneralForReq = new NBGeneral();
		NBGeneral nbGeneralForRes = new NBGeneral();
		nbAdapter.setAction("NCCARD");
		try {
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatNb())) {// 暫停永豐網銀通道
//				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {// 跨行交易{
//					if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {// 原存行交易
//						getFeptxn().setFeptxnAscRc(TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.InterBankServiceStop.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
//					} else {
//						getFeptxn().setFeptxnAscRc(TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.SenderBankServiceStop.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
//					}
//				} else {
//					getFeptxn().setFeptxnAscRc(TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.WithdrawServiceStop.getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()));
//				}
//				getLogContext().setRemark("暫停永豐網銀通道");
//				logMessage(Level.DEBUG, getLogContext());
//				this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
//				return FEPReturnCode.Normal;
//			}

			switch (action) {
				case "2": {// 推撥服務通知
					nbGeneralForReq.getRequest().setMsgID("PushServiceNotification");
					if ("NCS".equals(getFeptxn().getFeptxnTxCode()) && StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId())) {
						switch (getFeptxn().getFeptxnNoticeId().trim()) {
							case "1":
								// 開啟無卡提款
								nbGeneralForReq.getRequest().setMsgType("EnableCardless");
								break;
							case "2":
								// 變更無卡提款密碼
								nbGeneralForReq.getRequest().setMsgType("UpdatedPwd");
								break;
							case "3":
								// 關閉無卡提款
								nbGeneralForReq.getRequest().setMsgType("DisableCardless");
								break;
						}
					} else {
						// 無卡提款
						nbGeneralForReq.getRequest().setMsgType("OverErrorTimes");
					}
					nbGeneralForReq.getRequest().setChlEJNo(
							"FEP" + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 12, '0'));
					nbGeneralForReq.getRequest().setChlSendTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
					nbGeneralForReq.getRequest().setSendMailToo("true");
					nbGeneralForReq.getRequest().setBankCode(SysStatus.getPropertyValue().getSysstatHbkno());
					nbGeneralForReq.getRequest().setIDNO(getFeptxn().getFeptxnIdno());
					nbGeneralForReq.getRequest().setATMNO(getFeptxn().getFeptxnAtmno());

					PSNRequest reqClass = new PSNRequest();

					reqClass.setRqHeader(new FEPRqHeader());
					reqClass.setSvcRq(new PSNSvcRq());
					reqClass.getSvcRq().setRq(new PSNRq());

					nbAdapter.setMessageToNB(reqClass.makeMessageFromGeneral(nbGeneralForReq));
					break;
				}
				case "3": {
					nbGeneralForReq.getRequest().setMsgID("CardlessApp");
					nbGeneralForReq.getRequest().setMsgType("UpdateSecurityCode");
					nbGeneralForReq.getRequest().setChlEJNo(
							"FEP" + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) + StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 12, '0'));
					nbGeneralForReq.getRequest().setChlSendTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
					nbGeneralForReq.getRequest().setBankCode(SysStatus.getPropertyValue().getSysstatHbkno());
					nbGeneralForReq.getRequest().setIDNO(getFeptxn().getFeptxnIdno());
					nbGeneralForReq.getRequest().setAcct(getFeptxn().getFeptxnTroutActno());
					nbGeneralForReq.getRequest().setSecurityCode(getFeptxn().getFeptxnMajorActno());
					nbGeneralForReq.getRequest().setAmount(getFeptxn().getFeptxnTxAmt());
					nbGeneralForReq.getRequest().setCurrency(getFeptxn().getFeptxnTxCur());
					// Fly 2018/02/12 應行動銀行修改 跨行無卡提款放入”跨行”
					if (!DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
						nbGeneralForReq.getRequest().setATMNO(getFeptxn().getFeptxnAtmno());
						nbGeneralForReq.getRequest().setMemo("");
					} else {
						nbGeneralForReq.getRequest().setATMNO("跨行");
						AllbankMapper dbAllBank = SpringBeanFactoryUtil.getBean(AllbankMapper.class);
						Allbank defAllBank = new Allbank();
						defAllBank.setAllbankBkno(getFeptxn().getFeptxnBkno());
						defAllBank.setAllbankBrno("000");
						defAllBank = dbAllBank.selectByPrimaryKey(defAllBank.getAllbankBkno(), defAllBank.getAllbankBrno());
						if (defAllBank != null) {
							nbGeneralForReq.getRequest().setMemo(defAllBank.getAllbankAliasname());
						} else {
							nbGeneralForReq.getRequest().setMemo("");
						}
					}
					nbGeneralForReq.getRequest().setFee(getFeptxn().getFeptxnFeeCustpay());
					StringBuffer txnDate = new StringBuffer(txndate);
					try {
						nbGeneralForReq.getRequest().setTxnDate(txnDate.insert(13, ":").insert(11, ":").insert(6, "/").insert(4, "/").toString().replace(" ", "T"));
					} catch (Exception ex) {
						nbGeneralForReq.getRequest().setTxnDate("");
					}
					nbGeneralForReq.getRequest().setTransactionID(getFeptxn().getFeptxnCbsRrn());

					CLARequest reqClass = new CLARequest();

					reqClass.setRqHeader(new FEPRqHeader());
					reqClass.setSvcRq(new CLASvcRq());
					reqClass.getSvcRq().setRq(new CLARq());

					nbAdapter.setMessageToNB(reqClass.makeMessageFromGeneral(nbGeneralForReq));
					break;
				}
				default: {
					getLogContext().setRemark("傳入的ACTION " + action + "不存在");
					logMessage(Level.DEBUG, getLogContext());
					return rtn;
				}
			}

			getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(true)); /// * CBS 逾時 FLAG */
			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {// RC = L013
				return IOReturnCode.FEPTXNUpdateError;
			}
			rtn = nbAdapter.sendReceive();

			if (rtn == FEPReturnCode.Normal) {
				getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(false)); /// * CBS 逾時 FLAG */
			}

			if (!StringUtils.isBlank(nbAdapter.getMessageFromNB())) {
				switch (action) {
					case "2": {// 推撥服務通知
						PSNResponse resClass = new PSNResponse();
						resClass = (PSNResponse) deserializeFromXml(nbAdapter.getMessageFromNB(), PSNResponse.class);

						if (resClass.getRsHeader() == null) {
							resClass.setRsHeader(new FEPRsHeader());
						}
						if (resClass.getSvcRs() == null) {
							resClass.setSvcRs(new PSNSvcRs());
						}
						if (resClass.getSvcRs().getRs() == null) {
							resClass.getSvcRs().setRs(new PSNRs());
						}

						resClass.toGeneral(nbGeneralForRes);
						NBGeneralResponse tempVar2 = nbGeneralForRes.getResponse();
						if ("0000".equals(tempVar2.getRsStatRsStateCode())) {
							getFeptxn().setFeptxnAscRc("0000");
						} else {
							getFeptxn().setFeptxnAscRc(tempVar2.getRsStatRsStateCode());
						}
						break;
					}
					case "3": {
						CLAResponse resClass = new CLAResponse();
						resClass = (CLAResponse) deserializeFromXml(nbAdapter.getMessageFromNB(), CLAResponse.class);

						if (resClass.getRsHeader() == null) {
							resClass.setRsHeader(new FEPRsHeader());
						}
						if (resClass.getSvcRs() == null) {
							resClass.setSvcRs(new CLASvcRs());
						}
						if (resClass.getSvcRs().getRs() == null) {
							resClass.getSvcRs().setRs(new CLARs());
						}

						resClass.toGeneral(nbGeneralForRes);
						NBGeneralResponse tempVar3 = nbGeneralForRes.getResponse();
						if ("0000".equals(tempVar3.getRsStatRsStateCode())) {
							getFeptxn().setFeptxnAscRc("0000");
						} else {
							getFeptxn().setFeptxnAscRc(tempVar3.getRsStatRsStateCode());
						}
						break;
					}
				}
			}

			this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		}
		return FEPReturnCode.Normal;

	}

}
