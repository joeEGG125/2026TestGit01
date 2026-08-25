package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.inbk.UI_013114_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.server.common.TxHelper;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @title 跨行OPC作業 CD/ATM 作業狀況通
 * @date 2022/05/13
 * @author Han
 */
@Controller
public class UI_013114Controller extends BaseController {

	@Autowired
	private AtmService atmService;

	@Override
	public void pageOnLoad(ModelMap mode) {
//		showMessage(mode, MessageType.INFO, "");
		UI_013114_Form form = new UI_013114_Form();
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	/**
	 * 確認按鈕
	 */
	@PostMapping(value = "/inbk/UI_013114/btnConfirm")
	public String btnConfirm(@ModelAttribute UI_013114_Form form, ModelMap mode) {

		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		FISCGeneral aData = new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
		try {

			if (!CheckData(form, mode)) {
				return Router.UI_013114.getView();
			}

			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			if ("0".equals(form.getRbtn_ATMNo()))
			{
				auditLog.addParam("ATM代號", "ALL");
			}
			else
			{
				auditLog.addParam("ATM代號", form.getaTMNoTxt());
			}
			if (StringUtils.isNotBlank(form.getaTMWorkStatusDdl())) {
				auditLog.addParam("ATM作業狀況", form.getaTMWorkStatusDdl());
			}
			if (StringUtils.isNotBlank(form.getaTMServiceStatusDdl())) {
				auditLog.addParam("ATM服務型態", form.getaTMServiceStatusDdl());
			}
			setAuditLog(auditLog);

			aData.setOPCRequest(new FISC_OPC());
			aData.setSubSystem(FISCSubSystem.OPC);
			aData.getOPCRequest().setAtmStatus("01" + StringUtils.rightPad(form.getaTMNoTxt(), 8, ' ')
					+ form.getaTMWorkStatusDdl() + form.getaTMServiceStatusDdl());
			aData.getOPCRequest().setAtmStatus(EbcdicConverter.toHex(CCSID.English,aData.getOPCRequest().getAtmStatus().length(),aData.getOPCRequest().getAtmStatus()));

			if ("0".equals(form.getRbtn_ATMNo())) {
				form.setaTMNoTxt("");
			}

			aData.getOPCRequest().setProcessingCode("3114");
			aData.getOPCRequest().setMessageType("0600");
			aData.getOPCRequest().setMessageKind(MessageFlow.Request);

			// Call AA
			fepHandler.dispatch(FEPChannel.FEP, aData);

			// 將AA RC 顯示在UI上
			if (StringUtils.isBlank(aData.getDescription())) {
				aData.setDescription(MessageError);
			}

			String massage[] = aData.getDescription().split("-");

			if (massage.length == 2 && NormalRC.FISC_OK.equals(massage[0])) {

				// 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
				showMessage(mode, MessageType.INFO, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
			} else {
				showMessage(mode, MessageType.DANGER, aData.getDescription());
			}

		} catch (Exception e) {
			errorMessage(e, e.getMessage());
			showMessage(mode, MessageType.DANGER, programError);
		} finally {

		}
		return Router.UI_013114.getView();
	}

	/**
	 * @title 資料驗證
	 * @param form
	 * @param mode
	 * @return
	 */
	private boolean CheckData(UI_013114_Form form, ModelMap mode) {

		List<Map<String, Object>> dt = null;

		if ("0".equals(form.getRbtn_ATMNo())) {

			form.setaTMNoTxt("ALLCDATM");

		} else if ("1".equals(form.getRbtn_ATMNo())) {

			dt = atmService.getSingleATM(form.getaTMNoTxt());

			if (null == dt || dt.size() != 1) {
				showMessage(mode, MessageType.DANGER, WebCodeConstant.NoATMNo); // 此櫃員機號不存在ATM主檔中
				return false;
			}
		}
		return true;
	}

}
