package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Clrtotal;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019201_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 查詢清算總計資料(本行)
 *
 * @author xingyun_yang
 * @create 2021/9/13
 */
@Controller
public class UI_019201Controller extends BaseController {

	@Autowired
	private InbkService inbkService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_019201_Form form = new UI_019201_Form();
		form.setClearDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		// 財金STAN
		String bankNo = StringUtils.EMPTY;
		try {
			bankNo = SysStatus.getPropertyValue().getSysstatHbkno();
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, "查詢SYSSTAT出現異常");
		}
		form.setLblBankNo(bankNo);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/inbk/UI_019201/queryClick", produces = "application/json;charset=utf-8")
	public String queryClick(@ModelAttribute UI_019201_Form form, ModelMap mode) {
		bindFormViewData(form, mode);
		return Router.UI_019201.getView();
	}

	protected void bindFormViewData(UI_019201_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		Clrtotal clrtotal = new Clrtotal();
		try {
			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getLblBankNo())) {
				auditLog.addParam("銀行代號", form.getLblBankNo());
			}
			if (StringUtils.isNotBlank(form.getClearDate())) {
				auditLog.addParam("清算日期", form.getClearDate().replace("-",StringUtils.EMPTY));
			}
			setAuditLog(auditLog);

			String stdate = StringUtils.replace(form.getClearDate(), "-", StringUtils.EMPTY);
			List<HashMap<String, Object>> dtMaintain = inbkService.getAPTOTByStDate(stdate);
			List<HashMap<String, Object>> dt = inbkService.getFUNDLOGByTxDate(stdate);
			if (dtMaintain == null || dtMaintain.size() == 0 || dt.get(0) == null) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			} else {
				if (dtMaintain.size() == 1) {
					if ("2".equals(dtMaintain.get(0).get("APTOT_APID").toString())) {
						clrtotal.setClrtotalAtmCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMAMTCNTDR"))));
						clrtotal.setClrtotalAtmAmtDr(new BigDecimal(dtMaintain.get(0).get("SUMAMTDR").toString()));
						clrtotal.setClrtotalAtmCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMAMTCNTCR"))));
						clrtotal.setClrtotalAtmAmtCr(new BigDecimal(dtMaintain.get(0).get("SUMAMTCR").toString()));
						clrtotal.setClrtotalAtmEcCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMECCNTDR"))));
						clrtotal.setClrtotalAtmEcAmtDr(new BigDecimal(dtMaintain.get(0).get("SUMECDR").toString()));
						clrtotal.setClrtotalAtmEcCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMECCNTCR"))));
						clrtotal.setClrtotalAtmEcAmtCr(new BigDecimal(dtMaintain.get(0).get("SUMECCR").toString()));
						clrtotal.setClrtotalRmCntDr(0);
						clrtotal.setClrtotalRmAmtDr(new BigDecimal("0.00"));
						clrtotal.setClrtotalRmCntCr(0);
						clrtotal.setClrtotalRmAmtCr(new BigDecimal("0.00"));
					} else {
						clrtotal.setClrtotalAtmCntDr(0);
						clrtotal.setClrtotalAtmAmtDr(new BigDecimal("0.00"));
						clrtotal.setClrtotalAtmCntCr(0);
						clrtotal.setClrtotalAtmAmtCr(new BigDecimal("0.00"));
						clrtotal.setClrtotalAtmEcCntDr(0);
						clrtotal.setClrtotalAtmEcAmtDr(new BigDecimal("0.00"));
						clrtotal.setClrtotalAtmEcCntCr(0);
						clrtotal.setClrtotalAtmEcAmtCr(new BigDecimal("0.00"));
						clrtotal.setClrtotalRmCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMAMTCNTDR"))));
						clrtotal.setClrtotalRmAmtDr(new BigDecimal(dtMaintain.get(0).get("SUMAMTDR").toString()));
						clrtotal.setClrtotalRmCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(0).get("SUMAMTCNTCR"))));
						clrtotal.setClrtotalRmAmtCr(new BigDecimal(dtMaintain.get(0).get("SUMAMTCR").toString()));
					}
				} else {
					// '2012-3-8 KK for CD小計&通匯小計寫反了
					int atm, rm;
					if ("2".equals(dtMaintain.get(0).get("APTOT_APID").toString())) {
						atm = 0;
						rm = 1;
					} else {
						atm = 1;
						rm = 0;
					}
					clrtotal.setClrtotalAtmCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(atm).get("SUMAMTCNTDR"))));
					clrtotal.setClrtotalAtmAmtDr(new BigDecimal(dtMaintain.get(atm).get("SUMAMTDR").toString()));
					clrtotal.setClrtotalAtmCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(atm).get("SUMAMTCNTCR"))));
					clrtotal.setClrtotalAtmAmtCr(new BigDecimal(dtMaintain.get(atm).get("SUMAMTCR").toString()));
					clrtotal.setClrtotalAtmEcCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(atm).get("SUMECCNTDR"))));
					clrtotal.setClrtotalAtmEcAmtDr(new BigDecimal(dtMaintain.get(atm).get("SUMECDR").toString()));
					clrtotal.setClrtotalAtmEcCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(atm).get("SUMECCNTCR"))));
					clrtotal.setClrtotalAtmEcAmtCr(new BigDecimal(dtMaintain.get(atm).get("SUMECCR").toString()));
					clrtotal.setClrtotalRmCntDr(Integer.parseInt(String.valueOf(dtMaintain.get(rm).get("SUMAMTCNTDR"))));
					clrtotal.setClrtotalRmAmtDr(new BigDecimal(dtMaintain.get(rm).get("SUMAMTDR").toString()));
					clrtotal.setClrtotalRmCntCr(Integer.parseInt(String.valueOf(dtMaintain.get(rm).get("SUMAMTCNTCR"))));
					clrtotal.setClrtotalRmAmtCr(new BigDecimal(dtMaintain.get(rm).get("SUMAMTCR").toString()));
				}
				clrtotal.setClrtotalFeeAmtDr(new BigDecimal("0"));
				clrtotal.setClrtotalFeeAmtCr(new BigDecimal("0"));
				clrtotal.setClrtotalFeeEcAmtDr(new BigDecimal("0"));
				clrtotal.setClrtotalFeeEcAmtCr(new BigDecimal("0"));
				clrtotal.setClrtotalSumAmtDr(isnullTz(clrtotal.getClrtotalAtmAmtDr()).add(isnullTz(clrtotal.getClrtotalAtmEcAmtDr())).add(isnullTz(clrtotal.getClrtotalRmAmtDr())));
				clrtotal.setClrtotalSumAmtCr(isnullTz(clrtotal.getClrtotalAtmAmtCr()).add(isnullTz(clrtotal.getClrtotalAtmEcAmtCr())).add(isnullTz(clrtotal.getClrtotalRmAmtCr())));
				BigDecimal zero = new BigDecimal(0);
				if (clrtotal.getClrtotalSumAmtDr().subtract(clrtotal.getClrtotalSumAmtCr()).compareTo(zero) > 0) {
					clrtotal.setClrtotalOddsDr(new BigDecimal(clrtotal.getClrtotalSumAmtDr().toString()).subtract(clrtotal.getClrtotalSumAmtCr()));
					clrtotal.setClrtotalOddsCr(new BigDecimal(0));
				} else {
					clrtotal.setClrtotalOddsCr(new BigDecimal(clrtotal.getClrtotalSumAmtCr().toString()).subtract(clrtotal.getClrtotalSumAmtDr()));
					clrtotal.setClrtotalOddsDr(new BigDecimal(0));
				}
				if (CollectionUtils.isEmpty(dt) || MapUtils.isEmpty(dt.get(0)) || (new BigDecimal(dt.get(0).get("CNT").toString()).intValue()) == 0) {
					clrtotal.setClrtotalFgCntDr(0);
					clrtotal.setClrtotalFgAmtDr(new BigDecimal("0"));
				} else {
					clrtotal.setClrtotalFgCntDr((new BigDecimal(dt.get(0).get("CNT").toString()).intValue()));
					clrtotal.setClrtotalFgAmtDr(new BigDecimal(dt.get(0).get("SUMFGAMTCR").toString()));
				}
				clrtotal.setClrtotalFgCntCr(0);
				clrtotal.setClrtotalFgAmtCr(new BigDecimal("0"));
			}
			this.clearMessage(mode);
			WebUtil.putInAttribute(mode, AttributeName.DetailEntity, clrtotal);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
