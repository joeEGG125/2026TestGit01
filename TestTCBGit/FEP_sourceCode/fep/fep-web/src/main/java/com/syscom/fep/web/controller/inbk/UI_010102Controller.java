package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.inbk.UI_010102_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;

import com.syscom.fep.web.audit.AuditLog;
/**
 * 變更押碼基碼-0102
 * 
 * @author Richard
 */
@Controller
public class UI_010102Controller extends BaseController {

	@PostMapping(value = "/inbk/UI_010102/execute")
	@ResponseBody
	public BaseResp<?> execute(@RequestBody UI_010102_Form form) {
		this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
		BaseResp<?> response = new BaseResp<>();
		try {
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("執行");
			if (StringUtils.isNotBlank(form.getKeyId())) {
				auditLog.addParam("選擇 KEY-ID", form.getKeyId());
			}
			setAuditLog(auditLog);

			FISCGeneral aData = new FISCGeneral();
			aData.setOPCRequest(new FISC_OPC());
			aData.setSubSystem(FISCSubSystem.OPC);
			aData.getOPCRequest().setMessageKind(MessageFlow.Request);
			aData.getOPCRequest().setProcessingCode("0102");
			aData.getOPCRequest().setMessageType("0800");
			aData.getOPCRequest().setKEYID(form.getKeyId());
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
}
