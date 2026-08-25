package com.syscom.fep.web.controller.rm;

import java.util.Calendar;
import java.util.List;

import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028190_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 一般通訊確認取消交易
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028190Controller extends BaseController {
	@Autowired
	RmService rmService;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_028190_Form form = new UI_028190_Form();
		form.setUrl("/rm/UI_028190/queryClick");
		this.queryClick(form, mode);
	}

	@PostMapping(value = "/rm/UI_028190/deleteClick")
	@ResponseBody
	public BaseResp<?> deleteClick(@RequestBody List<UI_028190_Form> formList, ModelMap mode) {
		this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
		BaseResp<?> response = new BaseResp<>();
		try {
			int needUpdateCount = 0, updateCount = 0, iRes;
			Msgout defMsgout = new Msgout();
			StringBuilder errorMessage = new StringBuilder();

			for (UI_028190_Form gr : formList) {
				needUpdateCount += 1;
				defMsgout.setMsgoutTxdate(gr.getMsgoutTxdate());
				defMsgout.setMsgoutBrno(gr.getMsgoutBrno());
				defMsgout.setMsgoutFepno(gr.getMsgoutFepno());
				// FEP取消
				defMsgout.setMsgoutStat("07");
				defMsgout.setMsgoutRegTlrno(WebUtil.getUser().getUserId());
				iRes = rmService.updateMSGOUTByPK(defMsgout);
				String strMsg = "PK(MSGOUT_TXDATE=" + defMsgout.getMsgoutTxdate() +
						";MSGOUT_BRNO=" + defMsgout.getMsgoutBrno() +
						";MSGOUT_FEPNO=" + defMsgout.getMsgoutFepno() + ")";
				if (iRes != 1) {
					strMsg = "取消失敗, 請查明原因" + strMsg;
					errorMessage.append("FEP序號:" + defMsgout.getMsgoutFepno()); // +vbCrlf
				} else {
					strMsg = "交易成功" + strMsg;
					updateCount += 1;
				}
				prepareAndSendEMSData(mode, strMsg);
			}
			if (needUpdateCount == updateCount) {
				response.setMessage(MessageType.INFO, UpdateSuccess);
			} else {
				response.setMessage(MessageType.DANGER, errorMessage + UpdateFail);
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, UpdateFail);
		}
		return response;
	}

	@PostMapping(value = "/rm/UI_028190/queryClick")
	public String queryClick(@ModelAttribute UI_028190_Form form, ModelMap mode) {
		this.infoMessage("執行UI_028190, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		bindGridData(mode, form);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_028190.getView();
	}

	// 資料整理 依查詢條件查詢的主程式。
	private void bindGridData(ModelMap mode, UI_028190_Form form) {
		this.infoMessage("執行UI_028190, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			Msgout defMsgout = new Msgout();
			defMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
			defMsgout.setMsgoutStat("02");
			PageInfo<Msgout> dtResult = rmService.getMsgOutByDef(defMsgout, form.getPageNum(), form.getPageSize());
			if (dtResult == null || dtResult.getList().size() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				this.clearMessage(mode);
			}
			WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}

	/**
	 * 執行成功送成功信息
	 */
	private void prepareAndSendEMSData(ModelMap mode, String strMsg) throws Exception {

		try {
			List<Sysstat> _dtSYSSTAT = rmService.getStatus();
			if (_dtSYSSTAT.size() < 1) {
				this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料!!");
				return;
			}
		} catch (Exception ex) {
			this.showMessage(mode, MessageType.DANGER, ex.getMessage());
			return;
		}
		logContext.setChannel(FEPChannel.FEP);
		logContext.setSubSys(SubSystem.RM);
		logContext.setProgramName("UI_028190");
		logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
		logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
		logContext.setMessageId("UI028190");
		/* Rm */
		logContext.setMessageGroup("4");
		logContext.setMessageParm13("一般通訊確認取消交易" + strMsg);
		logContext.setTxUser(WebUtil.getUser().getUserId());
		logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.SendRMOUTCancel, logContext));
		rmService.logMessage(logContext, Level.INFO);
	}
}