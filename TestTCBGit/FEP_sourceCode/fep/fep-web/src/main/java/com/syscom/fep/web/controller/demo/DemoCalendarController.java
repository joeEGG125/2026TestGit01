package com.syscom.fep.web.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.demo.DemoCalendar_FormMain;
import com.syscom.fep.web.util.WebUtil;

@Controller
public class DemoCalendarController extends BaseController {

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		DemoCalendar_FormMain form = new DemoCalendar_FormMain();
		form.setChooseYear("2022");
		form.setActiveCalendar("20220713,20220714");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/demo/DemoCalendar/inquiryMain")
	public String doInquiryMain(@ModelAttribute DemoCalendar_FormMain form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			// 查詢後設置需要高亮顯示的日期
			form.setActiveCalendar("20220713,20220714,20220715");
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.DEMOCalendar.getView();
	}
}
