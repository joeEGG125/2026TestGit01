package com.syscom.fep.web.controller.rm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_020061_Form;

/**
 * 往來行庫資料維護
 *
 * @author xingyun_yang
 * @create 2021/11/22
 */
@Controller
public class UI_020061Controller extends BaseController {

	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_020061_Form form = new UI_020061_Form();
		form.setAllbankBkno("");
		form.setAllbankBrno("");
		form.setCountyDDL("");
		form.setRegionDDL("");
		form.setUrl("/rm/UI_020061/index");
		this.queryClick(form, mode);
		this.doKeepFormData(mode, form);
	}

	@PostMapping(value = "/rm/UI_020061/index")
	private String queryClick(@ModelAttribute UI_020061_Form form, ModelMap mode) {
		this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		return Router.UI_020061.getView();
	}
}
