package com.syscom.fep.web.controller.inbk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.WkpostdtlExtMapper;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.inbk.UI_019401_Form;
import com.syscom.fep.web.util.WebUtil;

/**
 * 過帳明細檔查詢
 *
 * @author xingyun_yang
 * @create 2021/9/16
 */
@Controller
public class UI_019401Controller extends BaseController {

	@Autowired
	WkpostdtlExtMapper wkpostdtlExtMapper;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化日期
		UI_019401_Form form = new UI_019401_Form();
		form.setLblStDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		mode.addAttribute("DRCNT", "借方總金額=0");
		mode.addAttribute("CRCNT", "貸方總金額=0");
		// 初始化PCODE下拉選單
		List<SelectOption<String>> options = new ArrayList<>();
		options.add(new SelectOption<String>(" ", ""));
		options.add(new SelectOption<String>("211:FEP入帳-ATM", "211"));
		options.add(new SelectOption<String>("212-FEP入帳-匯款", "212"));
		mode.addAttribute("options", options);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_019401/queryClick", produces = "application/json;charset=utf-8")
	public String queryClick(@ModelAttribute UI_019401_Form form, ModelMap mode) {
		bindData(form, mode);
		return Router.UI_019401.getView();
	}

	@PostMapping(value = "/inbk/UI_019401/selectOptionList", produces = "application/json;charset=utf-8")
	public String selectOptionList(@ModelAttribute UI_019401_Form form, ModelMap mode) {
		this.doKeepFormData(mode, form);
		List<SelectOption<String>> options = new ArrayList<>();
		if ("1".equals(form.getLblZone())) {
			options.add(new SelectOption<String>(" ", ""));
			options.add(new SelectOption<String>("211:FEP入帳-ATM", "211"));
			options.add(new SelectOption<String>("212-FEP入帳-匯款", "212"));
			mode.addAttribute("DRCNT", "借方總金額=0");
			mode.addAttribute("CRCNT", "貸方總金額=0");
		} else if ("2".equals(form.getLblZone())) {
			options.add(new SelectOption<String>("253:ATM跨行系統入帳", "253"));
		} else if ("3".equals(form.getLblZone())) {
			options.add(new SelectOption<String>("263:ATM跨行系統入帳", "263"));
		}
		mode.addAttribute("options", options);
		return Router.UI_019401.getView();
	}

	protected void bindData(UI_019401_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			Date txDate = FormatUtil.parseDataTime(form.getLblStDate(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH);
			String sysCode = form.getSysCode();
			String acBranchCode = form.getAcBranchCode();
			String drCrSide = form.getDrCrSide();
			String acCode = form.getAcCode();
			String subAcCode = form.getSubAcCode();
			String dtlAcCode = form.getDtlAcCode();
			String deptCode = form.getDeptCode();
			String txAmt = form.getTxAmt();
			List<SelectOption<String>> options = new ArrayList<>();
			if ("1".equals(form.getLblZone())) {
				options.add(new SelectOption<String>(" ", ""));
				options.add(new SelectOption<String>("211:FEP入帳-ATM", "211"));
				options.add(new SelectOption<String>("212-FEP入帳-匯款", "212"));
				mode.addAttribute("DRCNT", "借方總金額=0");
				mode.addAttribute("CRCNT", "貸方總金額=0");
			} else if ("2".equals(form.getLblZone())) {
				options.add(new SelectOption<String>("253:ATM跨行系統入帳", "253"));
			} else if ("3".equals(form.getLblZone())) {
				options.add(new SelectOption<String>("263:ATM跨行系統入帳", "263"));
			}
			mode.addAttribute("options", options);
			int pageNum = form.getPageNum();
			form.setPageSize(10);
			int pageSize = form.getPageSize();
			String table = Strings.EMPTY;
			if ("1".equals(form.getLblZone())) {
				table = "WKPOSTDTL";
			} else if ("2".equals(form.getLblZone())) {
				table = "HKWKPOSTDTL";
			} else {
				table = "MOWKPOSTDTL";
			}
			String finalTable = table;

			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getLblZone())) {
				auditLog.addParam("地區", table);
			}
			if (StringUtils.isNotBlank(form.getLblStDate())) {
				auditLog.addParam("清算日期", form.getLblStDate());
			}
			if (StringUtils.isNotBlank(form.getSysCode())) {
				auditLog.addParam("系統代號", form.getSysCode());
			}
			if (StringUtils.isNotBlank(form.getAcBranchCode())) {
				auditLog.addParam("掛帳行", form.getAcBranchCode());
			}
			if (StringUtils.isNotBlank(form.getDrCrSide())) {
				auditLog.addParam("借貸別", form.getDrCrSide());
			}
			if (StringUtils.isNotBlank(form.getAcCode()) || StringUtils.isNotBlank(form.getSubAcCode()) || StringUtils.isNotBlank(form.getDtlAcCode())) {
				auditLog.addParam("會計科目", acCode +"-"+subAcCode +"-"+dtlAcCode);
			}
			if (StringUtils.isNotBlank(form.getDeptCode())) {
				auditLog.addParam("績效行", form.getDeptCode());
			}
			if (StringUtils.isNotBlank(form.getTxAmt())) {
				auditLog.addParam("金額", form.getTxAmt());
			}
			if (StringUtils.isNotBlank(form.getBrapTxType())) {
				auditLog.addParam("交易類別", form.getBrapTxType());
			}
			if (StringUtils.isNotBlank(form.getBrapCur())) {
				auditLog.addParam("幣別", form.getBrapCur());
			}
			setAuditLog(auditLog);

			// 分頁查詢
			PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
				@Override
				public void doSelect() {
					wkpostdtlExtMapper.getWkPostDtl(txDate, sysCode, acBranchCode, drCrSide, acCode, subAcCode, dtlAcCode, deptCode, txAmt, finalTable);
				}
			});
			pageInfo.setList(pageInfo.getList());
			if (pageInfo == null || pageInfo.getList().size() == 0 || pageInfo.getList().get(0) == null) {
				if ("1".equals(form.getLblZone())) {
					mode.addAttribute("DRCNT", "借方總金額=0");
					mode.addAttribute("CRCNT", "貸方總金額=0");
				} else {
					mode.addAttribute("DRCNT", "");
					mode.addAttribute("CRCNT", "");
				}
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageInfo);
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				int tempVar = pageInfo.getList().size();
				// 借方總金額
				BigDecimal drCnt = new BigDecimal("0.00");
				// 貸方總金額
				BigDecimal crCnt = new BigDecimal("0.00");
				if ("1".equals(form.getLblZone())) {
					for (int i = 0; i < tempVar; i++) {
						if ("1".equals(pageInfo.getList().get(i).get("DRCRSIDE").toString())) {
							drCnt = drCnt.add((BigDecimal) pageInfo.getList().get(i).get("TXAMT"));
						} else {
							crCnt = crCnt.add((BigDecimal) pageInfo.getList().get(i).get("TXAMT"));
						}
					}
					mode.addAttribute("DRCNT", "借方總金額=" + drCnt);
					mode.addAttribute("CRCNT", "貸方總金額=" + crCnt);
				} else {
					mode.addAttribute("DRCNT", "");
					mode.addAttribute("CRCNT", "");
				}
				PageData<UI_019401_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}

	}
}
