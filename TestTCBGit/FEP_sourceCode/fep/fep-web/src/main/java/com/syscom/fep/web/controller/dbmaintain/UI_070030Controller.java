package com.syscom.fep.web.controller.dbmaintain;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Inbkparm;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070030_Detail_Form;
import com.syscom.fep.web.form.dbmaintain.UI_070030_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 跨行系統參數維護
 *
 * @author Joseph
 * @create 2022/05/23
 */
@Controller
public class UI_070030Controller extends BaseController {

	@Autowired
	private InbkService inbkService;
	
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070030_Form form = new UI_070030_Form();
		form.setUrl("/dbmaintain/UI_070030/queryClick");
		this.queryClick(form, mode);
	}

	@PostMapping(value = "/dbmaintain/UI_070030/queryClick")
	private String queryClick(@ModelAttribute UI_070030_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		
		try {
			BindGridData(form, mode);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_070030.getView();
	}

	@PostMapping(value = "/dbmaintain/UI_070030/btnDelete")
	@ResponseBody
	public BaseResp<UI_070030_Form> btnDelete(@RequestBody List<UI_070030_Form> list) {
		this.infoMessage("執行刪除動作, 條件 = [", list.toString(), "]");
		BaseResp<UI_070030_Form> response = new BaseResp<>();
		try {
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("刪除");
			for (UI_070030_Form form : list) {
				Inbkparm inbkparm = new Inbkparm();
				inbkparm.setInbkparmApid(form.getInbkparmApid());
				inbkparm.setInbkparmPcode(form.getInbkparmPcode());
				inbkparm.setInbkparmAcqFlag(form.getInbkparmAcqFlag());
				inbkparm.setInbkparmEffectDate(form.getInbkparmEffectDate());
				inbkparm.setInbkparmCur(form.getInbkparmCur());
				inbkparm.setInbkparmRangeFrom(form.getInbkparmRangeFrom());

				// 塞入auditLog.params
				if (StringUtils.isNotBlank(String.valueOf(form.getInbkparmApid())))
					auditLog.addParam("財金APID", String.valueOf(form.getInbkparmApid()));
				if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_PCODE())))
					auditLog.addParam("財金PCODE", String.valueOf(form.getINBKPARM_PCODE()));
				if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_ACQ_FLAG())))
					auditLog.addParam("代理/被代理", String.valueOf(form.getINBKPARM_ACQ_FLAG()));
				if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_EFFECT_DATE())))
					auditLog.addParam("生效日期", String.valueOf(form.getINBKPARM_EFFECT_DATE()));
				if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_CUR())))
					auditLog.addParam("幣別", String.valueOf(form.getINBKPARM_CUR()));
				if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_RANGE_FROM())))
					auditLog.addParam("金額起始範圍", String.valueOf(form.getINBKPARM_RANGE_FROM()));
				inbkService.deleteINBKPARM(inbkparm);
			}
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			response.setMessage(MessageType.INFO, DeleteSuccess);
			//清空查詢條件
			UI_070030_Form f = WebUtil.getUser().getCurrentPageForm();
			UI_070030_Form nf = new UI_070030_Form();
			nf.setUrl(f.getUrl());
			BeanUtils.copyProperties(nf, f);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}
	@PostMapping(value = "/dbmaintain/UI_070030/showDetail")
	private String showDetail(@ModelAttribute UI_070030_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		form.setBtnType(nullToEmptyStr(form.getBtnType()));
		form.setINBKPARM_APID(nullToEmptyStr(form.getINBKPARM_APID()));
		form.setINBKPARM_PCODE(nullToEmptyStr(form.getINBKPARM_PCODE()));
		form.setINBKPARM_ACQ(form.getINBKPARM_ACQ_FLAG().toString());
		form.setINBKPARM_EFFECT_DATE(nullToEmptyStr(form.getINBKPARM_EFFECT_DATE()));
		form.setINBKPARM_EFFECT_DATE(form.getINBKPARM_EFFECT_DATE().replace("-", ""));//處理日期符號
		form.setINBKPARM_CUR(nullToEmptyStr(form.getINBKPARM_CUR()));
		form.setINBKPARM_RANGE_FROM(form.getINBKPARM_RANGE_FROM());
		form.setINBKPARM_RANGE_TO(form.getINBKPARM_RANGE_TO());
		form.setINBKPARM_FEE_TYPE(form.getINBKPARM_FEE_TYPE());
		form.setINBKPARM_FEE_MBR_DR(form.getINBKPARM_FEE_MBR_DR());
		form.setINBKPARM_FEE_MBR_CR(form.getINBKPARM_FEE_MBR_CR());
		form.setINBKPARM_FEE_ASS_DR(form.getINBKPARM_FEE_ASS_DR());
		form.setINBKPARM_FEE_ASS_CR(form.getINBKPARM_FEE_ASS_CR());
		form.setINBKPARM_FEE_CUSTPAY(form.getINBKPARM_FEE_CUSTPAY());
		form.setINBKPARM_PRNCRDB(nullToEmptyStr(form.getINBKPARM_PRNCRDB()));
		form.setINBKPARM_FEE_MIN(form.getINBKPARM_FEE_MIN());
		if (!"insert".equals(form.getBtnType())) {
			String inbkparmacq = form.getINBKPARM_ACQ_FLAG().toString();
			Inbkparm inbkparm = bindFormViewData(form.getINBKPARM_APID(),inbkparmacq,form.getINBKPARM_CUR(),form.getINBKPARM_EFFECT_DATE(),form.getINBKPARM_RANGE_FROM(),form.getINBKPARM_PCODE());
			form.setINBKPARM_APID(nullToEmptyStr(inbkparm.getInbkparmApid()));
			form.setINBKPARM_PCODE(nullToEmptyStr(inbkparm.getInbkparmPcode()));
			form.setINBKPARM_ACQ(nullToEmptyStr(inbkparm.getInbkparmAcqFlag()));
			inbkparm.setInbkparmEffectDate(inbkparm.getInbkparmEffectDate().substring(0,4) + "-" +inbkparm.getInbkparmEffectDate().substring(4,6)+ "-" +inbkparm.getInbkparmEffectDate().substring(6,8));
			form.setINBKPARM_EFFECT_DATE(nullToEmptyStr(inbkparm.getInbkparmEffectDate()));
			form.setINBKPARM_CUR(nullToEmptyStr(inbkparm.getInbkparmCur()));
			form.setINBKPARM_RANGE_FROM(inbkparm.getInbkparmRangeFrom());
			form.setINBKPARM_RANGE_TO(inbkparm.getInbkparmRangeTo());
			form.setINBKPARM_FEE_TYPE(inbkparm.getInbkparmFeeType());
			form.setINBKPARM_FEE_MBR_DR(inbkparm.getInbkparmFeeMbrDr());
			form.setINBKPARM_FEE_MBR_CR(inbkparm.getInbkparmFeeMbrCr());
			form.setINBKPARM_FEE_ASS_DR(inbkparm.getInbkparmFeeAssDr());
			form.setINBKPARM_FEE_ASS_CR(inbkparm.getInbkparmFeeAssDr());
			form.setINBKPARM_FEE_CUSTPAY(inbkparm.getInbkparmFeeCustpay());
			form.setINBKPARM_PRNCRDB(nullToEmptyStr(inbkparm.getInbkparmPrncrdb()));
			form.setINBKPARM_FEE_MIN(inbkparm.getInbkparmFeeMin());
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_070030_Detail.getView();
	}

	private Inbkparm bindFormViewData(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
			BigDecimal INBKPARM_RANGE_FROM, String pcode) {
		return inbkService.getInbkparmByPK(APID,a,INBKPARM_CUR,INBKPARM_EFFECT_DATE,INBKPARM_RANGE_FROM,pcode);
	}
	@PostMapping(value = "/dbmaintain/UI_070030/saveClick")
	private String saveClick(@ModelAttribute UI_070030_Detail_Form dform, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("參數檔案維護, 條件= [", dform.toString(), "]");
		Inbkparm inbkparm = new Inbkparm();
		Integer iRes;
		if (checkAllField(inbkparm, dform, redirectAttributes)) {
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_APID())))
				auditLog.addParam("財金APID", String.valueOf(dform.getINBKPARM_APID()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_PCODE())))
				auditLog.addParam("財金PCODE", String.valueOf(dform.getINBKPARM_PCODE()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_ACQ_FLAG())))
				auditLog.addParam("代理/被代理", String.valueOf(dform.getINBKPARM_ACQ_FLAG()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_EFFECT_DATE())))
				auditLog.addParam("生效日期", String.valueOf(dform.getINBKPARM_EFFECT_DATE()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_CUR())))
				auditLog.addParam("幣別", String.valueOf(dform.getINBKPARM_CUR()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_RANGE_FROM())))
				auditLog.addParam("金額起始範圍", String.valueOf(dform.getINBKPARM_RANGE_FROM()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_RANGE_TO())))
				auditLog.addParam("金額結束範圍", String.valueOf(dform.getINBKPARM_RANGE_TO()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_TYPE())))
				auditLog.addParam("手續費型態", String.valueOf(dform.getINBKPARM_FEE_TYPE()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_MBR_DR())))
				auditLog.addParam("應收財金手續費", String.valueOf(dform.getINBKPARM_FEE_MBR_DR()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_MBR_CR())))
				auditLog.addParam("應付財金手續費", String.valueOf(dform.getINBKPARM_FEE_MBR_CR()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_ASS_DR())))
				auditLog.addParam("應收同業/銀行公會手續費", String.valueOf(dform.getINBKPARM_FEE_ASS_DR()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_ASS_CR())))
				auditLog.addParam("應付同業/銀行公會手續費", String.valueOf(dform.getINBKPARM_FEE_ASS_CR()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_CUSTPAY())))
				auditLog.addParam("客戶需付手續費", String.valueOf(dform.getINBKPARM_FEE_CUSTPAY()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_PRNCRDB())))
				auditLog.addParam("本金借貸別", String.valueOf(dform.getINBKPARM_PRNCRDB()));
			if (StringUtils.isNotBlank(String.valueOf(dform.getINBKPARM_FEE_MIN())))
				auditLog.addParam("客戶需付手續費", String.valueOf(dform.getINBKPARM_FEE_MIN()));
			if ("insert".equals(dform.getBtnType())) {
				// 塞入auditLog.action
				auditLog.setAction("新增");
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				String INBKPARM_EFFECT_DATE = StringUtils.replace(dform.getINBKPARM_EFFECT_DATE(), "-", StringUtils.EMPTY);
				inbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
				iRes = inbkService.insertINBKPARM(inbkparm);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
				}
			} else {
				// 塞入auditLog.action
				auditLog.setAction("修改");
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				String INBKPARM_EFFECT_DATE = StringUtils.replace(dform.getINBKPARM_EFFECT_DATE(), "-", StringUtils.EMPTY);
				inbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
				iRes = inbkService.updateINBKPARM(inbkparm);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
				}
			}
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}
	private boolean checkAllField(Inbkparm inbkparm, UI_070030_Detail_Form dform, RedirectAttributes redirectAttributes) {
		try {
			inbkparm.setInbkparmApid(dform.getINBKPARM_APID());
			if("".equals(dform.getINBKPARM_PCODE())) {
				inbkparm.setInbkparmPcode(dform.getINBKPARM_PCODE());
			}
			else {
				inbkparm.setInbkparmPcode(dform.getINBKPARM_PCODE());
			}
	
			if (dform.getINBKPARM_PCODE().equals(dform.getINBKPARM_APID())) {
				this.showMessage(redirectAttributes, MessageType.WARNING, "財金 PCODE等於財金APID, 則不需輸入財金PCODE");
				return false;
			}
			inbkparm.setInbkparmAcqFlag(dform.getINBKPARM_ACQ_FLAG());
			inbkparm.setInbkparmEffectDate(dform.getINBKPARM_EFFECT_DATE());
			inbkparm.setInbkparmCur(dform.getINBKPARM_CUR());
			inbkparm.setInbkparmRangeFrom(dform.getINBKPARM_RANGE_FROM());
			if(StringUtils.isBlank(dform.getINBKPARM_RANGE_TO().toString())){
				inbkparm.setInbkparmRangeTo(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmRangeTo(dform.getINBKPARM_RANGE_TO());
			}
			inbkparm.setInbkparmFeeType(dform.getINBKPARM_FEE_TYPE());
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MBR_DR().toString())){
				inbkparm.setInbkparmFeeMbrDr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMbrDr(dform.getINBKPARM_FEE_MBR_DR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MBR_CR().toString())){
				inbkparm.setInbkparmFeeMbrCr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMbrCr(dform.getINBKPARM_FEE_MBR_CR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_ASS_CR().toString())){
				inbkparm.setInbkparmFeeAssCr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeAssCr(dform.getINBKPARM_FEE_ASS_CR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_ASS_DR().toString())){
				inbkparm.setInbkparmFeeAssDr(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeAssDr(dform.getINBKPARM_FEE_ASS_DR());
			}
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_CUSTPAY().toString())){
				inbkparm.setInbkparmFeeCustpay(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeCustpay(dform.getINBKPARM_FEE_CUSTPAY());
			}
			inbkparm.setInbkparmPrncrdb(dform.getINBKPARM_PRNCRDB());
			if(StringUtils.isBlank(dform.getINBKPARM_FEE_MIN().toString())){
				inbkparm.setInbkparmFeeMin(BigDecimal.ZERO);
			}
			else {
				inbkparm.setInbkparmFeeMin(dform.getINBKPARM_FEE_MIN());
			}
			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(redirectAttributes, MessageType.WARNING, programError);
			return false;
		}
	}

	public void BindGridData(UI_070030_Form form, ModelMap mode) {
		PageInfo<Inbkparm> pageInfo = null;
		try {
			String APID = form.getINBKPARM_APID();
			String INBKPARM_ACQ_FLAG= form.getINBKPARM_ACQ_FLAG().toString();
			String INBKPARM_CUR = form.getINBKPARM_CUR();
			String INBKPARM_EFFECT_DATE = form.getINBKPARM_EFFECT_DATE();
			//String INBKPARM_RANGE_FROM = form.getINBKPARM_RANGE_FROM().toString();
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(String.valueOf(form.getInbkparmApid())))
				auditLog.addParam("財金APID", String.valueOf(form.getInbkparmApid()));
			if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_ACQ_FLAG())))
				auditLog.addParam("代理/被代理", String.valueOf(form.getINBKPARM_ACQ_FLAG()));
			if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_EFFECT_DATE())))
				auditLog.addParam("生效日期", String.valueOf(form.getINBKPARM_EFFECT_DATE()));
			if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_CUR())))
				auditLog.addParam("幣別", String.valueOf(form.getINBKPARM_CUR()));
			if (StringUtils.isNotBlank(String.valueOf(form.getINBKPARM_RANGE_FROM())))
				auditLog.addParam("金額起始範圍", String.valueOf(form.getINBKPARM_RANGE_FROM()));
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);

			if (StringUtils.isBlank(form.getINBKPARM_APID())
					&& "N".equals(form.getINBKPARM_ACQ_FLAG().toString())
					&& StringUtils.isBlank(form.getINBKPARM_EFFECT_DATE())
					&& StringUtils.isBlank(form.getINBKPARM_CUR())
					&& form.getINBKPARM_RANGE_FROM()==null) {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
									inbkService.getInbkparmAll();
							}
						});
			} else {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
                                    inbkService.getINBKPARMByPK(APID, INBKPARM_ACQ_FLAG, INBKPARM_CUR,
                                            INBKPARM_EFFECT_DATE, form.getINBKPARM_RANGE_FROM());
							}
						});
			}
			if (pageInfo.getList() == null || pageInfo.getList().size() == 0) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
				return;
			} else {
				this.clearMessage(mode);
				PageData<UI_070030_Form, Inbkparm> pageData = new PageData<>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}

		} catch (Exception exception) {
			this.errorMessage(exception, exception.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
