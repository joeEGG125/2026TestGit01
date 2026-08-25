package com.syscom.fep.web.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import com.syscom.fep.web.controller.BaseController;

@Controller
public class DemoTabController extends BaseController {

	@Override
	public void pageOnLoad(ModelMap mode) {
		this.infoMessage("init");
	}
	
}
