package com.syscom.fep.web.controller.inbk;

import java.util.Calendar;

import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_012140_Form;
import com.syscom.fep.web.form.inbk.UI_012140_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 人工沖正請求交易-2140
 *
 * @author ChenYu
 */

@Controller
public class UI_012140Controller extends BaseController {

	@Autowired
	private InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_012140_Form form = new UI_012140_Form();
		// 原交易日期
		form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		try {
			form.setOribknotxt(SysStatus.getPropertyValue().getSysstatHbkno());
			// bankId = StringUtils.join(SysStatus.getPropertyValue().getSysstatFbkno(), "0000");
		} catch (Exception ex) {
			this.showMessageWithArgs(mode, MessageType.DANGER, DATA_INQUIRY_EXCEPTION_OCCUR_WITH_MESSAGE, "財金STAN", "");
			this.errorMessage(ex, ex.getMessage());
		}
		form.setQueryok("");
		mode.addAttribute("disabled", false);
		mode.addAttribute("disableddl", false);
		mode.put("select", "000");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_012140/inquiryMain")
	public String getpendingTradingdate(@ModelAttribute UI_012140_Form form, ModelMap mode) throws Exception {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		FEPReturnCode rtncode;
		FISC ofiscbusiness = new FISC();
		String oribknotxt;
		String tradingDate;
		String oristantxt;
		FeptxnExt ofeptxn = new FeptxnExt();
		FEPCache.reloadCache(CacheItem.SYSSTAT);
		try {
			form.setQueryok("");
			ofiscbusiness.setFeptxn(ofeptxn);
			tradingDate = form.getTradingDate().replace("-", "");
			oristantxt = StringUtils.leftPad(form.getOristantxt().trim(), 7, "0");
			oribknotxt = form.getOribknotxt().trim();
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(form.getTradingDate()))
				auditLog.addParam("交易日期", form.getTradingDate());
			if (StringUtils.isNotBlank(form.getPcode()))
				auditLog.addParam("getPcode", form.getPcode());
			if (StringUtils.isNotBlank(form.getOribknotxt()))
				auditLog.addParam("原查詢序號bkno", form.getOribknotxt());
			if (StringUtils.isNotBlank(form.getOristantxt()))
				auditLog.addParam("原查詢序號stan", form.getOristantxt());
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			if ("2140".equals(form.getPcode())) {
				rtncode = ofiscbusiness.searchFeptxn(tradingDate, oribknotxt, oristantxt);
				if (rtncode == FEPReturnCode.ProgramException) {
					this.showMessage(mode, MessageType.DANGER, DATA_INQUIRY_EXCEPTION_OCCUR);
				} else if (rtncode != FEPReturnCode.Normal) {
					this.showMessage(mode, MessageType.INFO, PleaseInsertOther);
				} else {
					// 2012/05/08 Modify by Ruling for 查到原交易FEPTXN時，仍需判斷UI原交易日期與FEPTXN是否相符
					if (!tradingDate.equals(ofiscbusiness.getFeptxn().getFeptxnTxDate())) {
						this.showMessage(mode, MessageType.INFO, PleaseInsertOther);
					} else if(!StringUtils.equals(ofiscbusiness.getFeptxn().getFeptxnCbsProc(), "N")) {
						//只查詢3-2的判斷
						this.showMessage(mode, MessageType.INFO, PleaseInsertOther);
		                return Router.UI_012140.getView();
					} else {
						
						if ("2140".equals(form.getPcode()) && ("2571".equals(ofiscbusiness.getFeptxn().getFeptxnPcode()) || "2545".equals(ofiscbusiness.getFeptxn().getFeptxnPcode()))) {
							this.showMessage(mode, MessageType.DANGER, "PCODE錯誤, 請重新輸入");
						}
						form.setTxatmtxt(ofiscbusiness.getFeptxn().getFeptxnTxAmt().toString());
						form.setTroutbknotxt(ofiscbusiness.getFeptxn().getFeptxnTroutBkno());
						form.setTroutactnotxt(ofiscbusiness.getFeptxn().getFeptxnTroutActno());
						form.setQueryok("OK");
						// 'Fly 2015/09/04 EMV拒絕磁條卡交易
						// '2016/07/29 Modify By Nick for VISA卡跨國交易收處理費
						if ((("CWV".equals(ofiscbusiness.getFeptxn().getFeptxnTxCode()) && "2410".equals(ofiscbusiness.getFeptxn().getFeptxnPcode())) ||
								("CAV".equals(ofiscbusiness.getFeptxn().getFeptxnTxCode()) && "2420".equals(ofiscbusiness.getFeptxn().getFeptxnPcode())) ||
								("EWV".equals(ofiscbusiness.getFeptxn().getFeptxnTxCode()) && "2620".equals(ofiscbusiness.getFeptxn().getFeptxnPcode())) ||
								("EAV".equals(ofiscbusiness.getFeptxn().getFeptxnTxCode()) && "2622".equals(ofiscbusiness.getFeptxn().getFeptxnPcode()))) &&
								"Y".equals(ofiscbusiness.getFeptxn().getFeptxnRsCode().trim())) {
							form.setTxatmtxt(ofiscbusiness.getFeptxn().getFeptxnTxAmt() + ofiscbusiness.getFeptxn().getFeptxnFeeCustpay().toString());
						}
						// 2015/09/10 Modify by Ruling for 國際卡交易，轉出行要抓取FEPTXN_DES_BKNO
						if ("000".equals(ofiscbusiness.getFeptxn().getFeptxnTroutBkno())) {
							form.setTroutbknotxt(ofiscbusiness.getFeptxn().getFeptxnDesBkno());
						}
						form.setOridatetimetxt(ofiscbusiness.getFeptxn().getFeptxnTxDatetimeFisc());
						form.setMerchantid(ofiscbusiness.getFeptxn().getFeptxnMerchantId());
						form.setAtmno(ofiscbusiness.getFeptxn().getFeptxnAtmno());
					}
				}
			} else if ("2573".equals(form.getPcode()) || "2549".equals(form.getPcode())) {
				Ictltxn tempIctl = new Ictltxn();
				tempIctl.setIctltxnTxDate(form.getTradingDate().replace("-", ""));
				tempIctl.setIctltxnBkno(form.getOribknotxt());
				tempIctl.setIctltxnStan(form.getOristantxt());
				tempIctl = inbkService.searchIctltxn(tempIctl);
				if (tempIctl == null) {
					this.showMessage(mode, MessageType.INFO, PleaseInsertOther);
				} else {
					if (("2573".equals(form.getPcode()) && !"2571".equals(tempIctl.getIctltxnPcode())) || ("2549".equals(form.getPcode()) && !"2545".equals(tempIctl.getIctltxnPcode()))) {
						this.showMessage(mode, MessageType.INFO, "PCODE錯誤, 請重新輸入");
						WebUtil.putInAttribute(mode, AttributeName.Form, form);
						return Router.UI_012140.getView();
					}
					if (!"A".equals(tempIctl.getIctltxnTxrust()) && !"B".equals(tempIctl.getIctltxnTxrust())) {
						this.showMessage(mode, MessageType.INFO, "原交易未成功, 不得沖正,  請重新輸入");
					} else {
						form.setQueryok("OK");
						form.setTxatmtxt(String.valueOf(tempIctl.getIctltxnSetAmt()));
						form.setTroutactnotxt(tempIctl.getIctltxnTroutActno());
						form.setTroutbknotxt(tempIctl.getIctltxnTroutBkno());
						form.setOridatetimetxt(tempIctl.getIctltxnTxDatetimeFisc());
						form.setMerchantid(tempIctl.getIctltxnMerchantId());
						form.setAtmno(tempIctl.getIctltxnAtmno());

						mode.addAttribute("disableddl", true);
						// EC_INSTRUCTIONLbl.Enabled = False
						// EC_INSTRUCTIONDdl.Enabled = False
					}
				}
			}
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_012140.getView();
	}

