package com.syscom.fep.web.controller.inbk;

import static com.syscom.fep.vo.constant.NormalRC.FISC_OK;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_015202_Form;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.model.Clrdtl;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_CLR;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 查詢財金各項跨行借貸交易-5202
 *
 * @author  Kai
 */
@Controller
public class UI_015202Controller extends BaseController {
	private Sysstat dtSYSSTAT = new Sysstat();
	@SuppressWarnings("unused")
	private String sysstatTbsdyFisc;
	private String sysstatHbkno;

	@Autowired
	public AtmService atmService;
	@Autowired
	public InbkService inbkService;


	@Override
	public void pageOnLoad(ModelMap mode) {
		try {
			UI_015202_Form form = new UI_015202_Form();
			sysstatTbsdyFisc = SysStatus.getPropertyValue().getSysstatTbsdyFisc();
			sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
			form.setClearDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
			form.setLblBankNo(sysstatHbkno);
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception e) {
			this.showMessage(mode, MessageType.DANGER,"查詢Sysstat出現異常");
		}
	}

	@PostMapping(value = "/inbk/UI_015202/queryLoanTransaction", produces = "application/json;charset=utf-8")
	public String queryLoanTransaction(@ModelAttribute UI_015202_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		this.queryClick(form,mode);
		return Router.UI_015202.getView();
	}

	public void queryClick(UI_015202_Form form, ModelMap mode) {
		String queryOnly = WebConfiguration.getInstance().getQueryOnly();
		FISCGeneral aData = new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
		String[] message = null;
		try {
			if (Long.parseLong(form.getClearDate().replace("-","")) > Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date()))) {
				this.showMessage(mode,MessageType.DANGER,"輸入日期錯誤");
				return;
			}

