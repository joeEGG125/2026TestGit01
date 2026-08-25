package com.syscom.fep.server.aa.ims;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.fisc.FISCHeader;

public class CBSConfirmI extends INBKAABase{
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String rtnMessage = StringUtils.EMPTY;
	private String fiscTeleType = StringUtils.EMPTY;
	private FISCHeader fiscHeader;
	private boolean isRequestFEPTXN = false;
	private Short ConmacType = null;
	
	public CBSConfirmI(FISCData txnData) throws Exception {
		super(txnData);
		fiscTeleType = getTxData().getFiscTeleType().toString();
		switch(fiscTeleType) {
		case "EMVIC":
			fiscHeader = getFiscEMVICCon();
			break;
		case "OPC":
			fiscHeader = getFiscOPCCon();
			break;
		case "INBK":
			fiscHeader = getFiscCon();
			break;
		}
		ConmacType = this.getTxData().getMsgCtl().getMsgctlConmacType();
	}

	@Override
	public String processRequestData(){

		try {
			//記錄LOG
			this.logContext.setProgramFlowType(ProgramFlow.AAIn);
			this.logContext.setMessageFlowType(MessageFlow.Confirmation);
			this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage(this.getTxData().getTxRequestMessage());
			this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
			logMessage(this.logContext);
			
			rtnCode = this.checkConfirmFEPTXN();
			
			if(isRequestFEPTXN) {
				getFiscBusiness().getFeptxn().setFeptxnConRc(this.fiscHeader.getResponseCode());
				getFiscBusiness().getFeptxn().setFeptxnCbsRc(this.fiscHeader.getResponseCode());
				/* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
				getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			}
			// 3.產生財金 MAC
			rtnCode = this.makeFiscMac();
			

			// 4.SendToFISC送電文到財金
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = getFiscBusiness().sendToFISCFromCBS();
			}

			// 5.UpdateTxData:更新交易記錄 (FEPTXN)
			if (rtnCode == FEPReturnCode.Normal) {
				if(isRequestFEPTXN) {
					rtnCode = this.updateTxData();
					if (rtnCode != FEPReturnCode.Normal) {
						return rtnMessage;
					}
				}
			}

			// 6.label_END_OF_FUNC:
			if(isRequestFEPTXN) {
				rtnCode = this.updateFEPTXN();
			}

		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		} finally {
			this.logContext.setProgramFlowType(ProgramFlow.AAOut);
			this.logContext.setMessage(rtnMessage);
			this.logContext.setProgramName(this.aaName);
			this.logContext.setMessageFlowType(MessageFlow.ResponseConfirmation);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode, this.logContext));
			logMessage(Level.DEBUG, this.logContext);
		}
		
		return rtnMessage;
	}
	
	private FEPReturnCode checkConfirmFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		
		Calendar txDate = CalendarUtil.rocStringToADDate("0" + fiscHeader.getTxnInitiateDateAndTime().substring(0, 6)); // 民國轉成西元年
		if (txDate == null) {
			this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".checkConfirmFEPTXN"));
			this.logContext.setMessage(this.getTxData().getTxRequestMessage());
			this.logContext.setRemark("not find request Feptxn.");
			sendEMS(this.logContext);
		}else {
			String date = FormatUtil.dateTimeFormat(txDate, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
			String bkno = fiscHeader.getTxnSourceInstituteId().substring(0, 3);
			String stan = fiscHeader.getSystemTraceAuditNo();
			feptxn = feptxnDao.getFEPTXNByReqDateAndStan(date, bkno, stan);
			if(feptxn != null) {
				getFiscBusiness().setFeptxn(feptxn);
				isRequestFEPTXN = true;
				this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkPrepareFEPTXN"));
				this.logContext.setRemark("The Feptxn Exist, date = " + date + ", bkno = " + bkno + ", stan = " + stan);
				logMessage(this.logContext);
			}else {
				this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".checkConfirmFEPTXN"));
				this.logContext.setMessage(this.getTxData().getTxRequestMessage());
				this.logContext.setRemark("not find request Feptxn.");
				sendEMS(this.logContext);
			}
		}
		
		return rtnCode;
	}
	
	//3.產生財金 MAC
	private FEPReturnCode makeFiscMac() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String message = this.getTxData().getTxRequestMessage();
		String mac = "";
		RefString refMac = new RefString(mac);
		switch(fiscTeleType) {
		case "EMVIC":
			mac = getFiscEMVICCon().getMAC();
			refMac = new RefString(mac);
			if(ConmacType != null)
			rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
			break;
		case "OPC":
			mac = getFiscOPCCon().getMAC();
			refMac = new RefString(mac);
			if(ConmacType != null)
			rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
			break;
		case "INBK":
			mac = getFiscCon().getMAC();
			refMac = new RefString(mac);
			if(ConmacType != null)
			rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
			break;
		}
		
		this.logContext.setRemark(fiscTeleType + " new fisc mac :" + refMac.get());
		logMessage(Level.DEBUG, this.logContext);
		
		if (rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        }else {
        	if(ConmacType != null) {
        		message = message.substring(0, (message.length() - 8)) + refMac.get();
            	this.getTxData().setTxRequestMessage(message);
        	}
        }
		return rtnCode;
	}

	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)0);
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
				if (NormalRC.FISC_ATM_OK.equals(fiscHeader.getResponseCode())) {  /*+CON*/
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); /*成功*/
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 0); 
					String balb = "";
					String bala = "";
					
					switch(fiscTeleType) {
					case "EMVIC":
						balb = getFiscEMVICCon().getBALB();
						bala = getFiscEMVICCon().getBALA();
						break;
					case "OPC":
						break;
					case "INBK":
						balb = getFiscCon().getBALB();
						bala = getFiscCon().getBALA();
						break;
					}
					if(StringUtils.isNotBlank(balb)) {
						getFiscBusiness().getFeptxn().setFeptxnBalb(new BigDecimal(balb));
					}
					if(StringUtils.isNotBlank(bala)) {
						getFiscBusiness().getFeptxn().setFeptxnBala(new BigDecimal(bala));
					}
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);  /*Accept-Reverse*/
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
				}
				
			}
			
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
			getFiscBusiness().getFeptxn().setFeptxnConRc(fiscHeader.getResponseCode());
			
			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

		} catch (Exception ex) {
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(logContext);
			return CommonReturnCode.ProgramException;
		}
		
		return rtnCode;
	}
	
	private FEPReturnCode updateFEPTXN() throws Exception {
		if(getFiscBusiness().getFeptxn().getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
			if (this.rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());
			}
		}
		
		getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1); /*AA Close*/
		if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) > 0) {
			return FEPReturnCode.Normal;
		}
		
		return FEPReturnCode.UpdateFail;
	}
}