	@PostMapping(value = "/inbk/UI_012140/inquiryDetail")
	@ResponseBody
	public BaseResp<?> execute(@RequestBody UI_012140_FormDetail form, ModelMap mode) {
		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		mode.put("select", form.getEcinstructionddl());
		BaseResp<?> response = new BaseResp<>();
		FISCGeneral aData = new FISCGeneral();
		try {
			if (!"OK".equals(form.getQueryok())) {
				response.setMessage(MessageType.DANGER, PleaseDoQuery);
				return response;
			}
			form.setTroutactnotxt(StringUtils.leftPad(form.getTroutactnotxt(), 16, "0"));
			aData.setINBKRequest(new FISC_INBK());
			aData.setSubSystem(FISCSubSystem.INBK);
			aData.getINBKRequest().setMessageKind(MessageFlow.Request);
			aData.getINBKRequest().setProcessingCode(form.getPcode());
			aData.getINBKRequest().setMessageType("0200");
			aData.getINBKRequest().setTxAmt(form.getTxatmtxt());
			aData.getINBKRequest().setDueDate(CalendarUtil.adStringToROCString(String.valueOf(form.getTradingDate().replace("-", ""))));
			aData.getINBKRequest().setOriStan(form.getOribknotxt() + StringUtils.leftPad(form.getOristantxt(), 7, "0"));
			aData.getINBKRequest().setTroutBkno(form.getTroutbknotxt());
			aData.getINBKRequest().setTroutActno(form.getTroutactnotxt());
			aData.getINBKRequest().setREMARK(form.getEcinstructionddl()); // 暫存入REMARK傳給AA
			if ("2549".equals(form.getPcode())) {
				aData.getINBKRequest().setTxDatetimeFisc(form.getOridatetimetxt());
				aData.getINBKRequest().setMerchantId(form.getMerchantid());
				aData.getINBKRequest().setATMNO(form.getAtmno());
			}
			if ("2573".equals(form.getPcode())) {
				aData.getINBKRequest().setTxDatetimeFisc(form.getOridatetimetxt());
				aData.getINBKRequest().setATMNO(form.getAtmno());
			}
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("確認");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(form.getTxatmtxt()))
				auditLog.addParam("原交易金額", form.getTxatmtxt());
			if (StringUtils.isNotBlank(form.getTroutactnotxt()))
				auditLog.addParam("原交易帳號/卡號", form.getTroutactnotxt());
			if (StringUtils.isNotBlank(form.getTroutbknotxt()))
				auditLog.addParam("原交易存款單位代號", form.getTroutbknotxt());
			if (StringUtils.isNotBlank(form.getEcinstructionddl()))
				auditLog.addParam("沖正指示", form.getEcinstructionddl());
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
			//2022/08/25 Bruce modify
//			response.setMessage(MessageType.DANGER, ex.getMessage());
			response.setMessage(MessageType.DANGER, programError);
		}
		return response;
	}

}
