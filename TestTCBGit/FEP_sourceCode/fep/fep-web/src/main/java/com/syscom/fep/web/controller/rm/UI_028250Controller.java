package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.mybatis.model.Prgstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028250_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *  調整程式執行狀態交易
 * @author jie
 * @create 2021/12/9
 */
@Controller
public class UI_028250Controller extends BaseController {

	@Autowired
	RmService rmService;
	LogData _logContext = new LogData();
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028250_Form form = new UI_028250_Form();	
		form.setPRGSTAT_FLAG("0");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/rm/UI_028250/executeClick", produces = "application/json;charset=utf-8")
	public String btnExecute(@ModelAttribute UI_028250_Form form, ModelMap mode) {
		this.infoMessage("調整程式執行狀態交易, 條件 = [", form, "]");
		this.doKeepFormData(mode, form);
		try {
			Prgstat defPRGSTAT = new Prgstat();
			defPRGSTAT.setPrgstatProgramid(form.getProgramID());
			defPRGSTAT.setPrgstatFlag(Integer.parseInt(form.getPRGSTAT_FLAG()));
			if (rmService.updatePRGSTATByPK(defPRGSTAT) > 0) {
				this.showMessage(mode, MessageType.SUCCESS, DealSuccess);
			} else {
				this.showMessage(mode, MessageType.DANGER, UpdateFail);
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			FEPBase.sendEMS(getLogContext());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_028250.getView();
	}
}
