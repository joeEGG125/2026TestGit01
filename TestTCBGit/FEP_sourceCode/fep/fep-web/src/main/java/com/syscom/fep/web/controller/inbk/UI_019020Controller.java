package com.syscom.fep.web.controller.inbk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.inbk.UI_019020_FormDetail;
import com.syscom.fep.web.form.inbk.UI_019020_FormMain;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 查詢OPC交易記錄
 * 
 * @author Richard
 */
@Controller
public class UI_019020Controller extends BaseController {
	@Autowired
	private InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		this.bindConstant(mode);
		// 初始化表單資料
		UI_019020_FormMain form = new UI_019020_FormMain();
		// 交易日期
		form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	/**
	 * 為頁面綁定一些常量
	 * 
	 * @param mode
	 */
	private void bindConstant(ModelMap mode) {
		// 初始化PCODE下拉選單
		List<SelectOption<String>> selectOptionList = new ArrayList<>();
		selectOptionList.add(new SelectOption<String>("0101-財金公司押碼基碼同步通知交易", "0101"));
		selectOptionList.add(new SelectOption<String>("0102-參加單位變更押碼基碼請求交易", "0102"));
		selectOptionList.add(new SelectOption<String>("0105-財金公司變更3-DES押碼基碼通知交易", "0105"));
		selectOptionList.add(new SelectOption<String>("0107-財金公司變更TR-31 Key Block PP KEY交換基碼通知交易", "0107"));
		selectOptionList.add(new SelectOption<String>("3100-參加單位訊息通知交易", "3100"));
		selectOptionList.add(new SelectOption<String>("3101-參加單位應用系統連線作業請求交易", "3101"));
		selectOptionList.add(new SelectOption<String>("3106-參加單位應用系統異常連線作業結束交易", "3106"));
		selectOptionList.add(new SelectOption<String>("3107-參加單位應用系統緊急停止後重新啟動通知交易", "3107"));
		selectOptionList.add(new SelectOption<String>("3109-參加單位應用系統狀態查詢交易", "3109"));
		selectOptionList.add(new SelectOption<String>("3113-參加單位不明訊息通知交易", "3113"));
		selectOptionList.add(new SelectOption<String>("3201-財金公司訊息通知交易", "3201"));
		selectOptionList.add(new SelectOption<String>("3209-財金公司不明訊息通知交易", "3209"));
		selectOptionList.add(new SelectOption<String>("3210-財金公司應用系統連線結束", "3210"));
		selectOptionList.add(new SelectOption<String>("3211-預定連線作業強迫結束交易", "3211"));
		selectOptionList.add(new SelectOption<String>("3215-財金公司CD/ATM作業狀況查詢交易", "3215"));
		selectOptionList.add(new SelectOption<String>("3114-參加單位CD/ATM作業狀況通知", "3114"));
		selectOptionList.add(new SelectOption<String>("3115-參加單位查詢CD/ATM作業狀況", "3115"));
		WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
	}

	@PostMapping(value = "/inbk/UI_019020/inquiryMain")
	public String doInquiryMain(@ModelAttribute UI_019020_FormMain form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.bindConstant(mode);
		this.doKeepFormData(mode, form);
		try {
			String nbsday = StringUtils.EMPTY;
			String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
			// 找下營業日
			Bsdays bsdays = inbkService.getBsdaysByPk(ZoneCode.TWN, tradingDate);
			if (bsdays != null) {
				// 工作日
				if (DbHelper.toBoolean(bsdays.getBsdaysWorkday())) {
					nbsday = bsdays.getBsdaysNbsdy();
				} else {
					// 2012/12/10 Modify by Ruling BSDAYS_NBSDY的值要塞給nbSDY
					nbsday = bsdays.getBsdaysNbsdy();
				}
			}

			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getTradingDate())) {
				auditLog.addParam("交易日期", form.getTradingDate());
			}
			switch (form.getRadioOption()) {
				case EJNO:
					if (form.getEjno() != null) {
						auditLog.addParam("EJNO", form.getEjno());
					}
					break;
				case PCODE:
					if (StringUtils.isNotBlank(form.getPcode())) {
						auditLog.addParam("PCODE", form.getPcode());
					}
					break;
				case STAN:
					if (StringUtils.isNotBlank(form.getBkno())) {
						auditLog.addParam("STAN BKNO", form.getBkno());
					}
					if (StringUtils.isNotBlank(form.getStan())) {
						auditLog.addParam("STAN", form.getStan());
					}
					break;
				default:
					break;
			}
			setAuditLog(auditLog);

			FeptxnExt feptxn = new FeptxnExt();
			feptxn.setTableNameSuffix(tradingDate.substring(6, 8));
			feptxn.setFeptxnTxDate(tradingDate);
			switch (form.getRadioOption()) {
				case EJNO:
					feptxn.setFeptxnEjfno(form.getEjno());
					break;
				case PCODE:
					feptxn.setFeptxnPcode(form.getPcode());
					break;
				case STAN:
					feptxn.setFeptxnBkno(form.getBkno());
					feptxn.setFeptxnStan(form.getStan());
					break;
				default:
					break;
			}
			if (StringUtils.isNotBlank(nbsday) && nbsday.length() >= 8) {
				nbsday = nbsday.substring(6, 8);
			}
			PageInfo<Feptxn> pageInfo = inbkService.getFeptxnByTxDate(feptxn, nbsday, form.getPageNum(), form.getPageSize());
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			}
			PageData<UI_019020_FormMain, Feptxn> pageData = new PageData<UI_019020_FormMain, Feptxn>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_019020.getView();
	}

	@PostMapping(value = "/inbk/UI_019020/inquiryDetail")
	public String doInquiryDetail(@ModelAttribute UI_019020_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			String nbsday = StringUtils.EMPTY;
			String tradingDate = form.getFeptxnTxDate();
			// 找下營業日
			Bsdays bsdays = inbkService.getBsdaysByPk(ZoneCode.TWN, tradingDate);
			if (bsdays != null) {
				// 工作日
				if (DbHelper.toBoolean(bsdays.getBsdaysWorkday())) {
					nbsday = bsdays.getBsdaysNbsdy();
				} else {
					// 2012/12/10 Modify by Ruling BSDAYS_NBSDY的值要塞給nbSDY
					nbsday = bsdays.getBsdaysNbsdy();
				}
			}
			FeptxnExt feptxn = new FeptxnExt();
			feptxn.setTableNameSuffix(tradingDate.substring(6, 8));
			feptxn.setFeptxnTxDate(tradingDate);
			feptxn.setFeptxnEjfno(form.getFeptxnEjfno());
			if (StringUtils.isNotBlank(nbsday) && nbsday.length() >= 8) {
				nbsday = nbsday.substring(6, 8);
			}

			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(tradingDate)) {
				auditLog.addParam("交易日期", tradingDate);
			}
			if (form.getFeptxnEjfno() != null) {
				auditLog.addParam("EJ序號", form.getFeptxnEjfno());
			}
			setAuditLog(auditLog);

			PageInfo<Feptxn> pageInfo = inbkService.getFeptxnByTxDate(feptxn, nbsday, 1, 0);
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				feptxn = (FeptxnExt) pageInfo.getList().get(0);
			}
			// 應該只會有一筆資料
			WebUtil.putInAttribute(mode, AttributeName.DetailEntity, feptxn);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_019020_Detail.getView();
	}
}
