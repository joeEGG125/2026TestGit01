package com.syscom.fep.web.controller.inbk;


import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019070_Form;
import com.syscom.fep.web.form.inbk.UI_019070_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * FEP交易查詢 查詢電子支付交易結果
 * 
 * @author Han
 */
@Controller
public class UI_019070Controller extends BaseController {

	@Autowired
	private AtmService atmService;

	@Autowired
	private InbkService inbkService;
	
	String txTransactDate = "";
	String txTransactDateE = "";

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_019070_Form form = new UI_019070_Form();
		form.setDttxDATE(this.getTbsdy(mode));
		form.setDttxDATEe(this.getTbsdy(mode));
		form.setTxtTroutBkno("006");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	private String getTbsdy(ModelMap mode) {
		try {
			Sysstat sysstat = atmService.getStatus();
			if (sysstat != null) {
				String sysstatTbsdyFisc = sysstat.getSysstatTbsdyFisc();
				if (StringUtils.isNotBlank(sysstatTbsdyFisc)) {
					return charDateToDate(sysstatTbsdyFisc, "-");
				}
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, "交易日期", DATA_INQUIRY_EXCEPTION_OCCUR);
		}
		return StringUtils.EMPTY;
	}

	@PostMapping(value = "/inbk/UI_019070/queryComfirm")
	public String queryComfirm(@ModelAttribute UI_019070_Form form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		form.setUrl("/inbk/UI_019070/queryComfirm");
		this.doKeepFormData(mode, form);
		try {

			BindGridData(form, mode);

		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_019070.getView();
	}

	@PostMapping(value = "/inbk/UI_019070/inquiryDetail")
	public String inquiryDetail(@ModelAttribute UI_019070_FormMain form, ModelMap mode) {
		
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		
//		Obtltxn dt;
		try {
//			dt = BindFormViewData(mode,form);
			form.setObtltxn(BindFormViewData(mode,form));
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		}catch(Exception e) {
			getLogContext().setProgramException(e);
			sendEMS(getLogContext());
		}finally {
//			dt = null;
		}
		return Router.UI_019070_Detail.getView();
	}

	private Obtltxn BindFormViewData(ModelMap mode, UI_019070_FormMain form) {

		return inbkService.getOBTLTXNbyPK(form.getObtltxnTxDate(), Long.parseLong(form.getObtltxnEjfno()));
	}

	private void BindGridData(UI_019070_Form form, ModelMap mode) {

		try {
			GetResultData(form, mode);

		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	private void GetResultData(UI_019070_Form form, ModelMap mode) {
		
		PageInfo<Obtltxn> pageInfo = null;

		try {
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			if (StringUtils.isNotEmpty(form.getDttxDATE())) {
				txTransactDate = form.getDttxDATE().replaceAll("-", "/");
			}
			if (StringUtils.isNotEmpty(form.getDttxDATEe())) {
				txTransactDateE = form.getDttxDATEe().replaceAll("-", "/");
			}

			if (StringUtils.isNotBlank(form.getDttxDATE()) && StringUtils.isNotBlank(form.getDttxDATEe()))
				auditLog.addParam("交易日期", form.getDttxDATE() + "~" + form.getDttxDATEe());
			if (StringUtils.isNotBlank(form.getTxtTxAMT()))
				auditLog.addParam("交易金額", form.getTxtTxAMT());
			if (StringUtils.isNotBlank(form.getTxtOrderNO()))
				auditLog.addParam("訂單編號", form.getTxtOrderNO());
			if (StringUtils.isNotBlank(form.getTxtMerchantId()))
				auditLog.addParam("特店代號", form.getTxtMerchantId());
			if (StringUtils.isNotBlank(form.getTxtTroutActno()))
				auditLog.addParam("轉出帳號",form.getTxtTroutBkno() +"-"+ form.getTxtTroutActno());
			if (StringUtils.isNotBlank(form.getTxtBkno()) && StringUtils.isNotBlank(form.getTxtStan()))
				auditLog.addParam("財金STAN", form.getTxtBkno() +"-stan:"+ form.getTxtStan());
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			if (StringUtils.isNotEmpty(form.getDttxDATE())) {
				txTransactDate = form.getDttxDATE().replaceAll("-", "/");
			}
			if (StringUtils.isNotEmpty(form.getDttxDATEe())) {
				txTransactDateE = form.getDttxDATEe().replaceAll("-", "/");
			}

			pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
					form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
						@Override
						public void doSelect() {
								inbkService.getObtlTxn(form.getTxtTroutBkno(), form.getTxtTroutActno(), form.getTxtTxAMT(),
										form.getTxtOrderNO(), form.getTxtMerchantId(),txTransactDate.replaceAll("/", ""), txTransactDateE.replaceAll("/", ""), form.getTxtBkno(),
										form.getTxtStan());
						}
					});
			
			if (null == pageInfo || pageInfo.getList().size() == 0) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
			} else {
				this.showMessage(mode, MessageType.INFO, "查詢完成");
			}
			
			PageData<UI_019070_Form, Obtltxn> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, QueryFail);
		}finally {
//			pageInfo = null;
		}
	}

};