			if (Long.parseLong(form.getClearDate().replace("-","")) < Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date())) || "1".equals(queryOnly)) {
				// 若輸入清算日期小於系統日，則至資料表 CLRDTL 抓取資料
				Clrdtl defCLRDTL = new Clrdtl();

				defCLRDTL.setClrdtlApId(form.getApId5().trim());
				defCLRDTL.setClrdtlTxdate(form.getClearDate().replace("-",""));
				if(StringUtils.isNotBlank(form.getPayType())){
					defCLRDTL.setClrdtlPaytype(form.getPayType());
				}
				defCLRDTL = inbkService.getClrdtlByPrimaryKey(defCLRDTL);
				if (defCLRDTL != null) {
					form.setLblTxTime(charTimeToTime(defCLRDTL.getClrdtlTime()));
					form.setLblBknoStan(sysstatHbkno + "-" + defCLRDTL.getClrdtlStan());
					WebUtil.putInAttribute(mode, AttributeName.DetailEntity, defCLRDTL);
					this.showMessage(mode,MessageType.SUCCESS,DealSuccess);
				} else {
					this.showMessage(mode,MessageType.INFO,QueryNoData);
				}
				return;
			}

			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getLblBankNo())) {
				auditLog.addParam("銀行代號", form.getLblBankNo());
			}
			if (StringUtils.isNotBlank(form.getClearDate())) {
				auditLog.addParam("清算日期", form.getClearDate().replace("-",""));
			}
			if (StringUtils.isNotBlank(form.getLblTxTime())) {
				auditLog.addParam("查詢時間", form.getLblTxTime());
			}
			if (StringUtils.isNotBlank(form.getLblBknoStan())) {
				auditLog.addParam("查詢序號", form.getLblBknoStan());
			}
			if (StringUtils.isNotBlank(form.getApId5())) {
				auditLog.addParam("跨行業務代號", form.getApId5());
			}
			if (StringUtils.isNotBlank(form.getPayType())) {
				auditLog.addParam("代繳代發轉帳類別/消費扣款共用平台清算業務代號", form.getPayType());
			}
			setAuditLog(auditLog);

			aData.setCLRRequest(new FISC_CLR());
			aData.setSubSystem(FISCSubSystem.CLR);
			aData.getCLRRequest().setMessageKind(MessageFlow.Request);
			aData.getCLRRequest().setProcessingCode("5202");
			aData.getCLRRequest().setMessageType("0500");
			aData.getCLRRequest().setAPID5(form.getApId5());
			if ("13011".equals(aData.getCLRRequest().getAPID5())) {
				if(StringUtils.isBlank(form.getPayType())){
					aData.getCLRRequest().setPAYTYPE(StringUtils.SPACE);
				}else {
					aData.getCLRRequest().setPAYTYPE(form.getPayType());
				}
			}

			// Call AA
			fepHandler.dispatch(FEPChannel.FEP, aData);

			// 將AA RC 顯示在UI上
			if (aData.getDescription() == null || StringUtils.isBlank(aData.getDescription())) {
				aData.setDescription(MessageError);
			}
			message = aData.getDescription().split("[-]", -1);
			if (message.length == 2 && FISC_OK.equals(message[0])) {// 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
				form.setLblTxTime(charTimeToTime(aData.getCLRRequest().getTxnInitiateDateAndTime().substring(6, 12)));
				form.setLblBknoStan(sysstatHbkno + "-" + aData.getCLRRequest().getSystemTraceAuditNo());
				// 2015/07/14 Modify by Ruling for 清算電文之跨行預留基金的單位為新台幣元，需乘以100
				Clrdtl clrdtl = new Clrdtl();
				clrdtl.setClrdtlPreFund(new BigDecimal(aData.getCLRResponse().getFundBal().equals("") ? "0" : String.valueOf(Double.parseDouble(aData.getCLRResponse().getFundBal()) * 100)));
				clrdtl.setClrdtlUseBal(new BigDecimal(aData.getCLRResponse().getFundAvail().equals("") ? "0" : aData.getCLRResponse().getFundAvail()));
				clrdtl.setClrdtlTotDbcnt(Integer.parseInt(aData.getCLRResponse().getSumCntDr().equals("") ? "0" : aData.getCLRResponse().getSumCntDr()));
				clrdtl.setClrdtlTotDbamt(new BigDecimal(aData.getCLRResponse().getSumAmtDr().equals("") ? "0" : aData.getCLRResponse().getSumAmtDr()));
				clrdtl.setClrdtlFeeDbamt(new BigDecimal(aData.getCLRResponse().getFeeAmtDr().equals("") ? "0" : aData.getCLRResponse().getFeeAmtDr()));
				clrdtl.setClrdtlTotCrcnt(Integer.parseInt(aData.getCLRResponse().getSumCntCr().equals("") ? "0" : aData.getCLRResponse().getSumCntCr()));
				clrdtl.setClrdtlTotCramt(new BigDecimal(aData.getCLRResponse().getSumAmtCr().equals("") ? "0" : aData.getCLRResponse().getSumAmtCr()));
				clrdtl.setClrdtlFeeCramt(new BigDecimal(aData.getCLRResponse().getFeeAmtCr().equals("") ? "0" : aData.getCLRResponse().getFeeAmtCr()));
				WebUtil.putInAttribute(mode, AttributeName.DetailEntity, clrdtl);
				// add by Maxine on 2011/08/17 for 放開清算日期查詢
				if (insertCLRDTL(aData,form,mode)) {
					this.showMessage(mode,MessageType.INFO,aData.getDescription());
				}

			} else {
				this.showMessage(mode,MessageType.DANGER,aData.getDescription());
			}

		} catch (Exception ex) {
			this.showMessage(mode,MessageType.DANGER,programError);
		}

	}

	public void querySYSSTAT(ModelMap mode) {
		try {
			dtSYSSTAT = atmService.getStatus();
			if (dtSYSSTAT == null) {
				this.showMessage(mode,MessageType.DANGER,"SYSSTAT無資料!!");
				return;
			}

			sysstatTbsdyFisc = dtSYSSTAT.getSysstatTbsdyFisc();

		} catch (Exception ex) {
			this.showMessage(mode,MessageType.DANGER,ex.getMessage());
		}
	}

	public boolean insertCLRDTL(FISCGeneral aData, UI_015202_Form form, ModelMap mode) {
		Clrdtl defCLRDTL = new Clrdtl();
		try {
			defCLRDTL.setClrdtlApId(form.getApId5().trim());
			defCLRDTL.setClrdtlTxdate(form.getClearDate().replace("-",""));
			if(StringUtils.isNotBlank(form.getPayType())){
				defCLRDTL.setClrdtlPaytype(form.getPayType().trim());
			}
			defCLRDTL.setClrdtlTime(aData.getCLRRequest().getTxnInitiateDateAndTime().substring(6, 12));
			defCLRDTL.setClrdtlStan(aData.getCLRRequest().getSystemTraceAuditNo());
			// 2015/07/14 Modify by Ruling for 清算電文之跨行預留基金的單位為新台幣元，需乘以100
			if (StringUtils.isBlank(aData.getCLRResponse().getFundBal())) {
				defCLRDTL.setClrdtlPreFund(BigDecimal.valueOf(0));
			} else {
				defCLRDTL.setClrdtlPreFund(BigDecimal.valueOf(Double.parseDouble(aData.getCLRResponse().getFundBal()) * 100));
			}
			// defCLRDTL.CLRDTL_PRE_FUND = Convert.ToDecimal(IIf(aData.CLRResponse.FUND_BAL = "", "0", aData.CLRResponse.FUND_BAL))
			defCLRDTL.setClrdtlUseBal(new BigDecimal(aData.getCLRResponse().getFundAvail().equals("") ? "0" : aData.getCLRResponse().getFundAvail()));
			defCLRDTL.setClrdtlTotDbcnt(Integer.parseInt((aData.getCLRResponse().getSumCntDr().equals("")) ? "0" : aData.getCLRResponse().getSumCntDr()));
			defCLRDTL.setClrdtlTotDbamt(new BigDecimal((aData.getCLRResponse().getSumAmtDr().equals("")) ? "0" : aData.getCLRResponse().getSumAmtDr()));
			defCLRDTL.setClrdtlFeeDbamt(new BigDecimal((aData.getCLRResponse().getFeeAmtDr().equals("")) ? "0" : aData.getCLRResponse().getFeeAmtDr()));
			defCLRDTL.setClrdtlTotCrcnt(Integer.parseInt((aData.getCLRResponse().getSumCntCr().equals("")) ? "0" : aData.getCLRResponse().getSumCntCr()));
			defCLRDTL.setClrdtlTotCramt(new BigDecimal((aData.getCLRResponse().getSumAmtCr().equals("")) ? "0" : aData.getCLRResponse().getSumAmtCr()));
			defCLRDTL.setClrdtlFeeCramt(new BigDecimal((aData.getCLRResponse().getFeeAmtCr().equals("")) ? "0" : aData.getCLRResponse().getFeeAmtCr()));
			defCLRDTL.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
			defCLRDTL.setUpdateTime(new Date());
			if (inbkService.updateClrdtlByPK(defCLRDTL) < 1) {
				if (inbkService.insertClrdtl(defCLRDTL) < 1) {
					this.showMessage(mode,MessageType.DANGER,InsertFail , "(CLRDTL)");
					return false;
				}
			}
			return true;
		} catch (Exception ex) {
			this.showMessage(mode,MessageType.DANGER,InsertFail , "(CLRDTL) Exception " , ex.getMessage());
		}
		return false;

	}

}