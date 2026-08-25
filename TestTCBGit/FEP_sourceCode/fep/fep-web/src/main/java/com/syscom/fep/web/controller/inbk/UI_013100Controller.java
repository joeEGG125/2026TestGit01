package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.inbk.UI_013100_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;

import com.syscom.fep.web.audit.AuditLog;
/**
 * 訊息通知-3100
 * 
 * @author Richard
 */
@Controller
public class UI_013100Controller extends BaseController {

	@Override
	public void pageOnLoad(ModelMap mode) {
		String bankId = StringUtils.EMPTY;
		try {
			bankId = StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "0000");
		} catch (Exception e) {
			this.showMessageWithArgs(mode, MessageType.DANGER, DATA_INQUIRY_EXCEPTION_OCCUR_WITH_MESSAGE, "回覆金融機構代號","");
			this.errorMessage(e, e.getMessage());
		}
		WebUtil.putInAttribute(mode, AttributeName.BankId, bankId);
	}

	@PostMapping(value = "/inbk/UI_013100/execute")
	@ResponseBody
	public BaseResp<?> getConfirm(@RequestBody UI_013100_Form form) {
		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		BaseResp<?> response = new BaseResp<>();
		try {
			if ("4102".equals(form.getNoticeidd())) {
				if (!checkData(form.getIdtext())) {
					response.setMessage(MessageType.DANGER, UserIDFail);
					return response;
				}
			}

			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("確認");
			if (StringUtils.isNotBlank(form.getNoticeidd())) {
				auditLog.addParam("訊息通知代碼", form.getNoticeidd());
			}
			if (StringUtils.isNotBlank(form.getNoticetext().trim())) {
				auditLog.addParam("訊息通知內容", form.getNoticetext().trim());
			}
			if (StringUtils.isNotBlank(form.getTextnotxt())) {
				auditLog.addParam("文號", form.getTextnotxt());
			}
			if (StringUtils.isNotBlank(form.getIdtext())) {
				auditLog.addParam("身份証字號／營利事業統一編號", form.getIdtext());
			}
			if (StringUtils.isNotBlank(form.getResulttxt())) {
				auditLog.addParam("回覆結果(Y/N)", form.getResulttxt());
			}
			if (StringUtils.isNotBlank(form.getBanktxt())) {
				auditLog.addParam("回覆金融機構代號", form.getBanktxt());
			}
			setAuditLog(auditLog);

			FISCGeneral aData = new FISCGeneral();
			aData.setOPCRequest(new FISC_OPC());
			aData.setSubSystem(FISCSubSystem.OPC);
			aData.getOPCRequest().setMessageKind(MessageFlow.Request);
			aData.getOPCRequest().setProcessingCode("3100");
			aData.getOPCRequest().setMessageType("0600");
			aData.getOPCRequest().setNoticeId(form.getNoticeidd());
			if ("3000".equals(form.getNoticeidd())) {
				aData.getOPCRequest().setNoticeData(form.getNoticetext().trim());
			} else if ("4102".equals(form.getNoticeidd())) {
				aData.getOPCRequest().setNoticeData(StringUtils.join(
						form.getTextnotxt().trim(),
						form.getIdtext().trim(),
						form.getResulttxt().trim(),
						form.getBanktxt().trim(),
						StringUtils.leftPad(StringUtils.SPACE, 10, StringUtils.SPACE)));
			}
			// add by Maxine on 2011/09/02 for EMS加UserId
			aData.getOPCRequest().setLogContext(new LogData());
			// modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
			aData.getOPCRequest().getLogContext().setTxUser(WebUtil.getUser().getUserId());
			// Call AA
			this.infoMessage("Start to Call AA via FEPHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
			FEPHandler fepHandler = new FEPHandler();
			fepHandler.dispatch(FEPChannel.FEP, aData);
			// 將AA RC 顯示在UI上
			if (StringUtils.isBlank(aData.getDescription())) {
				aData.setDescription(MessageError);
			}
			String[] message = aData.getDescription().split("-");
			// 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
			if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
				response.setMessage(MessageType.SUCCESS, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
			} else {
				response.setMessage(MessageType.DANGER, aData.getDescription());
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			response.setMessage(MessageType.DANGER, programError);
		}
		return response;
	}

	private boolean checkData(String id) {
		if (id.length() == 10) {
			return utl_CheckId(id.trim());
		} else {
			return StringUtils.isNumeric(id.trim());
		}
	}
}