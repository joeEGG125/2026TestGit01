package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_012130_Form;
import com.syscom.fep.web.form.inbk.UI_012130_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;

/**
 * 傳送未完成交易處理結果-2130
 *
 * @author ChenYu
 */

@Controller
public class UI_012130Controller extends BaseController {
	@Autowired
	public InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_012130_Form form = new UI_012130_Form();
		// 交易日期
		form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		// 財金STAN
		// String bankId = StringUtils.EMPTY;
		try {
			form.setBkno(SysStatus.getPropertyValue().getSysstatFbkno());
			// bankId = StringUtils.join(SysStatus.getPropertyValue().getSysstatFbkno(), "0000");
		} catch (Exception ex) {
			this.showMessageWithArgs(mode, MessageType.DANGER, DATA_INQUIRY_EXCEPTION_OCCUR_WITH_MESSAGE, "財金STAN", "");
			this.errorMessage(ex, ex.getMessage());
		}
		form.setQueryok("");
		mode.put("select", "00");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_012130/inquiryMain")
	public String getpendingDateStanBkno(@ModelAttribute UI_012130_Form form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		Inbkpend aInbkpend = new Inbkpend();
		Inbkpend aInbkpend2130 = new Inbkpend();

		try {
			form.setQueryok("");
			// 交易日期
			String tradingDate = form.getTradingDate().replace("-", "");
			// 財金STAN
			String bkno = form.getBkno();
			String stan = form.getStan();

			aInbkpend.setInbkpendTxDate(tradingDate);
			aInbkpend.setInbkpendPcode("2120");
			aInbkpend.setInbkpendBkno(bkno.trim());
			aInbkpend.setInbkpendStan(StringUtils.leftPad(stan.trim(), 7, "0"));
			aInbkpend.setInbkpendRepRc("0001");

			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(form.getTradingDate()))
				auditLog.addParam("交易日期", form.getTradingDate());
			if (StringUtils.isNotBlank(form.getBkno()))
				auditLog.addParam("Bkno", form.getBkno());
			if (StringUtils.isNotBlank(form.getStan()))
				auditLog.addParam("財金STAN", form.getStan());
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			// Inbkpend bInbkpend = inbkpendExtMapper.getpendingDateStanBkno(aInbkpend);
			aInbkpend = inbkService.getInbkpendByBknoStan(aInbkpend);

			if (aInbkpend == null) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				// '檢核是否已傳送交易處理結果給財金 (PTYPE=2130)
				// '讀取 INBKPEND 2130
				aInbkpend2130.setInbkpendTxDate(aInbkpend.getInbkpendTxDate());
				aInbkpend2130.setInbkpendPcode("2130");
				aInbkpend2130.setInbkpendOriBkno(aInbkpend.getInbkpendOriBkno());
				aInbkpend2130.setInbkpendOriStan(aInbkpend.getInbkpendOriStan());
				aInbkpend2130.setInbkpendRepRc("0001");
				if (StringUtils.isBlank(aInbkpend2130.getInbkpendStan())) {
					aInbkpend2130.setInbkpendStan("");
				}
				if (StringUtils.isBlank(aInbkpend2130.getInbkpendRepRc())) {
					aInbkpend2130.setInbkpendRepRc("");
				}
				// Inbkpend Inbkpend2130 = inbkpendExtMapper.getpendingDateStanBkno(aInbkpend2130);
				aInbkpend2130 = inbkService.getInbkpendByBknoStan(aInbkpend2130);

				if (aInbkpend2130 != null) {
					if (FEPTxnMessageFlow.FISC_Response.equals(aInbkpend2130.getInbkpendMajorActno()) && FEPReturnCode.Normal.toString().equals(aInbkpend2130.getInbkpendAaRc())) {
						this.showMessage(mode, MessageType.INFO, CanNotBeResent);
					}
				}

				if (aInbkpend2130 != null) {
					if (FEPTxnMessageFlow.FISC_Response.equals(aInbkpend2130.getInbkpendMajorActno()) && FEPReturnCode.Normal.toString().equals(aInbkpend2130.getInbkpendAaRc())) {
						this.showMessage(mode, MessageType.DANGER, CanNotBeResent);
					}
				}else {
					//2022/08/18 Bruce add
					this.showMessage(mode, MessageType.SUCCESS, QuerySuccess);
				}
				form.setQueryok("OK");
				mode.put("select", aInbkpend.getInbkpendPrcResult());
				mode.put("querok", form.getQueryok());
			}
			// 顯示畫面欄位如下:

