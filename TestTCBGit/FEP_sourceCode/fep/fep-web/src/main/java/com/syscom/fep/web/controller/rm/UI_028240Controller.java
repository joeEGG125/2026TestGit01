package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028240_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *  啟動批量修改重送次數
 * @author jie
 * @create 2021/12/7
 */
@Controller
public class UI_028240Controller extends BaseController {

	@Autowired
	RmService rmService;
	LogData _logContext = new LogData();
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單數據
		UI_028240_Form form = new UI_028240_Form();
		
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/rm/UI_028240/btnExecute", produces = "application/json;charset=utf-8")
	public String btnExecute(@ModelAttribute UI_028240_Form form, ModelMap mode) {
		this.infoMessage("啟動批量修改重送次數, 條件 = [", form, "]");
		this.doKeepFormData(mode, form);
		try {
			Rmin defRMIN = new Rmin();
			RM rmBusiness = new RM();
			rmBusiness.setLogContext(_logContext);
			rmBusiness.setmRMData(new RMData());
			rmBusiness.getmRMData().setTxSubSystem(SubSystem.RM);
			rmBusiness.getmRMData().setLogContext(_logContext);
			defRMIN.setRminTxdate(FormatUtil.dateTimeFormat(CalendarUtil.rocStringToADDate(form.getTxdate().replace("-", "")),
					FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			FEPReturnCode rtnCode = rmBusiness.sendToCBS("RESET", (byte) 0, defRMIN, null, null, null);

			if (rtnCode == CommonReturnCode.Normal) {
				this.showMessage(mode, MessageType.SUCCESS, DealSuccess);
			} else {
				this.showMessage(mode, MessageType.DANGER, "主機回覆:" + RM.CBSRC);
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			FEPBase.sendEMS(getLogContext());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_028240.getView();
	}
}
