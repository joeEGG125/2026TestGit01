package com.syscom.fep.web.controller.atmmon;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.ems.model.Feplog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060610_A_FormDetail;
import com.syscom.fep.web.service.EmsService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 交易日誌(FEPLOG)查詢
 * 
 * @author Richard
 */
@Controller
public class UI_060610_AController extends BaseController {
	@Autowired
	private EmsService emsSvr;

	@PostMapping(value = "/atmmon/UI_060610_A/inquiryFeplogDetail")
	public String doInquiryFeplogDetail(@ModelAttribute UI_060610_A_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.changeTitle(mode, Router.UI_060610_A.getName());
		this.doKeepFormData(mode, form);
		try {
			Calendar logDate = CalendarUtil.parseDateTimeValue(Long.parseLong(form.getLogdate()));
			String tableNameSuffix = String.valueOf(CalendarUtil.getDayOfWeek(logDate));
			Feplog feplog = emsSvr.getFeplogDetail(form.getLogno(), tableNameSuffix);
			if (feplog == null) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				String txMessage = feplog.getTxmessage();
				if (StringUtils.isNotBlank(txMessage)) {
					if (XmlUtil.isXML(txMessage)) {
						Element root = XmlUtil.load(txMessage);
						txMessage = XmlUtil.getChildElementValue(root, "message", StringUtils.EMPTY);
					}
				}
				if (StringUtils.isNotBlank(txMessage))
					feplog.setTxmessage(txMessage);
				if (StringUtils.isNotBlank(feplog.getRemark()))
					feplog.setRemark(feplog.getRemark());
			}
			// 應該只會有一筆資料
			WebUtil.putInAttribute(mode, AttributeName.DetailEntity, feplog);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_060610_A_Detail.getView();
	}
}