			WebUtil.putInAttribute(mode, AttributeName.DetailEntity, aInbkpend);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			//2022/08/18 Bruce Modify 在畫面上顯示程式出現錯誤！請洽資訊人員
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		// List<Inbkpend> UI_012130_Form = inbkpendExtMapper.getpendingDateStanBkno(aInbkpend);
		return Router.UI_012130.getView();
	}

	@PostMapping(value = "/inbk/UI_012130/inquiryDetail")
	@ResponseBody
	public BaseResp<?> execute(@RequestBody UI_012130_FormDetail form, ModelMap mode) {
		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		mode.put("select", form.getPrcresultddl());
		BaseResp<?> response = new BaseResp<>();

		try {
			if (!"OK".equals(form.getQueryok())) {
				response.setMessage(MessageType.DANGER, PleaseDoQuery);
				return response;
			}
			FISCGeneral aData = new FISCGeneral();
			aData.setINBKRequest(new FISC_INBK());
			aData.setSubSystem(FISCSubSystem.INBK);
			aData.getINBKRequest().setProcessingCode("2130");
			aData.getINBKRequest().setMessageType("0200");
			aData.getINBKRequest().setMessageKind(MessageFlow.Request);
			aData.getINBKRequest().setTxAmt(form.getTxatmtxt());
			aData.getINBKRequest().setATMNO(form.getAtmnotxt());
			aData.getINBKRequest().setRsCode(form.getPrcresultddl());
			aData.getINBKRequest().setDueDate(CalendarUtil.adStringToROCString(form.getOritxdatetxt().replace("/", "")));
			aData.getINBKRequest().setOriStan(form.getOribknotxt() + StringUtils.leftPad(form.getOristantxt(), 7, "0"));
			aData.getINBKRequest().setICMARK(StringUtils.leftPad(form.getCardnotxt(), 16, ""));
			aData.getINBKRequest().setTroutBkno(form.getTroutbknotxt());
			aData.getINBKRequest().setTroutActno(form.getTroutactnotxt());
			aData.getINBKRequest().setTrinBkno(form.getTrinbknotxt());
			aData.getINBKRequest().setTrinActno(form.getTrinactnotxr());
			aData.getINBKRequest().setREMARK(StringUtils.leftPad(form.getTrinactnoactualtxt(), 16, "") + StringUtils.leftPad(form.getPcodetxt(), 4, "0"));

			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("確認");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(form.getPrcresultddl()))
				auditLog.addParam("處理結果", form.getPrcresultddl());
			if (StringUtils.isNotBlank(form.getOritxdatetxt()))
				auditLog.addParam("原交易日期", form.getOritxdatetxt());
			if (StringUtils.isNotBlank(form.getOritbsdyfisctxt()))
				auditLog.addParam("原入帳日期", form.getOritbsdyfisctxt());

			if (StringUtils.isNotBlank(form.getPcodetxt()))
				auditLog.addParam("原交易種類", form.getPcodetxt());
			if (StringUtils.isNotBlank(form.getTxatmtxt()))
				auditLog.addParam("原交易金額", form.getTxatmtxt());

			if (StringUtils.isNotBlank(form.getTroutbknotxt()))
				auditLog.addParam("原扣款帳號bkno", form.getTroutbknotxt());
			if (StringUtils.isNotBlank(form.getTroutactnotxt()))
				auditLog.addParam("原扣款帳號actno", form.getTroutactnotxt());

			if (StringUtils.isNotBlank(form.getTrinbknotxt()))
				auditLog.addParam("原轉入帳號bkno", form.getTrinbknotxt());
			if (StringUtils.isNotBlank(form.getTrinactnotxr()))
				auditLog.addParam("原轉入帳號actno", form.getTrinactnotxr());

			if (StringUtils.isNotBlank(form.getCardnotxt()))
				auditLog.addParam("原卡片帳號", form.getCardnotxt());
			if (StringUtils.isNotBlank(form.getTrinactnoactualtxt()))
				auditLog.addParam("原入帳帳號", form.getTrinactnoactualtxt());

			if (StringUtils.isNotBlank(form.getAtmnotxt()))
				auditLog.addParam("原ATM代號", form.getAtmnotxt());
			if (StringUtils.isNotBlank(form.getOribknotxt()))
				auditLog.addParam("財金STAN:bkno", form.getOribknotxt());
			if (StringUtils.isNotBlank(form.getOristantxt()))
				auditLog.addParam("財金STAN:stan", form.getOristantxt());

			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			// Call AA
			this.infoMessage("Start to Call AA via FEPHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
			FEPHandler fepHandler = new FEPHandler();
			fepHandler.dispatch(FEPChannel.FEP, aData);
			// 將AA RC 顯示在UI上
			if (StringUtils.isBlank(aData.getDescription())) {
				aData.setDescription(MessageError);
			}
			String[] message = aData.getDescription().split("-");
			// 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
			if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
				response.setMessage(MessageType.SUCCESS, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
			} else {
				response.setMessage(MessageType.DANGER, aData.getDescription());
			}
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			response.setMessage(MessageType.DANGER, programError);
		}
		return response;
	}
}
