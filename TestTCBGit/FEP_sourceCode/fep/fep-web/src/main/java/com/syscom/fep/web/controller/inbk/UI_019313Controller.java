package com.syscom.fep.web.controller.inbk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.inbk.UI_019313_Form;
import com.syscom.fep.web.form.inbk.UI_019313_Send;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.mybatis.model.Fundlog;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.service.InbkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UI_019313Controller extends BaseController {
	Fundlog fundlog = new Fundlog();
	String queryFlagTxt = "";

	@Autowired
	public InbkService inbkService;

	@PostMapping(value = "/inbk/UI_019313/getFundlog")
	@ResponseBody
	public UI_019313_Send getFundlog(@RequestBody UI_019313_Form form) {
		UI_019313_Send send = new UI_019313_Send();
		try {
			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getFgSeqno())) {
				auditLog.addParam("調撥序號", form.getFgSeqno());
			}
			setAuditLog(auditLog);

			this.queryFlagTxt  = "";
			fundlog = inbkService.getFundlogByFgSeqno(form.getFgSeqno());
			if (fundlog == null) {
				send.setMessage(QueryNoData);
				send.setMessageType(MessageType.DANGER);
				send.setTlrid("");
				send.setFundlog(fundlog);
				send.setResult("Fail");
			} else {
				send.setTlrid(WebUtil.getUser().getUserId());
				send.setFgAmt(MathUtil.toString(fundlog.getFundlogFgAmt(), "0.00",false));
				send.setFundlog(fundlog);
				send.setResult("Success");
			}
			this.queryFlagTxt  = "True";
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			send.setMessage(programError);
			send.setMessageType(MessageType.DANGER);
			send.setResult("Fail");
			return send;
		}
		return send;
	}

	@PostMapping(value = "/inbk/UI_019313/insertFundlog")
	@ResponseBody
	public UI_019313_Send insertClick(@RequestBody UI_019313_Form form) {
		UI_019313_Send send = new UI_019313_Send();
		this.queryFlagTxt  = "False";
		try {
			fundlog.setFundlogTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			fundlog.setFundlogTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			fundlog.setFundlogEjfno(TxHelper.generateEj());
			fundlog.setFundlogStatus("N");
			fundlog.setFundlogCur(""); /// *幣別*/
			if (!"".equals(form.getFgAmt().trim())) {
				if (PolyfillUtil.isNumeric(form.getFgAmt().trim())) {
					fundlog.setFundlogFgAmt(new BigDecimal(form.getFgAmt().trim()));
					if (fundlog.getFundlogFgAmt().doubleValue() <= 0) {
						send.setResult("Fail");
						send.setMessage("金額必須大於0");
						send.setMessageType(MessageType.DANGER);
						return send;
					}
				} else {
					send.setResult("Fail");
					send.setMessage("金額格式錯誤");
					send.setMessageType(MessageType.DANGER);
					return send;
				}
			} else {
				send.setResult("Fail");
				send.setMessage("金額未輸入");
				send.setMessageType(MessageType.DANGER);
				return send;
			}
			// aFUNDLOG.FUNDLOG_FG_TYPE = FG_TYPE
			// aFUNDLOG.FUNDLOG_TX_TYPE = UI.TX_TYPE
			// aFUNDLOG.FUNDLOG_TRIN_BKNO = UI.TRIN_BKNO
			// aFUNDLOG.FUNDLOG_TRIN_ACTNO = TRIN_ACTNO
			fundlog.setFundlogTlrno(WebUtil.getUser().getUserId());
			fundlog.setFundlogFgSeqno(inbkService.getSEQNOByPK()); // 取序號 from 交易序號檔SEQNO (PKEY = “FGTWD”)
			fundlog.setFundlogPcode("5313");
			fundlog.setFundlogFgType((short) 2); /// *減少基金*/
			fundlog.setFundlogFgInd("D"); /// *借方*/

			AuditLog auditLog = new AuditLog();
			auditLog.setAction("新增");
			if (StringUtils.isNotBlank(fundlog.getFundlogTxDate())) {
				auditLog.addParam("輸入日期", fundlog.getFundlogTxDate());
			}
			if (StringUtils.isNotBlank(fundlog.getFundlogFgAmt().toString())) {
				auditLog.addParam("撥轉金額", fundlog.getFundlogFgAmt());
			}
			if (StringUtils.isNotBlank(fundlog.getFundlogEjfno().toString())) {
				auditLog.addParam("財金序號", fundlog.getFundlogEjfno());
			}
			if (StringUtils.isNotBlank(fundlog.getFundlogStatus())) {
				auditLog.addParam("已放行否", fundlog.getFundlogStatus());
			}
			if(StringUtils.isNotBlank(WebUtil.getUser().getUserId())){
				auditLog.addParam("輸入ＩＤ", WebUtil.getUser().getUserId());
			}
			if(StringUtils.isNotBlank(fundlog.getFundlogSupno())){
				auditLog.addParam("輸入ＩＤ", fundlog.getFundlogSupno());
			}
			setAuditLog(auditLog);

			if (inbkService.insertFundlog(fundlog) > 0) {
				send.setTlrid(WebUtil.getUser().getUserId());
				send.setFgAmt(MathUtil.toString(fundlog.getFundlogFgAmt(), "0.00",false));
				send.setFundlog(fundlog);
				send.setResult("Success");
				send.setMessage(InsertSuccess);
				send.setMessageType(MessageType.SUCCESS);
			} else {
				send.setResult("Fail");
				send.setMessage(InsertFail);
				send.setMessageType(MessageType.DANGER);
			}
			return send;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			send.setResult("Fail");
			send.setMessage(InsertFail);
			send.setMessageType(MessageType.DANGER);
			return send;
		}
	}

	@PostMapping(value = "/inbk/UI_019313/updateFundlog")
	@ResponseBody
	public UI_019313_Send updateClick(@RequestBody UI_019313_Form form) {
		UI_019313_Send send = new UI_019313_Send();
		String message = null;
		try {
			if (!"True".equals(this.queryFlagTxt )) {
				send.setResult("Fail");
				send.setMessage(PleaseDoQuery);
				send.setMessageType(MessageType.DANGER);
				return send;
			}
			fundlog.setFundlogTxDate(form.getTxDateTxt());
			fundlog.setFundlogEjfno(Integer.parseInt(form.getEjfnoTxt()));
			if (!LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).equals(fundlog.getFundlogTxDate()) || !"N".equals(form.getSelectedValue())) {/// *非 未放行*/
				message = TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.RecordStatusError);
				if (StringUtils.isBlank(message)) {
					message = MessageError;
				}
				send.setResult("Fail");
				send.setMessage(message);
				send.setMessageType(MessageType.DANGER);
			} else {
				if (!"".equals(form.getFgAmt().trim())) {
					if (PolyfillUtil.isNumeric(form.getFgAmt().trim())) {
						fundlog.setFundlogFgAmt(new BigDecimal(form.getFgAmt().trim()));
						if (fundlog.getFundlogFgAmt().doubleValue() <= 0) {
							send.setResult("Fail");
							send.setMessage("金額必須大於0");
							send.setMessageType(MessageType.DANGER);
							return send;
						}
					} else {
						send.setResult("Fail");
						send.setMessage("金額格式錯誤");
						send.setMessageType(MessageType.DANGER);
						return send;
					}
				} else {
					send.setResult("Fail");
					send.setMessage("金額未輸入");
					send.setMessageType(MessageType.DANGER);
					return send;
				}
				// aFUNDLOG.FUNDLOG_FG_SEQNO = FGSEQNOTxt.Text
				fundlog.setFundlogTlrno(form.getTlridTxt().trim());

				AuditLog auditLog = new AuditLog();
				auditLog.setAction("修改");
				if (StringUtils.isNotBlank(fundlog.getFundlogTxDate())) {
					auditLog.addParam("輸入日期", fundlog.getFundlogTxDate());
				}
				if (StringUtils.isNotBlank(fundlog.getFundlogFgAmt().toString())) {
					auditLog.addParam("撥轉金額", fundlog.getFundlogFgAmt());
				}
				if (StringUtils.isNotBlank(fundlog.getFundlogEjfno().toString())) {
					auditLog.addParam("財金序號", fundlog.getFundlogEjfno());
				}
				if (StringUtils.isNotBlank(fundlog.getFundlogStatus())) {
					auditLog.addParam("已放行否", fundlog.getFundlogStatus());
				}
				if(StringUtils.isNotBlank(WebUtil.getUser().getUserId())){
					auditLog.addParam("輸入ＩＤ", WebUtil.getUser().getUserId());
				}
				if(StringUtils.isNotBlank(fundlog.getFundlogSupno())){
					auditLog.addParam("輸入ＩＤ", fundlog.getFundlogSupno());
				}
				setAuditLog(auditLog);

				if (inbkService.updateFundlog(fundlog) > 0) {
					send.setResult("Success");
					send.setMessage(UpdateSuccess);
					send.setMessageType(MessageType.SUCCESS);
				} else {
					send.setResult("Fail");
					send.setMessage(UpdateFail);
					send.setMessageType(MessageType.DANGER);
				}
			}
			return send;
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			send.setResult("Fail");
			send.setMessage(programError);
			send.setMessageType(MessageType.DANGER);
			return send;
		}
	}

	@PostMapping(value = "/inbk/UI_019313/delFundlog")
	@ResponseBody
	public UI_019313_Send deleteClick(@RequestBody UI_019313_Form form) {
		UI_019313_Send send = new UI_019313_Send();
		String message = null;
		try {
			AuditLog auditLog = new AuditLog();
			auditLog.setAction("刪除");
			if (StringUtils.isNotBlank(fundlog.getFundlogTxDate())) {
				auditLog.addParam("輸入日期", fundlog.getFundlogTxDate());
			}
			if (StringUtils.isNotBlank(fundlog.getFundlogEjfno().toString())) {
				auditLog.addParam("財金序號", fundlog.getFundlogEjfno());
			}
			setAuditLog(auditLog);

			if (!"True".equals(this.queryFlagTxt )) {
				send.setResult("Fail");
				send.setMessage(PleaseDoQuery);
				send.setMessageType(MessageType.DANGER);
				return send;
			}
			fundlog.setFundlogTxDate(form.getTxDateTxt());
			fundlog.setFundlogEjfno(Integer.parseInt(form.getEjfnoTxt()));
			if (!LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).equals(fundlog.getFundlogTxDate()) || !"N".equals(form.getSelectedValue())) {/// *非 未放行*/
				message = TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.RecordStatusError);
				if (StringUtils.isBlank(message)) {
					message = MessageError;
				}
				send.setResult("Fail");
				send.setMessage(message);
				send.setMessageType(MessageType.DANGER);
			} else {
				if (inbkService.deleteFundlog(fundlog) > 0) {
					this.queryFlagTxt = "False";
					send.setResult("Success");
					send.setMessage(DeleteSuccess);
					send.setMessageType(MessageType.SUCCESS);
				} else {
					send.setResult("Fail");
					send.setMessage(DeleteFail);
					send.setMessageType(MessageType.DANGER);
				}
			}
			return send;
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			send.setResult("Fail");
			send.setMessage(programError);
			send.setMessageType(MessageType.DANGER);
			return send;
		}
	}
}
