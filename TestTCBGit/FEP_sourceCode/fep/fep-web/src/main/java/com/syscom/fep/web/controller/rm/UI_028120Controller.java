package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028120_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Calendar;
import java.util.HashMap;

/**
 * 匯款換KEY交易查詢
 * @author jie
 * @create 2021/11/24
 */
@Controller
public class UI_028120Controller extends BaseController {

	@Autowired
	RmService rmService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028120_Form form = new UI_028120_Form();
		form.setTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/rm/UI_028120/queryClick", produces = "application/json;charset=utf-8")
	public String queryClick(@ModelAttribute UI_028120_Form form, ModelMap mode) {
		PageInfo<HashMap<String, Object>> dtResult = null;
		this.infoMessage("匯款換KEY交易查詢, 條件 = [", form, "]");
		this.doKeepFormData(mode, form);
		String txdateString = form.getTxdate().replace("-", "");
		form.setInFiscsno(StringUtils.leftPad(form.getInFiscsno(), 7, "0"));
		// '匯出
		try {
			if ("1".equals(form.getIoflag())) {
				dtResult = rmService.getMSGINUnionMSGOUT(txdateString, form.getBkno(), form.getInFiscsno(), "CHG",
						"REP",form.getPageNum(),form.getPageSize());
			} else if ("2".equals(form.getIoflag())) {
				dtResult = rmService.getMSGINUnionMSGOUT(txdateString, form.getBkno(), form.getInFiscsno(), "REP",
						"CHG", form.getPageNum(),form.getPageSize());
			}
			modifyDataTable(dtResult, mode,form);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			FEPBase.sendEMS(getLogContext());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_028120.getView();
	}

	private void modifyDataTable(PageInfo<HashMap<String, Object>> dt, ModelMap mode, UI_028120_Form form) {
		if (dt == null || dt.getSize()  == 0) {
			this.showMessage(mode, MessageType.DANGER, "一般通訊匯出/入主檔" + QueryNoData);
		} else {
			for (int i = 0; i < dt.getSize(); i++) {
				HashMap<String, Object> map = dt.getList().get(i);
				if (map.containsKey("ENGMEMO") && StringUtils.isNotBlank(map.get("ENGMEMO").toString())) {
					if (map.get("ENGMEMO").toString().length() > 35 && map.get("ENGMEMO").toString().substring(0, 4).equals("CHGK")) {
						map.put("MEMO", StringUtils.join(map.get("ENGMEMO").toString().substring(0, 4), " ", map.get("ENGMEMO").toString().substring(28, 35)));
					} else if (map.get("ENGMEMO").toString().substring(0, 4).equals("CHG3")) {
						if (map.containsKey("FISCSNO") && StringUtils.isNotBlank(map.get("FISCSNO").toString())) {
							map.put("MEMO", StringUtils.join(map.get("ENGMEMO").toString().substring(0, 4) , " " , map.get("FISCSNO").toString()));
						} else {
							map.put("MEMO", StringUtils.join(map.get("ENGMEMO").toString().substring(0, 4) , " "));
						}		
					} else if (map.get("ENGMEMO").toString().length() > 14 && map.get("ENGMEMO").toString().substring(0, 3).equals("REP")) {
						map.put("MEMO", StringUtils.join(map.get("ENGMEMO").toString().substring(0, 4) , " " , "RC=" , map.get("ENGMEMO").toString().substring(11, 15)));
					} else {
						map.put("MEMO", "");
					}
				} else {
					map.put("MEMO", "");
				}
				if (map.containsKey("SENDER_BANK") && map.containsKey("STAN")
						&& StringUtils.isNotBlank(map.get("SENDER_BANK").toString())
						&& map.get("SENDER_BANK").toString().length() >= 3
						&& StringUtils.isNotBlank(map.get("STAN").toString())) {
					map.put("OUT_STAN", (map.get("SENDER_BANK").toString().substring(0, 3)
							+ StringUtils.leftPad(map.get("STAN").toString(), 7, '0')));
				} else {
					map.put("OUT_STAN", "");
				}
			}
			PageData<UI_028120_Form,HashMap<String,Object>> pageData = new PageData<>(dt,form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		}
	}
}
